package com.asiet.aids.timetable.ui;

import com.asiet.aids.timetable.dao.TeacherDAO;
import com.asiet.aids.timetable.model.Teacher;
import com.asiet.aids.timetable.util.PasswordUtil;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

public class TeacherLoginDialog extends JDialog {

    private final TeacherDAO teacherDAO = new TeacherDAO();
    private final JTextField emailField = new JTextField(22);
    private final JPasswordField passwordField = new JPasswordField(22);
    private Teacher authenticated;

    public TeacherLoginDialog(Frame owner) {
        super(owner, "Teacher Login", true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel form = new JPanel(new GridLayout(2, 2, 6, 6));
        form.setBorder(BorderFactory.createEmptyBorder(12, 12, 6, 12));
        form.add(new JLabel("Email:"));
        form.add(emailField);
        form.add(new JLabel("Password:"));
        form.add(passwordField);

        JButton loginButton = new JButton("Login");
        loginButton.addActionListener(e -> attemptLogin());
        getRootPane().setDefaultButton(loginButton);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(loginButton);

        setLayout(new BorderLayout());
        add(form, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(owner);
    }

    private void attemptLogin() {
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());
        try {
            Teacher t = teacherDAO.findByEmail(email);
            String hash = PasswordUtil.sha256(password);
            if (t != null && hash.equalsIgnoreCase(t.getPasswordHash())) {
                authenticated = t;
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Invalid email or password.",
                        "Login failed", JOptionPane.ERROR_MESSAGE);
                passwordField.setText("");
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public Teacher getAuthenticatedTeacher() {
        return authenticated;
    }
}