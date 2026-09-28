# PRODUCT REQUIREMENTS DOCUMENT
## Automated Timetable Generator for Academic Institutions (AI & DS Department)

**Project Title:** Automated Timetable Generator (Teacher-Centric Focus)
**Target Stack:** Java (Swing GUI), MySQL via JDBC
**Phase Domain:** Academic Semester Implementation (Phase 1 — AI & DS Department Only)
**Author Context:** Engineering Team of Four (PBCST304 Course Project)

---

## 1. Introduction & Objectives

Manual timetable creation is a classical NP-hard scheduling problem prone to resource overlaps, room under-utilization, and faculty double-booking. This PRD defines a desktop Automated Timetable Generator, built in Java Swing with a MySQL backend via JDBC, scoped initially to the AI & DS department (8 classes: S1AI, S1DS, S3AI, S3DS, S5AI, S5DS, S7AI, S7DS), with architecture designed to scale college-wide in later phases.

The system supports two roles — **Admin** (data entry, generation, substitution management) and **Teacher** (view own schedule only) — and generates conflict-free timetables using a recursive backtracking constraint-satisfaction engine.

---

## 2. System Constraints & Boundary Values

- **Standard Operational Grid:** Monday–Friday, 7 periods/day.
- **Special Operational Grid:** Saturday, independent 7-period schedule.
- **Home Room:** Each class has a fixed, admin-predefined home room for all lecture (non-lab) sessions. Not auto-assigned by the system.
- **Lab Allocation:** Bypasses Home Room. 3 shared general-purpose labs (interchangeable — working assumption, pending confirmation once lab cycle is finalized), each with 30-seat capacity, shared across all AI & DS classes.
- **Batch Splitting for Labs:** Configurable per class by Admin during setup (not hardcoded). Example: S3AI splits into Batch 1 (Python Lab) and Batch 2 (DSA Lab), run simultaneously in 2 of the 3 labs.
- **Class Size:** Variable per class (`student_count` field) — not assumed to be a fixed capacity (e.g., AI classes ≤60, DS classes ≤30, but can be any actual value like 56 or 25).
- **Faculty Loading:** Consecutive slot loading allowed (e.g., up to 3 consecutive hours) without error.
- **Timetable Generation Scope:** One class generated at a time (not all 8 simultaneously). Each generated class's bookings (teacher/lab slots) are respected as fixed constraints when generating the next class.
- **Post-Generation Edits:** Admin can manually adjust individual slots after generation. All manual edits are validated against the same conflict rules (teacher/room/class clash) as auto-generation — no unvalidated free-for-all edits.

---

## 3. Core Data Entities & Schema Strategy

### 3.1 Admin Entity
- Username (unique)
- Password (hashed via SHA-256, no plaintext storage)

### 3.2 Teacher Entity
- Name
- Email Address (unique, contact/login identifier)
- Password (hashed via SHA-256)
- Subject Competencies (list of subject codes the teacher can teach)
- Weekly Operational Threshold (`H_max`) — max allowed teaching hours/week

### 3.3 Subject Entity
- Subject Code
- Subject Name
- Hour Requirement (periods/week)
- Type (Lecture / Lab)

### 3.4 Room Entity
- Room ID/Name
- Type (Standard Core / Laboratory)
- Capacity

### 3.5 Class Entity *(new — added to close a scheduling gap in the original draft)*
- Class Name (e.g., "S3AI")
- Student Count (actual enrolled count, variable)
- Home Room (fixed, admin-assigned during setup)
- Subject list with weekly hour requirements
- Batch Configuration (admin-defined): number of batches, subject-per-batch, lab-per-batch — used only for lab sessions

### 3.6 Timetable Slot Entity
- Day, Period
- Class, Teacher, Subject, Room
- Status flag (Regular / Substituted)

---

## 4. Functional Specifications

### 4.1 Admin Interface (Phase 1 Focus)
- Login (SHA-256 hashed credentials)
- Add/update/manage Teachers, Subjects, Rooms, Classes
- Configure batch-lab mapping per class
- Trigger timetable generation (one class at a time)
- Manually edit generated slots (with conflict validation)
- Mark a teacher absent for a given day

### 4.2 Automated Constraint Resolution Engine
Recursive backtracking model. For each candidate slot, the engine evaluates:

```
Constraint(T, R, C, D, P) = Free(T, D, P) × Free(R, D, P) × Free(C, D, P) × WithinQuota(T)
```

Where **T** = Teacher, **R** = Room, **C** = Class, **D** = Day, **P** = Period.
*(Note: `Free(C, D, P)` — the Class-availability check — was missing from the original draft and has been added; without it, two different subjects could be scheduled for the same class at the same time.)*

If any condition fails, a custom exception is thrown and the engine backtracks to the next valid cell.

### 4.3 Automated Substitution Recommender
- Admin marks a teacher absent for a specific day.
- System auto-scans and displays a list of valid substitute candidates who are:
  1. Free during that Day/Period,
  2. Competent in the required subject,
  3. Within their own weekly hour threshold (`H_max`) if added.
- Admin manually selects the substitute from the suggested list (system does not auto-assign).
- Once selected, that day's timetable record is permanently updated in the database (status: Substituted).

### 4.4 Teacher View & Authentication Portal
- Teacher logs in with their own credentials (SHA-256 hashed).
- Teachers see **only their own** assigned periods — no visibility into other teachers' schedules or free/unassigned rooms.
- **Day View:** 7 periods for a selected day.
- **Week View:** Full Monday–Saturday schedule.

---

## 5. Technical & Non-Functional Architecture

| Attribute | Requirement |
|---|---|
| Framework Stack | Java SE, Swing GUI; local MySQL instance |
| Data Linkage | JDBC with prepared statements (prevents SQL injection) |
| Security | SHA-256 password hashing (Java `MessageDigest`, no external libraries) |
| Exception Pattern | Custom exception classes for scheduling conflicts (e.g., `SlotClashException`) |
| Data Presentation | Java Swing grid components for multi-dimensional timetable views |
| Roles | Admin (full access), Teacher (own schedule, read-only) |

---

## 6. Execution & Implementation Milestones

1. **Milestone 1 — Database Construction:** Build relational schema (Admin, Teacher, Subject, Room, Class, Timetable Slot); establish JDBC connection layer.
2. **Milestone 2 — Engine Formulation:** Implement recursive backtracking search and 4-part constraint check (Teacher, Room, Class, Quota).
3. **Milestone 3 — GUI Implementation:** Build Admin data-entry windows, Teacher login/view portal, Day/Week grid layouts.
4. **Milestone 4 — Substitution & Validation:** Implement substitute-scanning logic, manual slot-edit validation, and finalize authentication security.

---

## 7. Open Assumptions (to confirm later)

- Labs are treated as interchangeable/general-purpose across all classes — pending confirmation once your lab cycle is fully underway.
- Batch-lab configuration pattern (2 batches, simultaneous, different labs) is confirmed for S3AI only; other classes' patterns to be entered independently by Admin as they're onboarded.
