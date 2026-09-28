package com.asiet.aids.timetable.dao;

import com.asiet.aids.timetable.model.ClassGroup;
import com.asiet.aids.timetable.model.Room;
import com.asiet.aids.timetable.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles all JDBC operations for the `class` table.
 *
 * Note: `class` holds only home_room_id as a foreign key, but our
 * ClassGroup model object wants a full Room object, not just an id.
 * So mapRow() here calls RoomDAO.findById() to build that nested object.
 * This means every read causes 2 queries (class + room) instead of 1 -
 * fine at our scale (8 classes), not something to optimize for this project.
 */
public class ClassGroupDAO {

    private final RoomDAO roomDAO = new RoomDAO();

    public void save(ClassGroup classGroup) throws SQLException {
        String sql = "INSERT INTO class (class_name, student_count, home_room_id) VALUES (?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, classGroup.getClassName());
            stmt.setInt(2, classGroup.getStudentCount());
            stmt.setInt(3, classGroup.getHomeRoom().getRoomId());

            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    classGroup.setClassId(keys.getInt(1));
                }
            }
        }
    }

    public List<ClassGroup> findAll() throws SQLException {
        String sql = "SELECT * FROM class ORDER BY class_name";
        List<ClassGroup> classes = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                classes.add(mapRow(rs));
            }
        }
        return classes;
    }

    public ClassGroup findById(int classId) throws SQLException {
        String sql = "SELECT * FROM class WHERE class_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, classId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public ClassGroup findByName(String className) throws SQLException {
        String sql = "SELECT * FROM class WHERE class_name = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, className);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public void update(ClassGroup classGroup) throws SQLException {
        String sql = "UPDATE class SET class_name = ?, student_count = ?, home_room_id = ? WHERE class_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, classGroup.getClassName());
            stmt.setInt(2, classGroup.getStudentCount());
            stmt.setInt(3, classGroup.getHomeRoom().getRoomId());
            stmt.setInt(4, classGroup.getClassId());

            stmt.executeUpdate();
        }
    }

    public void delete(int classId) throws SQLException {
        String sql = "DELETE FROM class WHERE class_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, classId);
            stmt.executeUpdate();
        }
    }

    private ClassGroup mapRow(ResultSet rs) throws SQLException {
        ClassGroup classGroup = new ClassGroup();
        classGroup.setClassId(rs.getInt("class_id"));
        classGroup.setClassName(rs.getString("class_name"));
        classGroup.setStudentCount(rs.getInt("student_count"));

        int homeRoomId = rs.getInt("home_room_id");
        Room homeRoom = roomDAO.findById(homeRoomId);
        classGroup.setHomeRoom(homeRoom);

        return classGroup;
    }
}
