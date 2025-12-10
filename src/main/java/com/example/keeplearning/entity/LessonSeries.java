package com.example.keeplearning.entity;

import jakarta.persistence.*;

import java.time.LocalTime;

@Entity
@Table(name = "lesson_series")
public class LessonSeries {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long teacherId;
    private Long studentId;
    private Long subjectId;

    private int weekday;

    private LocalTime startTime;

    private int duration;

    private boolean trialLesson;

    //Getter und Setter

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(Long lehrerId) {
        this.teacherId = lehrerId;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public Long getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(Long subjectId) {
        this.subjectId = subjectId;
    }

    public int getWeekday() {
        return weekday;
    }

    public void setWeekday(int weekday) {
        this.weekday = weekday;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public boolean isTrialLesson() {
        return trialLesson;
    }

    public void setTrialLesson(boolean trialLesson) {
        this.trialLesson = trialLesson;
    }
}

