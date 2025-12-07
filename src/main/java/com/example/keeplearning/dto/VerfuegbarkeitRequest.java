package com.example.keeplearning.dto;

import java.time.LocalTime;

public record VerfuegbarkeitRequest(
        int wochentag,
        LocalTime startZeit,
        LocalTime endZeit
) {}
