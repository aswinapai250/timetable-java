package com.asiet.aids.timetable.ui;

import com.asiet.aids.timetable.dao.*;
import com.asiet.aids.timetable.model.*;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;

/**
 * Dialog for adding a brand-new class: room, subjects (existing or new),
 * teachers (existing or new, matched by name with a confirm step), and
 * automatic batch-split handling based on student count vs lab capacity.
 *
 * On Save, inserts happen in FK-safe order:
 * Room (if new) -> ClassGroup -> Batch (if split) -> Subject (if new) ->
 * Teacher (if new/confirmed) -> TeacherSubjectClass -> BatchAssignment (if split lab).
 */
public class AddClassDialog extends JDialog {

    private final RoomDAO roomDAO = new RoomDAO();
    private final SubjectDAO subjectDAO = new SubjectDAO();
    private final TeacherDAO teacherDAO = new TeacherDAO();
    private final ClassGroupDAO classGroupDAO = new ClassGroupDAO();
    private final BatchDAO batchDAO = new BatchDAO();
    private final BatchAssignmentDAO batchAssignmentDAO = new BatchAssignmentDAO();
    private final TeacherSubjectClassDAO tscDAO = new TeacherSubjectClassDAO();

    private JTextField classNameField;
    private JTextField studentCountField;
    private JComboBox<Object> roomCombo;
    private JPanel newRoomPanel;
    private JTextField newRoomNameField;
    private JComboBox<Room.Type> newRoomTypeCombo;
    private JTextField newRoomCapacityField;

    private JPanel subjectRowsPanel;
    private final List<SubjectRow> subjectRows = new ArrayList<>();

    private List<Room> existingRooms;
    private List<Subject> existingSubjects;

    private boolean saved = false;

    public AddClassDialog(Frame owner) {
        super(owner, "Add New Class", true);
        loadReferenceData();
        buildUI();
        pack();
        setLocationRelativeTo(owner);
    }

    public boolean isSaved() {
        return saved;
    }

