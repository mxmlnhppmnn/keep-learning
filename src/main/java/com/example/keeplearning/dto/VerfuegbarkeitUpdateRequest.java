package com.example.keeplearning.dto;

import java.time.LocalTime;

public record VerfuegbarkeitUpdateRequest(
        Integer wochentag,
        LocalTime startZeit,
        LocalTime endZeit
) {}
