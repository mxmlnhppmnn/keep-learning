package com.example.keeplearning.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public class Timeslot {
    private LocalDate date;
    private LocalTime start;
    private LocalTime end;

    public Timeslot(LocalDate date, LocalTime start, LocalTime end) {
        this.date = date;
        this.start = start;
        this.end = end;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getStart() {
        return start;
    }

    public void setStart(LocalTime start) {
        this.start = start;
    }

    public LocalTime getEnd() {
        return end;
    }

    public void setEnd(LocalTime end) {
        this.end = end;
    }
}
