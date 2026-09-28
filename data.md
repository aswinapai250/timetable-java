# Data Collection Requirements
## Automated Teacher-Centric Timetable Generator
**Version:** Phase 1 (AI & DS Department)

---

# 1. Data to Collect from HOD (Initial Setup)

The HOD is responsible for providing all academic and scheduling data before timetable generation.

---

## 1.1 Teacher Details

| Field | Description |
|--------|-------------|
| Teacher ID | Unique identifier |
| Teacher Name | Full name |
| Email | Login ID |
| Password | Hashed password |
| Department | AI & DS |
| Designation | Assistant Professor / Associate Professor / Professor (Optional) |

---

## 1.2 Subject Details

| Field | Description |
|--------|-------------|
| Subject Code | University subject code |
| Subject Name | Subject title |
| Subject Type | Theory / Lab |
| Weekly Hours | Required periods per week |
| Session Length | 1 (Theory), 3 (Lab) |

Example

| Subject | Type | Weekly Hours | Session Length |
|----------|------|--------------|----------------|
| DSA | Theory | 4 | 1 |
| Java Lab | Lab | 3 | 3 |

---

## 1.3 Teacher Assignment Details

This defines which teacher teaches which subject for which class.

| Field | Description |
|--------|-------------|
| Teacher ID |
| Class |
| Subject |
| Session Type (Theory/Lab) |

Example

| Teacher | Class | Subject | Type |
|----------|-------|----------|------|
| Reshmi | S3AI | DSA | Theory |
| Arun | S3AI | Python Lab | Lab |
| Reshmi | S5AI | DBMS | Theory |

---

## 1.4 Class Details

| Field | Description |
|--------|-------------|
| Class Name |
| Student Strength |
| Home Classroom |

Example

| Class | Students | Room |
|-------|----------|------|
| S3AI | 59 | C304 |

---

## 1.5 Classroom Details

| Field | Description |
|--------|-------------|
| Room Name |
| Room Type (Classroom/Lab) |
| Capacity |

Example

| Room | Type | Capacity |
|------|------|----------|
| C304 | Classroom | 60 |
| Lab 1 | Laboratory | 30 |

---

## 1.6 Laboratory Details

| Field | Description |
|--------|-------------|
| Lab Name |
| Capacity |

Example

| Lab | Capacity |
|-----|----------|
| Lab 1 | 30 |
| Lab 2 | 30 |
| Lab 3 | 30 |

---

## 1.7 Batch Configuration

Required for laboratory sessions.

| Field | Description |
|--------|-------------|
| Class |
| Number of Batches |
| Batch Names |
| Students per Batch |

Example

| Class | Batch | Students |
|-------|-------|----------|
| S3AI | A | 30 |
| S3AI | B | 29 |

---

## 1.8 Lab Rotation Details

Defines which batch attends which lab on each lab day.

Example

### Tuesday

| Batch | Subject |
|-------|----------|
| A | DSA Lab |
| B | Python Lab |

### Friday

| Batch | Subject |
|-------|----------|
| A | Python Lab |
| B | DSA Lab |

---

## 1.9 Academic Calendar

| Field | Description |
|--------|-------------|
| Semester Start |
| Semester End |
| Public Holidays |
| Working Saturdays |
| Holiday Saturdays |

Default Rule

- 2nd Saturday → Holiday
- 4th Saturday → Holiday
- Others configurable

---

## 1.10 Period Timing

### Monday – Thursday

| Period | Time |
|---------|------|
| H1 | 08:50 – 09:50 |
| H2 | 09:50 – 10:50 |
| Break | 10 mins |
| H3 | 11:00 – 11:50 |
| H4 | 11:50 – 12:40 |
| Lunch | 12:40 – 01:30 |
| H5 | 01:30 – 02:20 |
| H6 | 02:20 – 03:10 |
| H7 | 03:10 – 04:00 |

### Friday

| Period | Time |
|---------|------|
| H1 | 08:50 – 09:50 |
| H2 | 09:50 – 10:50 |
| Break | 10 mins |
| H3 | 11:00 – 11:50 |
| H4 | 11:50 – 12:40 |
| Lunch | 12:40 – 02:00 |
| H5 | 02:00 – 02:45 |
| H6 | 02:45 – 03:30 |
| H7 | 03:30 – 04:15 |

---

# 2. Data to Collect from Teachers

Teachers only need to provide information that affects scheduling.

---

## 2.1 Leave Information

| Field | Description |
|--------|-------------|
| Teacher |
| Leave Date |
| Full Day / Half Day |

---

## 2.2 Substitute Competencies (Recommended)

Subjects the teacher can handle during substitutions.

Example

| Teacher | Can Handle |
|----------|------------|
| Reshmi | DSA, DBMS |
| Arun | Python Lab, Java Lab |
| Sabitha | OOP, TOC |

---

# 3. Data to Collect from Principal / HOD

Institution-wide scheduling policies.

| Field | Description |
|--------|-------------|
| Maximum teaching periods/day (Configurable) |
| Maximum consecutive periods |
| Teacher workload balancing preference |
| Working Saturdays |
| Semester dates |

---

# 4. Data Generated Automatically by the System

The following information is produced by the timetable generator.

- Teacher Timetable
- Class Timetable (Internal constraint only)
- Lab Allocation
- Classroom Allocation
- Teacher Workload Report
- Teacher Free Period Report
- Substitution Records
- Timetable Version History
- Conflict Reports

---

# 5. Excel Workbook Structure

The HOD imports **one Excel workbook** containing the following sheets:

```
Teachers
Subjects
TeacherAssignments
Classes
Rooms
Labs
LabBatches
LabRotation
AcademicCalendar
PeriodTimings
```

---

# Data NOT Required

The system does **not** require:

- Student names
- Student login accounts
- Attendance records
- Marks
- Parent details
- Student timetable preferences
- Individual student records

---

# Notes

- The system is **teacher-centric**, not student-centric.
- Teacher comfort and balanced workload are optimization goals.
- Classes, rooms and laboratories act as scheduling constraints.
- The generated timetable is reviewed and approved by the HOD.
- Approved timetables are stored in MySQL and remain active until modified or regenerated.
- Any manual edits made by the HOD are validated against teacher, room, lab and class conflict rules before being saved.