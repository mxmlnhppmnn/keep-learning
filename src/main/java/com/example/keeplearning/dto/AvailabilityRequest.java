package com.example.keeplearning.dto;

import java.time.LocalTime;

public record AvailabilityRequest(
        int weekday,
        LocalTime startTime,
        LocalTime endTime
) {}
