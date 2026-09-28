package com.asiet.aids.timetable.dao;

import com.asiet.aids.timetable.model.Teacher;
import com.asiet.aids.timetable.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class TeacherDAO {

    public void save(Teacher teacher) throws SQLException {
        String sql = "INSERT INTO teacher (name, email, password_hash, max_weekly_hours) VALUES (?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, teacher.getName());
            stmt.setString(2, teacher.getEmail());
            stmt.setString(3, teacher.getPasswordHash());
            stmt.setInt(4, teacher.getMaxWeeklyHours());
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    teacher.setTeacherId(keys.getInt(1));
                }
            }
        }
    }

    public List<Teacher> findAll() throws SQLException {
        String sql = "SELECT * FROM teacher ORDER BY name";
        List<Teacher> teachers = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                teachers.add(mapRow(rs));
            }
        }
        return teachers;
    }

    public Teacher findById(int teacherId) throws SQLException {
        String sql = "SELECT * FROM teacher WHERE teacher_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, teacherId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    public Teacher findByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM teacher WHERE email = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    public void update(Teacher teacher) throws SQLException {
        String sql = "UPDATE teacher SET name = ?, email = ?, password_hash = ?, max_weekly_hours = ? WHERE teacher_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, teacher.getName());
            stmt.setString(2, teacher.getEmail());
            stmt.setString(3, teacher.getPasswordHash());
            stmt.setInt(4, teacher.getMaxWeeklyHours());
            stmt.setInt(5, teacher.getTeacherId());
            stmt.executeUpdate();
        }
    }

    public void delete(int teacherId) throws SQLException {
        String sql = "DELETE FROM teacher WHERE teacher_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, teacherId);
            stmt.executeUpdate();
        }
    }

    private Teacher mapRow(ResultSet rs) throws SQLException {
        Teacher teacher = new Teacher();
        teacher.setTeacherId(rs.getInt("teacher_id"));
        teacher.setName(rs.getString("name"));
        teacher.setEmail(rs.getString("email"));
        teacher.setPasswordHash(rs.getString("password_hash"));
        teacher.setMaxWeeklyHours(rs.getInt("max_weekly_hours"));
        return teacher;
    }
}