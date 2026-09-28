package com.asiet.aids.timetable.dao;

import com.asiet.aids.timetable.model.ClassGroup;
import com.asiet.aids.timetable.model.Subject;
import com.asiet.aids.timetable.model.Teacher;
import com.asiet.aids.timetable.model.TeacherSubjectClass;
import com.asiet.aids.timetable.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles all JDBC operations for the `teacher_subject_class` table -
 * the fixed "who teaches what to whom" data the engine reads from but
 * never writes to (except when Admin sets it up).
 */
public class TeacherSubjectClassDAO {

    private final TeacherDAO teacherDAO = new TeacherDAO();
    private final SubjectDAO subjectDAO = new SubjectDAO();
    private final ClassGroupDAO classGroupDAO = new ClassGroupDAO();

    public void save(TeacherSubjectClass tsc) throws SQLException {
        String sql = "INSERT INTO teacher_subject_class (teacher_id, subject_id, class_id) VALUES (?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, tsc.getTeacher().getTeacherId());
            stmt.setInt(2, tsc.getSubject().getSubjectId());
            stmt.setInt(3, tsc.getClassGroup().getClassId());

            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    tsc.setTscId(keys.getInt(1));
                }
            }
        }
    }

    /**
     * The main method the engine calls: "give me every theory/lab
     * subject this class needs, with the teacher already assigned."
     */
    public List<TeacherSubjectClass> findByClassId(int classId) throws SQLException {
        String sql = "SELECT * FROM teacher_subject_class WHERE class_id = ?";
        List<TeacherSubjectClass> list = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, classId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    public List<TeacherSubjectClass> findByTeacherId(int teacherId) throws SQLException {
        String sql = "SELECT * FROM teacher_subject_class WHERE teacher_id = ?";
        List<TeacherSubjectClass> list = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, teacherId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    public void delete(int tscId) throws SQLException {
        String sql = "DELETE FROM teacher_subject_class WHERE tsc_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, tscId);
            stmt.executeUpdate();
        }
    }

    private TeacherSubjectClass mapRow(ResultSet rs) throws SQLException {
        TeacherSubjectClass tsc = new TeacherSubjectClass();
        tsc.setTscId(rs.getInt("tsc_id"));

        Teacher teacher = teacherDAO.findById(rs.getInt("teacher_id"));
        Subject subject = subjectDAO.findById(rs.getInt("subject_id"));
        ClassGroup classGroup = classGroupDAO.findById(rs.getInt("class_id"));

        tsc.setTeacher(teacher);
        tsc.setSubject(subject);
        tsc.setClassGroup(classGroup);

        return tsc;
    }
}
