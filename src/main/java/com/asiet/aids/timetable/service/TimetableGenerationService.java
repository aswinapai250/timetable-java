package com.asiet.aids.timetable.service;

import com.asiet.aids.timetable.dao.*;
import com.asiet.aids.timetable.exception.SchedulingException;
import com.asiet.aids.timetable.model.*;

import java.sql.SQLException;
import java.util.*;

public class TimetableGenerationService {

    private final SlotDAO slotDAO = new SlotDAO();
    private final RoomDAO roomDAO = new RoomDAO();
    private final BatchDAO batchDAO = new BatchDAO();
    private final TeacherSubjectClassDAO tscDAO = new TeacherSubjectClassDAO();
    private final BatchAssignmentDAO batchAssignmentDAO = new BatchAssignmentDAO();
    private final TimetableEntryDAO entryDAO = new TimetableEntryDAO();

    private int labSessionCounter = 0;

    public void generateForClass(ClassGroup classGroup) throws SQLException, SchedulingException {
        entryDAO.deleteByClassId(classGroup.getClassId());
        generateForClassInternal(classGroup);
    }

    public void generateForAllClasses(List<ClassGroup> allClasses) throws SQLException, SchedulingException {
        entryDAO.deleteAll();
        labSessionCounter = 0;
        for (ClassGroup classGroup : allClasses) {
            generateForClassInternal(classGroup);
        }
    }

    private void generateForClassInternal(ClassGroup classGroup) throws SQLException, SchedulingException {
        List<Slot> allSlots = slotDAO.findAll();
        List<TeacherSubjectClass> assignments = tscDAO.findByClassId(classGroup.getClassId());
        List<Room> allLabs = roomDAO.findByType(Room.Type.LAB);

        List<SessionUnit> tasks = buildTasks(classGroup, assignments, allLabs);
        tasks.sort(Comparator.comparing(t -> t.isLab ? 0 : 1));

        List<TimetableEntry> placed = new ArrayList<>(entryDAO.findAllExceptClass(classGroup.getClassId()));
        int lockedCount = placed.size();

        boolean success = backtrack(tasks, 0, placed, allSlots, allLabs, classGroup);

        if (!success) {
            throw new SchedulingException(
                    "Could not generate a complete timetable for " + classGroup.getClassName() +
                    " - no arrangement satisfies all constraints, even with backtracking."
            );
        }

        for (int i = lockedCount; i < placed.size(); i++) {
            entryDAO.save(placed.get(i));
        }
    }

    private static class SessionUnit {
        Subject subject;
        Teacher teacher;
        Batch batch;
        boolean isLab;
        int blockLength;
    }

    private List<SessionUnit> buildTasks(ClassGroup classGroup, List<TeacherSubjectClass> assignments,
                                          List<Room> allLabs) throws SQLException {
        List<SessionUnit> tasks = new ArrayList<>();

        for (TeacherSubjectClass assignment : assignments) {
            Subject subject = assignment.getSubject();

            if (!subject.isLab()) {
                int sessionsNeeded = subject.getWeeklyHours();
                for (int i = 0; i < sessionsNeeded; i++) {
                    SessionUnit unit = new SessionUnit();
                    unit.subject = subject;
                    unit.teacher = assignment.getTeacher();
                    unit.isLab = false;
                    unit.blockLength = 1;
                    tasks.add(unit);
                }
            } else {
                int sessionLength = subject.getSessionLength();
                int sessionsNeeded = subject.getWeeklyHours() / sessionLength;

                boolean splitIntoBatches = !allLabs.isEmpty()
                        && classGroup.needsBatchSplit(allLabs.get(0).getCapacity());

                if (splitIntoBatches) {
                    List<Batch> batches = batchDAO.findByClassId(classGroup.getClassId());
                    for (Batch batch : batches) {
                        List<BatchAssignment> batchAssignments = batchAssignmentDAO.findByBatchId(batch.getBatchId());
                        for (BatchAssignment ba : batchAssignments) {
                            if (ba.getSubject().getSubjectId() != subject.getSubjectId()) continue;
                            for (int i = 0; i < sessionsNeeded; i++) {
                                SessionUnit unit = new SessionUnit();
                                unit.subject = ba.getSubject();
                                unit.teacher = ba.getTeacher();
                                unit.batch = batch;
                                unit.isLab = true;
                                unit.blockLength = sessionLength;
                                tasks.add(unit);
                            }
                        }
                    }
                } else {
                    for (int i = 0; i < sessionsNeeded; i++) {
                        SessionUnit unit = new SessionUnit();
                        unit.subject = subject;
                        unit.teacher = assignment.getTeacher();
                        unit.isLab = true;
                        unit.blockLength = sessionLength;
                        tasks.add(unit);
                    }
                }
            }
        }
        return tasks;
    }

