package com.asiet.aids.timetable.dao;

import com.asiet.aids.timetable.model.Batch;
import com.asiet.aids.timetable.model.ClassGroup;
import com.asiet.aids.timetable.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles all JDBC operations for the `batch` table.
 * Same nested-object pattern as ClassGroupDAO: `batch` stores class_id
 * as a foreign key, but our Batch model wants a full ClassGroup object,
 * so mapRow() calls ClassGroupDAO.findById() to build it.
 */
public class BatchDAO {

    private final ClassGroupDAO classGroupDAO = new ClassGroupDAO();

    public void save(Batch batch) throws SQLException {
        String sql = "INSERT INTO batch (class_id, batch_name, student_count) VALUES (?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, batch.getClassGroup().getClassId());
            stmt.setString(2, batch.getBatchName());
            stmt.setInt(3, batch.getStudentCount());

            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    batch.setBatchId(keys.getInt(1));
                }
            }
        }
    }

    public List<Batch> findAll() throws SQLException {
        String sql = "SELECT * FROM batch ORDER BY class_id, batch_name";
        List<Batch> batches = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                batches.add(mapRow(rs));
            }
        }
        return batches;
    }

    public Batch findById(int batchId) throws SQLException {
        String sql = "SELECT * FROM batch WHERE batch_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, batchId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    /**
     * Returns all batches belonging to a given class (e.g. both
     * Batch A and Batch B for S3AI).
     */
    public List<Batch> findByClassId(int classId) throws SQLException {
        String sql = "SELECT * FROM batch WHERE class_id = ? ORDER BY batch_name";
        List<Batch> batches = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, classId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    batches.add(mapRow(rs));
                }
            }
        }
        return batches;
    }

    public void update(Batch batch) throws SQLException {
        String sql = "UPDATE batch SET class_id = ?, batch_name = ?, student_count = ? WHERE batch_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, batch.getClassGroup().getClassId());
            stmt.setString(2, batch.getBatchName());
            stmt.setInt(3, batch.getStudentCount());
            stmt.setInt(4, batch.getBatchId());

            stmt.executeUpdate();
        }
    }

    public void delete(int batchId) throws SQLException {
        String sql = "DELETE FROM batch WHERE batch_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, batchId);
            stmt.executeUpdate();
        }
    }

    private Batch mapRow(ResultSet rs) throws SQLException {
        Batch batch = new Batch();
        batch.setBatchId(rs.getInt("batch_id"));
        batch.setBatchName(rs.getString("batch_name"));
        batch.setStudentCount(rs.getInt("student_count"));

        int classId = rs.getInt("class_id");
        ClassGroup classGroup = classGroupDAO.findById(classId);
        batch.setClassGroup(classGroup);

        return batch;
    }
}
