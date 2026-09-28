package com.asiet.aids.timetable.dao;

import com.asiet.aids.timetable.model.*;
import com.asiet.aids.timetable.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles all JDBC operations for the `timetable_entry` table.
 *
 * This is the DAO the backtracking engine talks to the most. Its
 * isXFree() methods are direct implementations of the "Free(...)"
 * terms in the PRD's constraint formula:
 *
 *     Constraint(T, R, C, D, P) = Free(T,D,P) x Free(R,D,P) x Free(C,D,P) x WithinQuota(T) x MaxOnePerDay(S,C,D)
 *
 * The engine will call isTeacherFree(), isRoomFree(), isClassFree() (or
 * isClassSlotFreeForBatch()), isWithinQuota(), and isSubjectFreeOnDay()
 * for a candidate slot BEFORE inserting anything. If all checks return
 * true, only then does it call save().
 */
public class TimetableEntryDAO {

    private final SlotDAO slotDAO = new SlotDAO();
    private final ClassGroupDAO classGroupDAO = new ClassGroupDAO();
    private final SubjectDAO subjectDAO = new SubjectDAO();
    private final TeacherDAO teacherDAO = new TeacherDAO();
    private final RoomDAO roomDAO = new RoomDAO();
    private final BatchDAO batchDAO = new BatchDAO();

    // ------------------------------------------------------------
    // CONFLICT CHECKS - Free(T,D,P), Free(R,D,P), Free(C,D,P)
    // ------------------------------------------------------------

    /**
     * Free(Teacher, Day, Period): true if this teacher has NO booking
     * in this slot yet.
     */
    public boolean isTeacherFree(int teacherId, int slotId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM timetable_entry WHERE teacher_id = ? AND slot_id = ?";
        return countIsZero(sql, teacherId, slotId);
    }

    /**
     * Free(Room, Day, Period): true if this room (classroom OR lab) has
     * NO booking in this slot yet.
     */
    public boolean isRoomFree(int roomId, int slotId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM timetable_entry WHERE room_id = ? AND slot_id = ?";
        return countIsZero(sql, roomId, slotId);
    }

    /**
     * Free(Class, Day, Period): true if this class has NO booking in
     * this slot yet. For split-batch labs, pass the SAME classId for
     * both batches - the class as a whole can't have 2 things at once
     * even if the batches are in different rooms with different teachers.
     */
    public boolean isClassFree(int classId, int slotId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM timetable_entry WHERE class_id = ? AND slot_id = ?";
        return countIsZero(sql, classId, slotId);
    }