    private boolean backtrack(List<SessionUnit> tasks, int index, List<TimetableEntry> placed,
                               List<Slot> allSlots, List<Room> allLabs, ClassGroup classGroup) {

        if (index == tasks.size()) return true;

        SessionUnit task = tasks.get(index);

        if (!task.isLab) {
            List<Slot> orderedSlots = orderSlotsByBusyness(allSlots, placed, classGroup);
            for (Slot slot : orderedSlots) {
                if (canPlaceTheory(placed, classGroup, task, slot)) {
                    TimetableEntry entry = new TimetableEntry(
                            slot, classGroup, task.subject, task.teacher, classGroup.getHomeRoom(), null, null
                    );
                    placed.add(entry);
                    if (backtrack(tasks, index + 1, placed, allSlots, allLabs, classGroup)) return true;
                    placed.remove(placed.size() - 1);
                }
            }
            return false;
        } else {
            for (int startIndex = 0; startIndex <= allSlots.size() - task.blockLength; startIndex++) {
                List<Slot> block = getConsecutiveBlockOnSameDay(allSlots, startIndex, task.blockLength);
                if (block == null) continue;

                for (Room lab : allLabs) {
                    if (canPlaceLabBlock(placed, classGroup, task, lab, block)) {
                        int sessionGroup = labSessionCounter++;
                        List<TimetableEntry> blockEntries = new ArrayList<>();
                        for (Slot slot : block) {
                            blockEntries.add(new TimetableEntry(
                                    slot, classGroup, task.subject, task.teacher, lab, task.batch, sessionGroup
                            ));
                        }
                        placed.addAll(blockEntries);

                        if (backtrack(tasks, index + 1, placed, allSlots, allLabs, classGroup)) return true;

                        for (int k = 0; k < blockEntries.size(); k++) {
                            placed.remove(placed.size() - 1);
                        }
                        labSessionCounter--;
                    }
                }
            }
            return false;
        }
    }

    private List<Slot> orderSlotsByBusyness(List<Slot> allSlots, List<TimetableEntry> placed, ClassGroup classGroup) {
        Map<Slot.Day, Integer> dayBusyness = new HashMap<>();
        for (TimetableEntry e : placed) {
            if (e.getClassGroup().getClassId() == classGroup.getClassId()) {
                dayBusyness.merge(e.getSlot().getDay(), 1, Integer::sum);
            }
        }
        List<Slot> ordered = new ArrayList<>(allSlots);
        Collections.shuffle(ordered);
        ordered.sort(Comparator.comparingInt(s -> dayBusyness.getOrDefault(s.getDay(), 0)));
        return ordered;
    }

    private boolean canPlaceTheory(List<TimetableEntry> placed, ClassGroup classGroup, SessionUnit task, Slot slot) {
        int classId = classGroup.getClassId();
        int teacherId = task.teacher.getTeacherId();
        int roomId = classGroup.getHomeRoom().getRoomId();
        int subjectId = task.subject.getSubjectId();

        int teacherWeeklyCount = 0;
        Map<Integer, Integer> sessionsPerSubjectToday = new HashMap<>();

        for (TimetableEntry e : placed) {
            if (e.getSlot().getSlotId() == slot.getSlotId()) {
                if (e.getTeacher().getTeacherId() == teacherId) return false;
                if (e.getRoom().getRoomId() == roomId) return false;
                if (e.getClassGroup().getClassId() == classId) return false;
            }
            if (e.getTeacher().getTeacherId() == teacherId) teacherWeeklyCount++;

            if (e.getClassGroup().getClassId() == classId && e.getSlot().getDay() == slot.getDay()) {
                sessionsPerSubjectToday.merge(e.getSubject().getSubjectId(), 1, Integer::sum);
            }
        }

        if (teacherWeeklyCount >= task.teacher.getMaxWeeklyHours()) return false;

        int thisSubjectToday = sessionsPerSubjectToday.getOrDefault(subjectId, 0);
        if (thisSubjectToday >= 2) return false;

        if (thisSubjectToday == 1) {
            for (Map.Entry<Integer, Integer> e : sessionsPerSubjectToday.entrySet()) {
                if (!e.getKey().equals(subjectId) && e.getValue() >= 2) return false;
            }
        }

        return true;
    }

    private boolean canPlaceLabBlock(List<TimetableEntry> placed, ClassGroup classGroup, SessionUnit task,
                                      Room lab, List<Slot> block) {
        int classId = classGroup.getClassId();
        int teacherId = task.teacher.getTeacherId();
        Integer batchId = (task.batch != null) ? task.batch.getBatchId() : null;

        int teacherCountSoFar = 0;
        for (TimetableEntry e : placed) {
            if (e.getTeacher().getTeacherId() == teacherId) teacherCountSoFar++;
        }
        if (teacherCountSoFar + block.size() > task.teacher.getMaxWeeklyHours()) return false;

        for (Slot slot : block) {
            for (TimetableEntry e : placed) {
                if (e.getSlot().getSlotId() != slot.getSlotId()) continue;

                if (e.getTeacher().getTeacherId() == teacherId) return false;
                if (e.getRoom().getRoomId() == lab.getRoomId()) return false;

                if (e.getClassGroup().getClassId() == classId) return false;
            }
        }

        Slot.Day blockDay = block.get(0).getDay();
        for (TimetableEntry e : placed) {
            if (e.getClassGroup().getClassId() == classId
                    && e.getSlot().getDay() == blockDay
                    && e.getSubject().isLab()
                    && e.getSubject().getSubjectId() != task.subject.getSubjectId()) {
                return false;
            }
        }

        return true;
    }

    private List<Slot> getConsecutiveBlockOnSameDay(List<Slot> allSlots, int startIndex, int length) {
        Slot first = allSlots.get(startIndex);
        List<Slot> block = new ArrayList<>();
        for (int i = 0; i < length; i++) {
            Slot current = allSlots.get(startIndex + i);
            if (current.getDay() != first.getDay()) return null;
            block.add(current);
        }
        return block;
    }
}