    private void loadReferenceData() {
        try {
            existingRooms = roomDAO.findAll();
            existingSubjects = subjectDAO.findAll();
        } catch (SQLException e) {
            existingRooms = new ArrayList<>();
            existingSubjects = new ArrayList<>();
            JOptionPane.showMessageDialog(this,
                    "Could not load existing rooms/subjects: " + e.getMessage(),
                    "Load Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void buildUI() {
        setLayout(new BorderLayout(8, 8));

        JPanel topPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4, 4, 4, 4);
        gc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        gc.gridx = 0; gc.gridy = row; topPanel.add(new JLabel("Class Name:"), gc);
        classNameField = new JTextField(15);
        gc.gridx = 1; gc.gridy = row; topPanel.add(classNameField, gc);
        row++;

        gc.gridx = 0; gc.gridy = row; topPanel.add(new JLabel("Total Students:"), gc);
        studentCountField = new JTextField(15);
        gc.gridx = 1; gc.gridy = row; topPanel.add(studentCountField, gc);
        row++;

        gc.gridx = 0; gc.gridy = row; topPanel.add(new JLabel("Room:"), gc);
        roomCombo = new JComboBox<>();
        for (Room r : existingRooms) roomCombo.addItem(r);
        roomCombo.addItem("-- New Room --");
        roomCombo.addActionListener(e -> toggleNewRoomPanel());
        gc.gridx = 1; gc.gridy = row; topPanel.add(roomCombo, gc);
        row++;

        newRoomPanel = new JPanel(new GridBagLayout());
        GridBagConstraints ngc = new GridBagConstraints();
        ngc.insets = new Insets(2, 4, 2, 4);
        ngc.fill = GridBagConstraints.HORIZONTAL;

        ngc.gridx = 0; ngc.gridy = 0; newRoomPanel.add(new JLabel("New Room Name:"), ngc);
        newRoomNameField = new JTextField(12);
        ngc.gridx = 1; ngc.gridy = 0; newRoomPanel.add(newRoomNameField, ngc);

        ngc.gridx = 0; ngc.gridy = 1; newRoomPanel.add(new JLabel("Room Type:"), ngc);
        newRoomTypeCombo = new JComboBox<>(Room.Type.values());
        ngc.gridx = 1; ngc.gridy = 1; newRoomPanel.add(newRoomTypeCombo, ngc);

        ngc.gridx = 0; ngc.gridy = 2; newRoomPanel.add(new JLabel("Capacity:"), ngc);
        newRoomCapacityField = new JTextField(12);
        ngc.gridx = 1; ngc.gridy = 2; newRoomPanel.add(newRoomCapacityField, ngc);

        newRoomPanel.setVisible(false);
        gc.gridx = 0; gc.gridy = row; gc.gridwidth = 2;
        topPanel.add(newRoomPanel, gc);
        gc.gridwidth = 1;
        row++;

        add(topPanel, BorderLayout.NORTH);

        subjectRowsPanel = new JPanel();
        subjectRowsPanel.setLayout(new BoxLayout(subjectRowsPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(subjectRowsPanel);
        scrollPane.setPreferredSize(new Dimension(600, 300));
        scrollPane.setBorder(BorderFactory.createTitledBorder("Subjects"));
        add(scrollPane, BorderLayout.CENTER);

        addSubjectRow();

        JPanel bottomPanel = new JPanel(new BorderLayout());
        JButton addSubjectBtn = new JButton("+ Add Subject");
        addSubjectBtn.addActionListener(e -> addSubjectRow());
        bottomPanel.add(addSubjectBtn, BorderLayout.WEST);

        JPanel actionPanel = new JPanel();
        JButton saveBtn = new JButton("Save Class");
        saveBtn.addActionListener(e -> onSave());
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> dispose());
        actionPanel.add(saveBtn);
        actionPanel.add(cancelBtn);
        bottomPanel.add(actionPanel, BorderLayout.EAST);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void toggleNewRoomPanel() {
        boolean isNew = "-- New Room --".equals(roomCombo.getSelectedItem());
        newRoomPanel.setVisible(isNew);
        pack();
    }

    private void addSubjectRow() {
        SubjectRow rowPanel = new SubjectRow(existingSubjects);
        subjectRows.add(rowPanel);
        subjectRowsPanel.add(rowPanel);
        subjectRowsPanel.revalidate();
        subjectRowsPanel.repaint();
        pack();
    }

    // ---------------- SAVE LOGIC ----------------

    private void onSave() {
        String className = classNameField.getText().trim();
        String studentCountText = studentCountField.getText().trim();

        if (className.isEmpty()) {
            showError("Class name is required.");
            return;
        }
        int studentCount;
        try {
            studentCount = Integer.parseInt(studentCountText);
            if (studentCount <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            showError("Total students must be a positive number.");
            return;
        }
        if (subjectRows.isEmpty()) {
            showError("Add at least one subject.");
            return;
        }
        for (SubjectRow row : subjectRows) {
            String err = row.validateRow();
            if (err != null) {
                showError(err);
                return;
            }
        }

        try {
            // 1. Room (existing or new)
            Room room = resolveRoom();
            if (room == null) return; // error already shown

            // 2. ClassGroup
            ClassGroup classGroup = new ClassGroup();
            classGroup.setClassName(className);
            classGroup.setStudentCount(studentCount);
            classGroup.setHomeRoom(room);
            classGroupDAO.save(classGroup);

            // 3. Determine batch split
            List<Room> labs = roomDAO.findByType(Room.Type.LAB);
            boolean needsSplit = !labs.isEmpty() && classGroup.needsBatchSplit(labs.get(0).getCapacity());
            Batch batchA = null;
            Batch batchB = null;
            if (needsSplit) {
                int half = studentCount / 2;
                int rest = studentCount - half;
                batchA = new Batch();
                batchA.setClassGroup(classGroup);
                batchA.setBatchName("A");
                batchA.setStudentCount(half);
                batchDAO.save(batchA);

                batchB = new Batch();
                batchB.setClassGroup(classGroup);
                batchB.setBatchName("B");
                batchB.setStudentCount(rest);
                batchDAO.save(batchB);
            }

            // 4. Per-subject: resolve subject, resolve teacher, save TSC (+ BatchAssignment if lab+split)
            for (SubjectRow row : subjectRows) {
                Subject subject = resolveSubject(row);
                Teacher teacher = resolveTeacher(row.getTeacherName());
                if (teacher == null) return; // user cancelled mid-flow

                TeacherSubjectClass tsc = new TeacherSubjectClass();
                tsc.setTeacher(teacher);
                tsc.setSubject(subject);
                tsc.setClassGroup(classGroup);
                tscDAO.save(tsc);

                if (subject.isLab() && needsSplit) {
                    Batch assignedBatch = "A".equals(row.getSelectedBatchLabel()) ? batchA : batchB;
                    BatchAssignment ba = new BatchAssignment();
                    ba.setBatch(assignedBatch);
                    ba.setSubject(subject);
                    ba.setTeacher(teacher);
                    batchAssignmentDAO.save(ba);
                }
            }

            saved = true;
            JOptionPane.showMessageDialog(this,
                    "Class '" + className + "' created successfully.",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            dispose();

        } catch (SQLException ex) {
            showError("Database error while saving: " + ex.getMessage());
        }
    }

    private Room resolveRoom() throws SQLException {
        Object selected = roomCombo.getSelectedItem();
        if (selected instanceof Room) {
            return (Room) selected;
        }
        // new room
        String name = newRoomNameField.getText().trim();
        String capacityText = newRoomCapacityField.getText().trim();
        if (name.isEmpty()) {
            showError("New room name is required.");
            return null;
        }
        int capacity;
        try {
            capacity = Integer.parseInt(capacityText);
        } catch (NumberFormatException ex) {
            showError("Room capacity must be a number.");
            return null;
        }
        Room room = new Room();
        room.setRoomName(name);
        room.setType((Room.Type) newRoomTypeCombo.getSelectedItem());
        room.setCapacity(capacity);
        roomDAO.save(room);
        return room;
    }

    private Subject resolveSubject(SubjectRow row) throws SQLException {
        if (row.isExistingSubjectSelected()) {
            return row.getSelectedExistingSubject();
        }
        Subject subject = new Subject();
        subject.setSubjectCode(row.getNewSubjectCode());
        subject.setSubjectName(row.getNewSubjectName());
        subject.setType(row.getNewSubjectType());
        subject.setWeeklyHours(row.getNewSubjectWeeklyHours());
        subject.setSessionLength(row.getNewSubjectSessionLength());
        subjectDAO.save(subject);
        return subject;
    }

    /**
     * Looks up a teacher by name (case-insensitive). If matches exist, asks the
     * user to confirm whether it's the same person. Returns the resolved
     * Teacher, or null if the user cancelled.
     */
    private Teacher resolveTeacher(String name) throws SQLException {
        List<Teacher> allTeachers = teacherDAO.findAll();
        Teacher existingMatch = null;
        for (Teacher t : allTeachers) {
            if (t.getName().equalsIgnoreCase(name.trim())) {
                existingMatch = t;
                break; // edge case of multiple matches: out of scope for now
            }
        }

        if (existingMatch != null) {
            int choice = JOptionPane.showConfirmDialog(this,
                    "Found existing teacher: " + existingMatch.getName() +
                            " (" + existingMatch.getEmail() + ").\nIs this the same person?",
                    "Confirm Teacher", JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) {
                return existingMatch;
            }
            // fall through to create new
        }

        Teacher newTeacher = new Teacher();
        newTeacher.setName(name.trim());
        newTeacher.setEmail("placeholder_" + System.currentTimeMillis() + "@example.com");
        newTeacher.setPasswordHash("changeme");
        newTeacher.setMaxWeeklyHours(20);
        teacherDAO.save(newTeacher);
        return newTeacher;
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Validation Error", JOptionPane.ERROR_MESSAGE);
    }

    // ---------------- SUBJECT ROW COMPONENT ----------------

    private static class SubjectRow extends JPanel {
        private final JComboBox<Object> subjectCombo;
        private final JPanel newSubjectPanel;
        private final JTextField newCodeField;
        private final JTextField newNameField;
        private final JComboBox<Subject.Type> newTypeCombo;
        private final JTextField newWeeklyHoursField;
        private final JTextField newSessionLengthField;
        private final JTextField teacherNameField;
        private final JComboBox<String> batchCombo;

        SubjectRow(List<Subject> existingSubjects) {
            setLayout(new GridBagLayout());
            setBorder(BorderFactory.createEtchedBorder());
            GridBagConstraints gc = new GridBagConstraints();
            gc.insets = new Insets(3, 3, 3, 3);
            gc.fill = GridBagConstraints.HORIZONTAL;

            subjectCombo = new JComboBox<>();
            for (Subject s : existingSubjects) subjectCombo.addItem(s);
            subjectCombo.addItem("-- New Subject --");
            subjectCombo.addActionListener(e -> toggleNewSubjectPanel());

            gc.gridx = 0; gc.gridy = 0; add(new JLabel("Subject:"), gc);
            gc.gridx = 1; gc.gridy = 0; add(subjectCombo, gc);

            newSubjectPanel = new JPanel(new GridBagLayout());
            GridBagConstraints ngc = new GridBagConstraints();
            ngc.insets = new Insets(2, 2, 2, 2);
            ngc.fill = GridBagConstraints.HORIZONTAL;

            newCodeField = new JTextField(8);
            newNameField = new JTextField(14);
            newTypeCombo = new JComboBox<>(Subject.Type.values());
            newWeeklyHoursField = new JTextField(4);
            newSessionLengthField = new JTextField(4);

            ngc.gridx = 0; ngc.gridy = 0; newSubjectPanel.add(new JLabel("Code:"), ngc);
            ngc.gridx = 1; ngc.gridy = 0; newSubjectPanel.add(newCodeField, ngc);
            ngc.gridx = 2; ngc.gridy = 0; newSubjectPanel.add(new JLabel("Name:"), ngc);
            ngc.gridx = 3; ngc.gridy = 0; newSubjectPanel.add(newNameField, ngc);
            ngc.gridx = 0; ngc.gridy = 1; newSubjectPanel.add(new JLabel("Type:"), ngc);
            ngc.gridx = 1; ngc.gridy = 1; newSubjectPanel.add(newTypeCombo, ngc);
            ngc.gridx = 2; ngc.gridy = 1; newSubjectPanel.add(new JLabel("Weekly Hrs:"), ngc);
            ngc.gridx = 3; ngc.gridy = 1; newSubjectPanel.add(newWeeklyHoursField, ngc);
            ngc.gridx = 0; ngc.gridy = 2; newSubjectPanel.add(new JLabel("Session Len:"), ngc);
            ngc.gridx = 1; ngc.gridy = 2; newSubjectPanel.add(newSessionLengthField, ngc);

            newSubjectPanel.setVisible(false);
            gc.gridx = 0; gc.gridy = 1; gc.gridwidth = 2;
            add(newSubjectPanel, gc);
            gc.gridwidth = 1;

            teacherNameField = new JTextField(16);
            gc.gridx = 0; gc.gridy = 2; add(new JLabel("Teacher Name:"), gc);
            gc.gridx = 1; gc.gridy = 2; add(teacherNameField, gc);

            batchCombo = new JComboBox<>(new String[]{"A", "B"});
            batchCombo.setToolTipText("Only used if this class needs a batch split and this is a lab subject.");
            gc.gridx = 0; gc.gridy = 3; add(new JLabel("Batch (if split):"), gc);
            gc.gridx = 1; gc.gridy = 3; add(batchCombo, gc);
        }

        private void toggleNewSubjectPanel() {
            boolean isNew = "-- New Subject --".equals(subjectCombo.getSelectedItem());
            newSubjectPanel.setVisible(isNew);
            revalidate();
            repaint();
        }

        boolean isExistingSubjectSelected() {
            return subjectCombo.getSelectedItem() instanceof Subject;
        }

        Subject getSelectedExistingSubject() {
            return (Subject) subjectCombo.getSelectedItem();
        }

        String getNewSubjectCode() { return newCodeField.getText().trim(); }
        String getNewSubjectName() { return newNameField.getText().trim(); }
        Subject.Type getNewSubjectType() { return (Subject.Type) newTypeCombo.getSelectedItem(); }

        int getNewSubjectWeeklyHours() {
            try { return Integer.parseInt(newWeeklyHoursField.getText().trim()); }
            catch (NumberFormatException e) { return 0; }
        }

        int getNewSubjectSessionLength() {
            try { return Integer.parseInt(newSessionLengthField.getText().trim()); }
            catch (NumberFormatException e) { return 1; }
        }

        String getTeacherName() { return teacherNameField.getText().trim(); }
        String getSelectedBatchLabel() { return (String) batchCombo.getSelectedItem(); }

        /** Returns an error message if this row is invalid, or null if OK. */
        String validateRow() {
            if (getTeacherName().isEmpty()) {
                return "Every subject row needs a teacher name.";
            }
            if (!isExistingSubjectSelected()) {
                if (getNewSubjectCode().isEmpty() || getNewSubjectName().isEmpty()) {
                    return "New subject rows need both a code and a name.";
                }
                try {
                    int wh = Integer.parseInt(newWeeklyHoursField.getText().trim());
                    if (wh <= 0) throw new NumberFormatException();
                } catch (NumberFormatException e) {
                    return "New subject '" + getNewSubjectCode() + "' needs a valid weekly hours value.";
                }
                if (getNewSubjectType() == Subject.Type.LAB) {
                    try {
                        int sl = Integer.parseInt(newSessionLengthField.getText().trim());
                        if (sl <= 0) throw new NumberFormatException();
                    } catch (NumberFormatException e) {
                        return "New lab subject '" + getNewSubjectCode() + "' needs a valid session length.";
                    }
                }
            }
            return null;
        }
    }
}
