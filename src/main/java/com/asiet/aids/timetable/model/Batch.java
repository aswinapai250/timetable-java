package com.asiet.aids.timetable.model;

/**
 * Mirrors the `batch` table. Only exists for classes whose student count
 * exceeds a lab's capacity (e.g. AI classes splitting into Batch A/B).
 */
public class Batch {
    private int batchId;
    private ClassGroup classGroup;
    private String batchName;      // "A", "B"
    private int studentCount;

    public Batch() {
    }

    public Batch(int batchId, ClassGroup classGroup, String batchName, int studentCount) {
        this.batchId = batchId;
        this.classGroup = classGroup;
        this.batchName = batchName;
        this.studentCount = studentCount;
    }

    public int getBatchId() {
        return batchId;
    }

    public void setBatchId(int batchId) {
        this.batchId = batchId;
    }

    public ClassGroup getClassGroup() {
        return classGroup;
    }

    public void setClassGroup(ClassGroup classGroup) {
        this.classGroup = classGroup;
    }

    public String getBatchName() {
        return batchName;
    }

    public void setBatchName(String batchName) {
        this.batchName = batchName;
    }

    public int getStudentCount() {
        return studentCount;
    }

    public void setStudentCount(int studentCount) {
        this.studentCount = studentCount;
    }

    @Override
    public String toString() {
        return classGroup.getClassName() + "-" + batchName;
    }
}
