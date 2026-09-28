package com.asiet.aids.timetable.dao;

import com.asiet.aids.timetable.model.Slot;
import com.asiet.aids.timetable.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles all JDBC operations for the `slot` table.
 *
 * Unlike the other DAOs, there is no save() here on purpose - the 35 rows
 * (5 days x 7 periods) were already seeded once by schema.sql and are
 * never meant to change. This class is read-only by design.
 */
public class SlotDAO {

    public List<Slot> findAll() throws SQLException {
        String sql = "SELECT * FROM slot ORDER BY FIELD(day,'MON','TUE','WED','THU','FRI'), period";
        List<Slot> slots = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                slots.add(mapRow(rs));
            }
        }
        return slots;
    }

    public Slot findById(int slotId) throws SQLException {
        String sql = "SELECT * FROM slot WHERE slot_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, slotId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    /**
     * Used constantly by the backtracking engine: given a day and a
     * starting period, find that exact slot to check/book it.
     */
    public Slot findByDayAndPeriod(Slot.Day day, int period) throws SQLException {
        String sql = "SELECT * FROM slot WHERE day = ? AND period = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, day.name());
            stmt.setInt(2, period);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    /**
     * Returns all 7 slots for a single day, in period order. Handy for
     * rendering one day's row in the Swing grid.
     */
    public List<Slot> findByDay(Slot.Day day) throws SQLException {
        String sql = "SELECT * FROM slot WHERE day = ? ORDER BY period";
        List<Slot> slots = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, day.name());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    slots.add(mapRow(rs));
                }
            }
        }
        return slots;
    }

    private Slot mapRow(ResultSet rs) throws SQLException {
        Slot slot = new Slot();
        slot.setSlotId(rs.getInt("slot_id"));
        slot.setDay(Slot.Day.valueOf(rs.getString("day")));
        slot.setPeriod(rs.getInt("period"));
        return slot;
    }
}
