package com.asiet.aids.timetable;

import java.util.ArrayList;
import java.util.List;

import com.asiet.aids.timetable.dao.ClassGroupDAO;
import com.asiet.aids.timetable.model.ClassGroup;
import com.asiet.aids.timetable.service.TimetableGenerationService;
import com.asiet.aids.timetable.exception.SchedulingException;
public class Main {
    public static void main(String[] args) {
    System.out.println("Automatic Timetable Generator - starting up...");
    try {
        ClassGroupDAO classGroupDAO = new ClassGroupDAO();
        List<ClassGroup> allClasses = new ArrayList<>();
        allClasses.add(classGroupDAO.findById(1)); // AI3
        allClasses.add(classGroupDAO.findById(2)); // TestSplit
        allClasses.add(classGroupDAO.findById(3)); // S3AI

        TimetableGenerationService engine = new TimetableGenerationService();
        engine.generateForAllClasses(allClasses);

        System.out.println("Generation completed for all classes. Check timetable_entry table.");
    } catch (SchedulingException e) {
        System.err.println("Scheduling failed: " + e.getMessage());
    } catch (Exception e) {
        System.err.println("Unexpected error during generation.");
        e.printStackTrace();
    }
}
}