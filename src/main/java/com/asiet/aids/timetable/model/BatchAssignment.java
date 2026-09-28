package com.asiet.aids.timetable.model;

/**
 * Mirrors the `batch_assignment` table. Fixed input data (Phase 1: static,
 * no auto-rotation) - tells the engine which subject + teacher a specific
 * batch (e.g. S3AI-BatchA) is doing for its lab session.
 */
public class BatchAssignment {
    private int batchAssignmentId;
    private Batch batch;
    private Subject subject;
    private Teacher teacher;

    public BatchAssignment() {
    }

    public BatchAssignment(int batchAssignmentId, Batch batch, Subject subject, Teacher teacher) {
        this.batchAssignmentId = batchAssignmentId;
        this.batch = batch;
        this.subject = subject;
        this.teacher = teacher;
    }

    public int getBatchAssignmentId() {
        return batchAssignmentId;
    }

    public void setBatchAssignmentId(int batchAssignmentId) {
        this.batchAssignmentId = batchAssignmentId;
    }

    public Batch getBatch() {
        return batch;
    }

    public void setBatch(Batch batch) {
        this.batch = batch;
    }

    public Subject getSubject() {
        return subject;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    public Teacher getTeacher() {
        return teacher;
    }

    public void setTeacher(Teacher teacher) {
        this.teacher = teacher;
    }

    @Override
    public String toString() {
        return batch.toString() + " - " + subject.getSubjectCode() + " - " + teacher.getName();
    }
}
