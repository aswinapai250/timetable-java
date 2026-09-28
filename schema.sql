-- ============================================================
-- Automatic Timetable Generator - Phase 1 Schema
-- AI & DS Department, ASIET
-- Run this once in MySQL Workbench before starting Main.java
-- ============================================================

CREATE DATABASE IF NOT EXISTS timetable_db;
USE timetable_db;

-- ------------------------------------------------------------
-- 1. ADMIN
-- ------------------------------------------------------------
CREATE TABLE admin (
    admin_id     INT AUTO_INCREMENT PRIMARY KEY,
    username     VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(64) NOT NULL   -- SHA-256 hex string
);

-- ------------------------------------------------------------
-- 2. TEACHER
-- ------------------------------------------------------------
CREATE TABLE teacher (
    teacher_id      INT AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(100) NOT NULL,
    email           VARCHAR(100) NOT NULL UNIQUE,
    password_hash   VARCHAR(64) NOT NULL,
    max_weekly_hours INT NOT NULL DEFAULT 20   -- H_max
);

-- ------------------------------------------------------------
-- 3. SUBJECT
-- ------------------------------------------------------------
CREATE TABLE subject (
    subject_id      INT AUTO_INCREMENT PRIMARY KEY,
    subject_code    VARCHAR(20) NOT NULL UNIQUE,
    subject_name    VARCHAR(100) NOT NULL,
    subject_type    ENUM('THEORY', 'LAB') NOT NULL,
    weekly_hours    INT NOT NULL,          -- periods/week required
    session_length  INT NOT NULL DEFAULT 1 -- 1 = theory, 3 = lab (consecutive periods)
);

-- ------------------------------------------------------------
-- 4. ROOM  (classrooms AND labs live here, differentiated by type)
-- ------------------------------------------------------------
CREATE TABLE room (
    room_id     INT AUTO_INCREMENT PRIMARY KEY,
    room_name   VARCHAR(50) NOT NULL UNIQUE,
    room_type   ENUM('CLASSROOM', 'LAB') NOT NULL,
    capacity    INT NOT NULL
);

-- ------------------------------------------------------------
-- 5. CLASS  (e.g. S3AI, S3DS ...)
-- ------------------------------------------------------------
CREATE TABLE class (
    class_id        INT AUTO_INCREMENT PRIMARY KEY,
    class_name      VARCHAR(20) NOT NULL UNIQUE,
    student_count   INT NOT NULL,
    home_room_id    INT NOT NULL,
    FOREIGN KEY (home_room_id) REFERENCES room(room_id)
);

-- ------------------------------------------------------------
-- 6. TEACHER_SUBJECT_CLASS
-- Fixed assignment data imported/entered by Admin: who teaches
-- what subject to which class. This is INPUT, not generated.
-- ------------------------------------------------------------
CREATE TABLE teacher_subject_class (
    tsc_id      INT AUTO_INCREMENT PRIMARY KEY,
    teacher_id  INT NOT NULL,
    subject_id  INT NOT NULL,
    class_id    INT NOT NULL,
    FOREIGN KEY (teacher_id) REFERENCES teacher(teacher_id),
    FOREIGN KEY (subject_id) REFERENCES subject(subject_id),
    FOREIGN KEY (class_id)   REFERENCES class(class_id),
    UNIQUE KEY uq_subject_per_class (subject_id, class_id) -- one teacher per subject per class
);

-- ------------------------------------------------------------
-- 7. BATCH
-- Only relevant for lab sessions where class_size > lab capacity.
-- Static/manual for Phase 1 (Admin assigns lab per batch directly).
-- ------------------------------------------------------------
CREATE TABLE batch (
    batch_id    INT AUTO_INCREMENT PRIMARY KEY,
    class_id    INT NOT NULL,
    batch_name  VARCHAR(10) NOT NULL,   -- e.g. 'A', 'B'
    student_count INT NOT NULL,
    FOREIGN KEY (class_id) REFERENCES class(class_id),
    UNIQUE KEY uq_batch_per_class (class_id, batch_name)
);

-- Which subject (lab) a batch is assigned to, and which teacher/room.
-- Phase 1: static, admin-entered, no auto-rotation.
CREATE TABLE batch_assignment (
    batch_assignment_id INT AUTO_INCREMENT PRIMARY KEY,
    batch_id    INT NOT NULL,
    subject_id  INT NOT NULL,
    teacher_id  INT NOT NULL,
    FOREIGN KEY (batch_id) REFERENCES batch(batch_id),
    FOREIGN KEY (subject_id) REFERENCES subject(subject_id),
    FOREIGN KEY (teacher_id) REFERENCES teacher(teacher_id)
);

-- ------------------------------------------------------------
-- 8. SLOT  (35 fixed rows: 5 days x 7 periods, seeded below)
-- ------------------------------------------------------------
CREATE TABLE slot (
    slot_id INT AUTO_INCREMENT PRIMARY KEY,
    day     ENUM('MON','TUE','WED','THU','FRI') NOT NULL,
    period  INT NOT NULL,   -- 1 to 7
    UNIQUE KEY uq_day_period (day, period)
);

-- ------------------------------------------------------------
-- 9. TIMETABLE_ENTRY
-- The actual generated (or manually edited) bookings.
-- For a lab (session_length = 3), 3 rows are inserted, one per
-- period, all sharing the same lab_session_group value so the
-- UI/engine can treat them as one atomic block.
-- batch_id is NULL for theory sessions (whole class, not split).
-- ------------------------------------------------------------
CREATE TABLE timetable_entry (
    entry_id            INT AUTO_INCREMENT PRIMARY KEY,
    slot_id             INT NOT NULL,
    class_id            INT NOT NULL,
    subject_id          INT NOT NULL,
    teacher_id          INT NOT NULL,
    room_id             INT NOT NULL,
    batch_id            INT NULL,
    lab_session_group   INT NULL,   -- groups the 3 rows of one lab session together
    status              ENUM('REGULAR','SUBSTITUTED') NOT NULL DEFAULT 'REGULAR',
    FOREIGN KEY (slot_id)    REFERENCES slot(slot_id),
    FOREIGN KEY (class_id)   REFERENCES class(class_id),
    FOREIGN KEY (subject_id) REFERENCES subject(subject_id),
    FOREIGN KEY (teacher_id) REFERENCES teacher(teacher_id),
    FOREIGN KEY (room_id)    REFERENCES room(room_id),
    FOREIGN KEY (batch_id)   REFERENCES batch(batch_id)
);

-- ------------------------------------------------------------
-- Seed the 35 fixed slots (Mon-Fri x 7 periods)
-- ------------------------------------------------------------
INSERT INTO slot (day, period) VALUES
('MON',1),('MON',2),('MON',3),('MON',4),('MON',5),('MON',6),('MON',7),
('TUE',1),('TUE',2),('TUE',3),('TUE',4),('TUE',5),('TUE',6),('TUE',7),
('WED',1),('WED',2),('WED',3),('WED',4),('WED',5),('WED',6),('WED',7),
('THU',1),('THU',2),('THU',3),('THU',4),('THU',5),('THU',6),('THU',7),
('FRI',1),('FRI',2),('FRI',3),('FRI',4),('FRI',5),('FRI',6),('FRI',7);
