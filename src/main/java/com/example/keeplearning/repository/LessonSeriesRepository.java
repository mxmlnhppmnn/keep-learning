package com.example.keeplearning.repository;

import com.example.keeplearning.entity.LessonSeries;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LessonSeriesRepository extends JpaRepository<LessonSeries, Long> {

    List<LessonSeries> findByStudentIdOrderByIdDesc(Long studentId);

    List<LessonSeries> findByTeacherIdOrderByIdDesc(Long teacherId);

}
