package com.asiet.aids.timetable.model;

public class Teacher {
    private int teacherId;
    private String name;
    private String email;
    private String passwordHash;
    private int maxWeeklyHours;

    public Teacher() {
    }

    public Teacher(int teacherId, String name, String email, String passwordHash, int maxWeeklyHours) {
        this.teacherId = teacherId;
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.maxWeeklyHours = maxWeeklyHours;
    }

    public int getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(int teacherId) {
        this.teacherId = teacherId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public int getMaxWeeklyHours() {
        return maxWeeklyHours;
    }

    public void setMaxWeeklyHours(int maxWeeklyHours) {
        this.maxWeeklyHours = maxWeeklyHours;
    }

    @Override
    public String toString() {
        return name + " (" + email + ")";
    }
}