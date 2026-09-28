package com.asiet.aids.timetable.ui;

import com.asiet.aids.timetable.dao.ClassGroupDAO;
import com.asiet.aids.timetable.dao.TimetableEntryDAO;
import com.asiet.aids.timetable.exception.SchedulingException;
import com.asiet.aids.timetable.model.ClassGroup;
import com.asiet.aids.timetable.model.TimetableEntry;
import com.asiet.aids.timetable.service.TimetableGenerationService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AdminTimetableViewer extends JFrame {

    private final ClassGroupDAO classGroupDAO = new ClassGroupDAO();
    private final TimetableEntryDAO entryDAO = new TimetableEntryDAO();
    private final TimetableGenerationService engine = new TimetableGenerationService();

    private JComboBox<ClassGroup> classPicker;
    private JTable table;
    private DefaultTableModel tableModel;

    private JTable legendTable;
    private DefaultTableModel legendModel;

    private static final String[] COLUMNS = {
            "Period", "MON", "TUE", "WED", "THU", "FRI"
    };

    private static final String[] LEGEND_COLUMNS = {
            "Code", "Subject Name", "Teacher", "Room"
    };

    public AdminTimetableViewer() {
        super("Timetable Generator - Admin View");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 500);
        setSize(1400, 700);
        setLocationRelativeTo(null);

        buildUI();
        loadClasses();
    }

    private void buildUI() {
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        classPicker = new JComboBox<>();
        classPicker.addActionListener(e -> loadTimetableForSelectedClass());

        JButton regenerateButton = new JButton("Regenerate");
        regenerateButton.addActionListener(e -> onRegenerateClicked());

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadTimetableForSelectedClass());

        JButton addClassButton = new JButton("Add Class");
        addClassButton.addActionListener(e -> onAddClassClicked());

        topPanel.add(new JLabel("Class:"));
        topPanel.add(classPicker);
        topPanel.add(regenerateButton);
        topPanel.add(refreshButton);
        topPanel.add(addClassButton);

        tableModel = new DefaultTableModel(COLUMNS, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
table.getColumnModel().getColumn(0).setPreferredWidth(50);
for (int i = 1; i <= 5; i++) {
    table.getColumnModel().getColumn(i).setPreferredWidth(260);
}
        legendModel = new DefaultTableModel(LEGEND_COLUMNS, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        legendTable = new JTable(legendModel);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                new JScrollPane(table), new JScrollPane(legendTable));
        splitPane.setResizeWeight(0.6);

        setLayout(new BorderLayout());
        add(topPanel, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);
    }

    private void loadClasses() {
        try {
            List<ClassGroup> classes = classGroupDAO.findAll();
            classPicker.removeAllItems();
            for (ClassGroup c : classes) {
                classPicker.addItem(c);
            }
            if (classPicker.getItemCount() > 0) {
                loadTimetableForSelectedClass();
            }
        } catch (SQLException ex) {
            showError("Failed to load classes: " + ex.getMessage());
        }
    }

    private void loadTimetableForSelectedClass() {
        ClassGroup selected = (ClassGroup) classPicker.getSelectedItem();
        if (selected == null) return;

        try {
            List<TimetableEntry> entries = entryDAO.findByClassId(selected.getClassId());

            java.util.Map<String, List<TimetableEntry>> lookup = new java.util.HashMap<>();
            java.util.Map<Integer, Object[]> legendRows = new java.util.LinkedHashMap<>();

            for (TimetableEntry entry : entries) {
                String key = entry.getSlot().getDay() + "-" + entry.getSlot().getPeriod();
                lookup.computeIfAbsent(key, k -> new ArrayList<>()).add(entry);

                int subjectId = entry.getSubject().getSubjectId();
                if (!legendRows.containsKey(subjectId)) {
                    String room = entry.getSubject().isLab() ? entry.getRoom().getRoomName() : "";
                    legendRows.put(subjectId, new Object[]{
                            entry.getSubject().getSubjectCode(),
                            entry.getSubject().getSubjectName(),
                            entry.getTeacher().getName(),
                            room
                    });
                }
            }

            tableModel.setRowCount(0);
            com.asiet.aids.timetable.model.Slot.Day[] days = com.asiet.aids.timetable.model.Slot.Day.values();

            for (int period = 1; period <= 7; period++) {
                Object[] row = new Object[6];
                row[0] = period;

                for (int col = 0; col < days.length; col++) {
                    String key = days[col] + "-" + period;
                    List<TimetableEntry> slotEntries = lookup.get(key);

                    if (slotEntries == null || slotEntries.isEmpty()) {
                        row[col + 1] = "Placement Training";
                    } else if (slotEntries.size() == 1) {
                        TimetableEntry entry = slotEntries.get(0);
                        row[col + 1] = entry.getSubject().getSubjectName() + " - " + entry.getSubject().getSubjectCode();
                    } else {
                        StringBuilder sb = new StringBuilder();
                        for (TimetableEntry e : slotEntries) {
                            if (sb.length() > 0) sb.append(" | ");
                            String batchLabel = (e.getBatch() != null) ? e.getBatch().toString() + ": " : "";
                            sb.append(batchLabel).append(e.getSubject().getSubjectCode());
                        }
                        row[col + 1] = sb.toString();
                    }
                }
                tableModel.addRow(row);
            }

            legendModel.setRowCount(0);
            for (Object[] legendRow : legendRows.values()) {
                legendModel.addRow(legendRow);
            }
        } catch (SQLException ex) {
            showError("Failed to load timetable: " + ex.getMessage());
        }
    }

    private void onRegenerateClicked() {
        ClassGroup selected = (ClassGroup) classPicker.getSelectedItem();
        if (selected == null) return;

        int confirm = JOptionPane.showConfirmDialog(this,
                "This wipes the existing timetable for " + selected.getClassName() +
                        " and regenerates it. Continue?",
                "Confirm Regenerate", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            engine.generateForClass(selected);
            JOptionPane.showMessageDialog(this,
                    "Regeneration successful for " + selected.getClassName() + ".",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            loadTimetableForSelectedClass();
        } catch (SchedulingException ex) {
            showError("Scheduling failed: " + ex.getMessage() +
                    "\n\nNote: the previous timetable for " + selected.getClassName() +
                    " was already cleared. Click Refresh to see the current " +
                    "(empty) state, or fix the underlying data and try again.");
        } catch (SQLException ex) {
            showError("Database error during regeneration: " + ex.getMessage());
        }
    }

    private void onAddClassClicked() {
        AddClassDialog dialog = new AddClassDialog(this);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            loadClasses();
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AdminTimetableViewer().setVisible(true));
    }
}