    /**
     * Batch-aware version of the class-availability check.
     *
     * Plain isClassFree() would wrongly block simultaneous batch labs -
     * e.g. AI-BatchA (Python) and AI-BatchB (DSA) both share the same
     * class_id and the same slot_id by design, so isClassFree() would
     * see 1 existing row and reject the second batch.
     *
     * The real rule: a slot is free for a batch booking if there is no
     * THEORY entry (batch_id IS NULL) for this class in this slot, AND
     * this exact batch isn't already booked in this slot. Two DIFFERENT
     * batches of the same class ARE allowed to share a slot.
     *
     * For theory bookings (batchId = null), pass null and this behaves
     * exactly like isClassFree() - any existing entry for the class
     * blocks it, batched or not.
     */
    public boolean isClassSlotFreeForBatch(int classId, Integer batchId, int slotId) throws SQLException {
        String sql;
        boolean isTheoryCheck = (batchId == null);

        if (isTheoryCheck) {
            // Theory session: blocked by ANY existing entry for this class.
            sql = "SELECT COUNT(*) FROM timetable_entry WHERE class_id = ? AND slot_id = ?";
        } else {
            // Batch lab session: blocked only by a theory entry (batch_id IS NULL)
            // or by this exact same batch already being booked.
            sql = "SELECT COUNT(*) FROM timetable_entry WHERE class_id = ? AND slot_id = ? " +
                    "AND (batch_id IS NULL OR batch_id = ?)";
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, classId);
            stmt.setInt(2, slotId);
            if (!isTheoryCheck) {
                stmt.setInt(3, batchId);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                return rs.getInt(1) == 0;
            }
        }
    }

    /**
     * WithinQuota(Teacher): true if adding one more booking would NOT
     * push this teacher's total weekly bookings past their max_weekly_hours.
     */
    public boolean isWithinQuota(int teacherId, int maxWeeklyHours) throws SQLException {
        String sql = "SELECT COUNT(*) FROM timetable_entry WHERE teacher_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, teacherId);

            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                int currentBookings = rs.getInt(1);
                return currentBookings < maxWeeklyHours;
            }
        }
    }

    /**
     * MaxOnePerDay(Subject, Class, Day): true if this subject does NOT
     * already have a booking for this class on the same day as the given slot.
     * Prevents a subject's weekly sessions from stacking back-to-back
     * on one day - each session must land on a different day.
     */
    public boolean isSubjectFreeOnDay(int classId, int subjectId, int slotId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM timetable_entry te " +
                "JOIN slot s1 ON te.slot_id = s1.slot_id " +
                "JOIN slot s2 ON s2.slot_id = ? " +
                "WHERE te.class_id = ? AND te.subject_id = ? AND s1.day = s2.day";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, slotId);
            stmt.setInt(2, classId);
            stmt.setInt(3, subjectId);

            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                return rs.getInt(1) == 0;
            }
        }
    }
    public int countSubjectSessionsOnDay(int classId, int subjectId, int slotId) throws SQLException {
    String sql = "SELECT COUNT(*) FROM timetable_entry te " +
            "JOIN slot s1 ON te.slot_id = s1.slot_id " +
            "JOIN slot s2 ON s2.slot_id = ? " +
            "WHERE te.class_id = ? AND te.subject_id = ? AND s1.day = s2.day";

    try (Connection conn = DBConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

        stmt.setInt(1, slotId);
        stmt.setInt(2, classId);
        stmt.setInt(3, subjectId);

        try (ResultSet rs = stmt.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }
}
public boolean anotherSubjectAlreadyDoubledOnDay(int classId, int subjectId, int slotId) throws SQLException {
    String sql = "SELECT COUNT(*) FROM (" +
            "SELECT te.subject_id, COUNT(*) AS cnt " +
            "FROM timetable_entry te " +
            "JOIN slot s1 ON te.slot_id = s1.slot_id " +
            "JOIN slot s2 ON s2.slot_id = ? " +
            "WHERE te.class_id = ? AND s1.day = s2.day AND te.subject_id != ? " +
            "GROUP BY te.subject_id " +
            "HAVING cnt >= 2" +
            ") AS doubled";

    try (Connection conn = DBConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

        stmt.setInt(1, slotId);
        stmt.setInt(2, classId);
        stmt.setInt(3, subjectId);

        try (ResultSet rs = stmt.executeQuery()) {
            rs.next();
            return rs.getInt(1) > 0;
        }
    }
}

    private boolean countIsZero(String sql, int id, int slotId) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.setInt(2, slotId);

            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                return rs.getInt(1) == 0;
            }
        }
    }

    // ------------------------------------------------------------
    // BASIC CRUD
    // ------------------------------------------------------------

    /**
     * Inserts one booking row. Call this only AFTER all isXFree() checks
     * passed - this method does NOT re-check conflicts itself.
     */
    public void save(TimetableEntry entry) throws SQLException {
        String sql = "INSERT INTO timetable_entry " +
                "(slot_id, class_id, subject_id, teacher_id, room_id, batch_id, lab_session_group, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, entry.getSlot().getSlotId());
            stmt.setInt(2, entry.getClassGroup().getClassId());
            stmt.setInt(3, entry.getSubject().getSubjectId());
            stmt.setInt(4, entry.getTeacher().getTeacherId());
            stmt.setInt(5, entry.getRoom().getRoomId());

            if (entry.getBatch() != null) {
                stmt.setInt(6, entry.getBatch().getBatchId());
            } else {
                stmt.setNull(6, java.sql.Types.INTEGER);
            }

            if (entry.getLabSessionGroup() != null) {
                stmt.setInt(7, entry.getLabSessionGroup());
            } else {
                stmt.setNull(7, java.sql.Types.INTEGER);
            }

            stmt.setString(8, entry.getStatus().name());

            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    entry.setEntryId(keys.getInt(1));
                }
            }
        }
    }

    /**
     * Returns the full generated timetable for one class - what the
     * Admin sees after generation, and what feeds the class grid view.
     */
    public List<TimetableEntry> findByClassId(int classId) throws SQLException {
    String sql = "SELECT * FROM timetable_entry WHERE class_id = ?";
    List<TimetableEntry> entries = queryList(sql, classId);
    entries.sort(
        java.util.Comparator
            .comparing((TimetableEntry e) -> e.getSlot().getDay())
            .thenComparingInt(e -> e.getSlot().getPeriod())
    );
    return entries;
}

    /**
     * Returns everything a single teacher teaches across all classes -
     * this is exactly what the Teacher Portal (view own schedule) needs.
     */
    public List<TimetableEntry> findByTeacherId(int teacherId) throws SQLException {
        String sql = "SELECT * FROM timetable_entry WHERE teacher_id = ?";
        return queryList(sql, teacherId);
    }
