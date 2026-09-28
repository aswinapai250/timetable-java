package com.asiet.aids.timetable.dao;

import com.asiet.aids.timetable.model.Subject;
import com.asiet.aids.timetable.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles all JDBC operations for the `subject` table.
 */
public class SubjectDAO {

    public void save(Subject subject) throws SQLException {
        String sql = "INSERT INTO subject (subject_code, subject_name, subject_type, weekly_hours, session_length) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, subject.getSubjectCode());
            stmt.setString(2, subject.getSubjectName());
            stmt.setString(3, subject.getType().name());
            stmt.setInt(4, subject.getWeeklyHours());
            stmt.setInt(5, subject.getSessionLength());

            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    subject.setSubjectId(keys.getInt(1));
                }
            }
        }
    }

    public List<Subject> findAll() throws SQLException {
        String sql = "SELECT * FROM subject ORDER BY subject_code";
        List<Subject> subjects = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                subjects.add(mapRow(rs));
            }
        }
        return subjects;
    }

    public Subject findById(int subjectId) throws SQLException {
        String sql = "SELECT * FROM subject WHERE subject_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, subjectId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public Subject findByCode(String subjectCode) throws SQLException {
        String sql = "SELECT * FROM subject WHERE subject_code = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, subjectCode);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public void update(Subject subject) throws SQLException {
        String sql = "UPDATE subject SET subject_code = ?, subject_name = ?, subject_type = ?, weekly_hours = ?, session_length = ? WHERE subject_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, subject.getSubjectCode());
            stmt.setString(2, subject.getSubjectName());
            stmt.setString(3, subject.getType().name());
            stmt.setInt(4, subject.getWeeklyHours());
            stmt.setInt(5, subject.getSessionLength());
            stmt.setInt(6, subject.getSubjectId());

            stmt.executeUpdate();
        }
    }

    public void delete(int subjectId) throws SQLException {
        String sql = "DELETE FROM subject WHERE subject_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, subjectId);
            stmt.executeUpdate();
        }
    }

    private Subject mapRow(ResultSet rs) throws SQLException {
        Subject subject = new Subject();
        subject.setSubjectId(rs.getInt("subject_id"));
        subject.setSubjectCode(rs.getString("subject_code"));
        subject.setSubjectName(rs.getString("subject_name"));
        subject.setType(Subject.Type.valueOf(rs.getString("subject_type")));
        subject.setWeeklyHours(rs.getInt("weekly_hours"));
        subject.setSessionLength(rs.getInt("session_length"));
        return subject;
    }
}
