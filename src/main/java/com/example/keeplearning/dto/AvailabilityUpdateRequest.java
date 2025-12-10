package com.example.keeplearning.dto;

import java.time.LocalTime;

public record AvailabilityUpdateRequest(
        Integer weekday,
        LocalTime startTime,
        LocalTime endTime
) {}