public List<TimetableEntry> findAllExceptClass(int classId) throws SQLException {
    String sql = "SELECT * FROM timetable_entry WHERE class_id != ?";
    return queryList(sql, classId);
}
    private List<TimetableEntry> queryList(String sql, int id) throws SQLException {
        List<TimetableEntry> entries = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    entries.add(mapRow(rs));
                }
            }
        }
        return entries;
    }

    /**
     * Deletes a single booking - used when Admin manually clears one
     * slot before re-editing it.
     */
    public void delete(int entryId) throws SQLException {
        String sql = "DELETE FROM timetable_entry WHERE entry_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, entryId);
            stmt.executeUpdate();
        }
    }

    /**
     * Deletes every booking for one class - used when Admin wants to
     * regenerate a class's timetable from scratch.
     */
    public void deleteByClassId(int classId) throws SQLException {
        String sql = "DELETE FROM timetable_entry WHERE class_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, classId);
            stmt.executeUpdate();
        }
    }
    /**
 * Wipes the ENTIRE timetable_entry table - used only by
 * generateForAllClasses() for the all-or-nothing full regeneration.
 */
public void deleteAll() throws SQLException {
    String sql = "DELETE FROM timetable_entry";

    try (Connection conn = DBConnection.getConnection();
         Statement stmt = conn.createStatement()) {

        stmt.executeUpdate(sql);
    }
}

    /**
     * Marks an entry as substituted and swaps in the new teacher -
     * used by the Substitution Recommender once Admin picks a replacement.
     */
    public void substituteTeacher(int entryId, int newTeacherId) throws SQLException {
        String sql = "UPDATE timetable_entry SET teacher_id = ?, status = 'SUBSTITUTED' WHERE entry_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, newTeacherId);
            stmt.setInt(2, entryId);
            stmt.executeUpdate();
        }
    }

    /**
     * Builds a full TimetableEntry object with all nested objects
     * (Slot, ClassGroup, Subject, Teacher, Room, Batch) attached.
     * This means 5-6 extra queries per row - acceptable at this scale
     * (max 35 rows per class), not something to optimize for Phase 1.
     */
    private TimetableEntry mapRow(ResultSet rs) throws SQLException {
        TimetableEntry entry = new TimetableEntry();
        entry.setEntryId(rs.getInt("entry_id"));

        entry.setSlot(slotDAO.findById(rs.getInt("slot_id")));
        entry.setClassGroup(classGroupDAO.findById(rs.getInt("class_id")));
        entry.setSubject(subjectDAO.findById(rs.getInt("subject_id")));
        entry.setTeacher(teacherDAO.findById(rs.getInt("teacher_id")));
        entry.setRoom(roomDAO.findById(rs.getInt("room_id")));

        int batchId = rs.getInt("batch_id");
        if (!rs.wasNull()) {
            entry.setBatch(batchDAO.findById(batchId));
        }

        int labGroup = rs.getInt("lab_session_group");
        if (!rs.wasNull()) {
            entry.setLabSessionGroup(labGroup);
        }

        entry.setStatus(TimetableEntry.Status.valueOf(rs.getString("status")));

        return entry;
    }
   public java.util.Map<Slot.Day, Integer> countBookedSlotsByDayForClass(int classId) throws SQLException {
    java.util.Map<Slot.Day, Integer> result = new java.util.HashMap<>();
    String sql = "SELECT s.day, COUNT(*) AS cnt FROM timetable_entry te " +
                 "JOIN slot s ON te.slot_id = s.slot_id " +
                 "WHERE te.class_id = ? GROUP BY s.day";
    try (Connection conn = DBConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setInt(1, classId);
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Slot.Day day = Slot.Day.valueOf(rs.getString("day"));
                result.put(day, rs.getInt("cnt"));
            }
        }
    }
    return result;
}
}