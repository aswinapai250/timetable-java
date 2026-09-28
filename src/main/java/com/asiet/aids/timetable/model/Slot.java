package com.asiet.aids.timetable.model;

/**
 * Mirrors the `slot` table. There are exactly 35 rows (5 days x 7 periods),
 * seeded once by schema.sql and never modified.
 */
public class Slot {

    public enum Day {
        MON, TUE, WED, THU, FRI
    }

    private int slotId;
    private Day day;
    private int period;   // 1 to 7

    public Slot() {
    }

    public Slot(int slotId, Day day, int period) {
        this.slotId = slotId;
        this.day = day;
        this.period = period;
    }

    public int getSlotId() {
        return slotId;
    }

    public void setSlotId(int slotId) {
        this.slotId = slotId;
    }

    public Day getDay() {
        return day;
    }

    public void setDay(Day day) {
        this.day = day;
    }

    public int getPeriod() {
        return period;
    }

    public void setPeriod(int period) {
        this.period = period;
    }

    @Override
    public String toString() {
        return day + " P" + period;
    }
}
