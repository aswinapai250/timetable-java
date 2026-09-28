package com.asiet.aids.timetable.model;

/**
 * Mirrors the `class` table. Named ClassGroup instead of "Class" because
 * "Class" is a reserved type name in java.lang and would shadow it.
 */
public class ClassGroup {
    private int classId;
    private String className;      // e.g. "S3AI"
    private int studentCount;
    private Room homeRoom;

    public ClassGroup() {
    }

    public ClassGroup(int classId, String className, int studentCount, Room homeRoom) {
        this.classId = classId;
        this.className = className;
        this.studentCount = studentCount;
        this.homeRoom = homeRoom;
    }

    public int getClassId() {
        return classId;
    }

    public void setClassId(int classId) {
        this.classId = classId;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public int getStudentCount() {
        return studentCount;
    }

    public void setStudentCount(int studentCount) {
        this.studentCount = studentCount;
    }

    public Room getHomeRoom() {
        return homeRoom;
    }

    public void setHomeRoom(Room homeRoom) {
        this.homeRoom = homeRoom;
    }

    /**
     * Phase 1 rule: a class needs lab batches only if its student count
     * exceeds the given lab's capacity.
     */
    public boolean needsBatchSplit(int labCapacity) {
        return studentCount > labCapacity;
    }

    @Override
    public String toString() {
        return className;
    }
}
