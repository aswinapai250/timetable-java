package com.asiet.aids.timetable.exception;

/**
 * Thrown by the backtracking engine when a subject's required weekly
 * sessions cannot be placed anywhere in the 35 available slots without
 * violating a constraint (teacher/room/class clash or quota).
 *
 * Phase 1 behaviour: this is NOT caught-and-retried internally. It
 * propagates up and aborts the whole generation for that class, so
 * Admin knows exactly which subject failed and why.
 */
public class SchedulingException extends Exception {

    public SchedulingException(String message) {
        super(message);
    }

    public SchedulingException(String message, Throwable cause) {
        super(message, cause);
    }
}
