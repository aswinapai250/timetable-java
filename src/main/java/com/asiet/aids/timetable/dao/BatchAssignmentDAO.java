package com.asiet.aids.timetable.dao;

import com.asiet.aids.timetable.model.Batch;
import com.asiet.aids.timetable.model.BatchAssignment;
import com.asiet.aids.timetable.model.Subject;
import com.asiet.aids.timetable.model.Teacher;
import com.asiet.aids.timetable.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles all JDBC operations for the `batch_assignment` table -
 * fixed input data telling the engine which subject/teacher a batch
 * does for its lab session (Phase 1: static, no auto-rotation).
 */
public class BatchAssignmentDAO {

    private final BatchDAO batchDAO = new BatchDAO();
    private final SubjectDAO subjectDAO = new SubjectDAO();
    private final TeacherDAO teacherDAO = new TeacherDAO();

    public void save(BatchAssignment ba) throws SQLException {
        String sql = "INSERT INTO batch_assignment (batch_id, subject_id, teacher_id) VALUES (?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, ba.getBatch().getBatchId());
            stmt.setInt(2, ba.getSubject().getSubjectId());
            stmt.setInt(3, ba.getTeacher().getTeacherId());

            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    ba.setBatchAssignmentId(keys.getInt(1));
                }
            }
        }
    }

    /**
     * The main method the engine calls: "what lab subject + teacher
     * is this specific batch assigned to?"
     */
    public List<BatchAssignment> findByBatchId(int batchId) throws SQLException {
        String sql = "SELECT * FROM batch_assignment WHERE batch_id = ?";
        List<BatchAssignment> list = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, batchId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    public void delete(int batchAssignmentId) throws SQLException {
        String sql = "DELETE FROM batch_assignment WHERE batch_assignment_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, batchAssignmentId);
            stmt.executeUpdate();
        }
    }

    private BatchAssignment mapRow(ResultSet rs) throws SQLException {
        BatchAssignment ba = new BatchAssignment();
        ba.setBatchAssignmentId(rs.getInt("batch_assignment_id"));

        Batch batch = batchDAO.findById(rs.getInt("batch_id"));
        Subject subject = subjectDAO.findById(rs.getInt("subject_id"));
        Teacher teacher = teacherDAO.findById(rs.getInt("teacher_id"));

        ba.setBatch(batch);
        ba.setSubject(subject);
        ba.setTeacher(teacher);

        return ba;
    }
}
