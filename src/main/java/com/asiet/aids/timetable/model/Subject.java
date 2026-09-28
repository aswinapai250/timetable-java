package com.asiet.aids.timetable.model;

/**
 * Mirrors the `subject` table.
 * sessionLength: 1 for THEORY, 3 for LAB (consecutive periods booked atomically).
 */
public class Subject {

    public enum Type {
        THEORY, LAB
    }

    private int subjectId;
    private String subjectCode;
    private String subjectName;
    private Type type;
    private int weeklyHours;
    private int sessionLength;

    public Subject() {
    }

    public Subject(int subjectId, String subjectCode, String subjectName,
                    Type type, int weeklyHours, int sessionLength) {
        this.subjectId = subjectId;
        this.subjectCode = subjectCode;
        this.subjectName = subjectName;
        this.type = type;
        this.weeklyHours = weeklyHours;
        this.sessionLength = sessionLength;
    }

    public int getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(int subjectId) {
        this.subjectId = subjectId;
    }

    public String getSubjectCode() {
        return subjectCode;
    }

    public void setSubjectCode(String subjectCode) {
        this.subjectCode = subjectCode;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public int getWeeklyHours() {
        return weeklyHours;
    }

    public void setWeeklyHours(int weeklyHours) {
        this.weeklyHours = weeklyHours;
    }

    public int getSessionLength() {
        return sessionLength;
    }

    public void setSessionLength(int sessionLength) {
        this.sessionLength = sessionLength;
    }

    public boolean isLab() {
        return type == Type.LAB;
    }

    @Override
    public String toString() {
        return subjectCode + " - " + subjectName;
    }
}
