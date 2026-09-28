package com.asiet.aids.timetable.dao;

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
 * Handles all JDBC operations for the `room` table.
 * Covers both CLASSROOM and LAB rows - they live in the same table,
 * differentiated by the `type` column.
 */
public class RoomDAO {

    public void save(Room room) throws SQLException {
        String sql = "INSERT INTO room (room_name, room_type, capacity) VALUES (?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, room.getRoomName());
            stmt.setString(2, room.getType().name());
            stmt.setInt(3, room.getCapacity());

            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    room.setRoomId(keys.getInt(1));
                }
            }
        }
    }

    public List<Room> findAll() throws SQLException {
        String sql = "SELECT * FROM room ORDER BY room_name";
        List<Room> rooms = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                rooms.add(mapRow(rs));
            }
        }
        return rooms;
    }

    public Room findById(int roomId) throws SQLException {
        String sql = "SELECT * FROM room WHERE room_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, roomId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    /**
     * Used by the engine to fetch all interchangeable labs (or all
     * classrooms) when looking for a free room of a given type.
     */
    public List<Room> findByType(Room.Type type) throws SQLException {
        String sql = "SELECT * FROM room WHERE room_type = ? ORDER BY room_name";
        List<Room> rooms = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, type.name());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    rooms.add(mapRow(rs));
                }
            }
        }
        return rooms;
    }

    public void update(Room room) throws SQLException {
        String sql = "UPDATE room SET room_name = ?, room_type = ?, capacity = ? WHERE room_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, room.getRoomName());
            stmt.setString(2, room.getType().name());
            stmt.setInt(3, room.getCapacity());
            stmt.setInt(4, room.getRoomId());

            stmt.executeUpdate();
        }
    }

    public void delete(int roomId) throws SQLException {
        String sql = "DELETE FROM room WHERE room_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, roomId);
            stmt.executeUpdate();
        }
    }

    private Room mapRow(ResultSet rs) throws SQLException {
        Room room = new Room();
        room.setRoomId(rs.getInt("room_id"));
        room.setRoomName(rs.getString("room_name"));
        room.setType(Room.Type.valueOf(rs.getString("room_type")));
        room.setCapacity(rs.getInt("capacity"));
        return room;
    }
}
