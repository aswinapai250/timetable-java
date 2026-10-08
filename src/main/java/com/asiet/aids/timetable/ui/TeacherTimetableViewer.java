package com.asiet.aids.timetable.ui;

import com.asiet.aids.timetable.dao.TimetableEntryDAO;
import com.asiet.aids.timetable.model.Slot;
import com.asiet.aids.timetable.model.Teacher;
import com.asiet.aids.timetable.model.TimetableEntry;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TeacherTimetableViewer extends JFrame {

    private final TimetableEntryDAO entryDAO = new TimetableEntryDAO();
    private final Teacher teacher;

    private JComboBox<String> viewPicker;
    private JLabel summaryLabel;
    private JTable table;
    private DefaultTableModel tableModel;
    private final Map<String, List<TimetableEntry>> lookup = new HashMap<>();

    public TeacherTimetableViewer(Teacher teacher) {
        super("My Timetable - " + teacher.getName());
        this.teacher = teacher;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1300, 500);
        setLocationRelativeTo(null);
        buildUI();
        loadEntries();
    }

    private void buildUI() {
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Logged in: " + teacher.getName()));

        viewPicker = new JComboBox<>();
        viewPicker.addItem("Week");
        for (Slot.Day d : Slot.Day.values()) viewPicker.addItem(d.name());
        viewPicker.addActionListener(e -> render());

        JButton refresh = new JButton("Refresh");
        refresh.addActionListener(e -> loadEntries());

        summaryLabel = new JLabel();

        top.add(new JLabel("View:"));
        top.add(viewPicker);
        top.add(refresh);
        top.add(summaryLabel);

        tableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setRowHeight(32);

        setLayout(new BorderLayout());
        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    private void loadEntries() {
        try {
            List<TimetableEntry> entries = entryDAO.findByTeacherId(teacher.getTeacherId());
            lookup.clear();
            for (TimetableEntry e : entries) {
                String key = e.getSlot().getDay() + "-" + e.getSlot().getPeriod();
                lookup.computeIfAbsent(key, k -> new ArrayList<>()).add(e);
            }
            summaryLabel.setText("   " + entries.size() + " periods/week (max "
                    + teacher.getMaxWeeklyHours() + ")");
            render();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Failed to load timetable: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void render() {
        String selected = (String) viewPicker.getSelectedItem();
        if (selected == null) return;

        Slot.Day[] days = "Week".equals(selected)
                ? Slot.Day.values()
                : new Slot.Day[]{Slot.Day.valueOf(selected)};

        Object[] columns = new Object[days.length + 1];
        columns[0] = "Period";
        for (int i = 0; i < days.length; i++) columns[i + 1] = days[i].name();

        tableModel.setColumnIdentifiers(columns);
        tableModel.setRowCount(0);

        for (int period = 1; period <= 7; period++) {
            Object[] row = new Object[days.length + 1];
            row[0] = period;
            for (int i = 0; i < days.length; i++) {
                row[i + 1] = describe(lookup.get(days[i] + "-" + period));
            }
            tableModel.addRow(row);
        }
        table.getColumnModel().getColumn(0).setPreferredWidth(50);
    }

    private String describe(List<TimetableEntry> list) {
        if (list == null || list.isEmpty()) return "Free";
        StringBuilder sb = new StringBuilder();
        for (TimetableEntry e : list) {
            if (sb.length() > 0) sb.append(" | ");
            sb.append(e.getSubject().getSubjectCode())
              .append(" - ").append(e.getClassGroup().getClassName());
            if (e.getBatch() != null) sb.append(" (").append(e.getBatch()).append(")");
            sb.append(" @ ").append(e.getRoom().getRoomName());
            if (e.getStatus() == TimetableEntry.Status.SUBSTITUTED) sb.append(" [SUB]");
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            TeacherLoginDialog login = new TeacherLoginDialog(null);
            login.setVisible(true);
            Teacher t = login.getAuthenticatedTeacher();
            if (t == null) System.exit(0);
            new TeacherTimetableViewer(t).setVisible(true);
        });
    }
}