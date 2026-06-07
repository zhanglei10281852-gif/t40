package com.prison.repository;

import com.prison.entity.EducationCourse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EducationCourseRepository extends JpaRepository<EducationCourse, Long> {

    Optional<EducationCourse> findByCourseCode(String courseCode);

    List<EducationCourse> findByRequiredTrueOrderBySortOrderAsc();

    List<EducationCourse> findAllByOrderBySortOrderAsc();
}
