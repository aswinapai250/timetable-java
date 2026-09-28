package com.asiet.aids.timetable.model;

/**
 * Mirrors the `teacher_subject_class` table. This is FIXED INPUT data -
 * Admin enters it (or imports it via Excel per data.md), the engine
 * never invents or changes it. It only decides the WHEN (slot), not the
 * WHO (this table already answers that).
 */
public class TeacherSubjectClass {
    private int tscId;
    private Teacher teacher;
    private Subject subject;
    private ClassGroup classGroup;

    public TeacherSubjectClass() {
    }

    public TeacherSubjectClass(int tscId, Teacher teacher, Subject subject, ClassGroup classGroup) {
        this.tscId = tscId;
        this.teacher = teacher;
        this.subject = subject;
        this.classGroup = classGroup;
    }

    public int getTscId() {
        return tscId;
    }

    public void setTscId(int tscId) {
        this.tscId = tscId;
    }

    public Teacher getTeacher() {
        return teacher;
    }

    public void setTeacher(Teacher teacher) {
        this.teacher = teacher;
    }

    public Subject getSubject() {
        return subject;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    public ClassGroup getClassGroup() {
        return classGroup;
    }

    public void setClassGroup(ClassGroup classGroup) {
        this.classGroup = classGroup;
    }

    @Override
    public String toString() {
        return classGroup.getClassName() + " - " + subject.getSubjectCode() + " - " + teacher.getName();
    }
}
