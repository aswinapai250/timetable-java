package com.asiet.aids.timetable.model;

/**
 * Mirrors the `room` table. A room is either a CLASSROOM (used as a
 * class's fixed home room for theory) or a LAB (shared, interchangeable).
 */
public class Room {

    public enum Type {
        CLASSROOM, LAB
    }

    private int roomId;
    private String roomName;
    private Type type;
    private int capacity;

    public Room() {
    }

    public Room(int roomId, String roomName, Type type, int capacity) {
        this.roomId = roomId;
        this.roomName = roomName;
        this.type = type;
        this.capacity = capacity;
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public boolean isLab() {
        return type == Type.LAB;
    }

    @Override
    public String toString() {
        return roomName;
    }
}
