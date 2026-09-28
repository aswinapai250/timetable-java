package com.asiet.aids.timetable.model;

/**
 * Mirrors the `timetable_entry` table - a single booking of
 * (slot, class, subject, teacher, room), optionally tied to a batch.
 *
 * For a LAB subject (sessionLength = 3), the engine creates 3 of these
 * (one per consecutive period) sharing the same labSessionGroup value,
 * so they can be treated as one atomic block when displaying or editing.
 */
public class TimetableEntry {

    public enum Status {
        REGULAR, SUBSTITUTED
    }

    private int entryId;
    private Slot slot;
    private ClassGroup classGroup;
    private Subject subject;
    private Teacher teacher;
    private Room room;
    private Batch batch;              // null for theory (whole-class) sessions
    private Integer labSessionGroup;  // null for theory; shared id for the 3 lab rows
    private Status status = Status.REGULAR;

    public TimetableEntry() {
    }

    public TimetableEntry(Slot slot, ClassGroup classGroup, Subject subject,
                           Teacher teacher, Room room, Batch batch, Integer labSessionGroup) {
        this.slot = slot;
        this.classGroup = classGroup;
        this.subject = subject;
        this.teacher = teacher;
        this.room = room;
        this.batch = batch;
        this.labSessionGroup = labSessionGroup;
    }

    public int getEntryId() {
        return entryId;
    }

    public void setEntryId(int entryId) {
        this.entryId = entryId;
    }

    public Slot getSlot() {
        return slot;
    }

    public void setSlot(Slot slot) {
        this.slot = slot;
    }

    public ClassGroup getClassGroup() {
        return classGroup;
    }

    public void setClassGroup(ClassGroup classGroup) {
        this.classGroup = classGroup;
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

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public Batch getBatch() {
        return batch;
    }

    public void setBatch(Batch batch) {
        this.batch = batch;
    }

    public Integer getLabSessionGroup() {
        return labSessionGroup;
    }

    public void setLabSessionGroup(Integer labSessionGroup) {
        this.labSessionGroup = labSessionGroup;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    @Override
    public String toString() {
        String who = (batch != null) ? batch.toString() : classGroup.getClassName();
        return slot + " | " + who + " | " + subject.getSubjectCode()
                + " | " + teacher.getName() + " | " + room.getRoomName();
    }
}
