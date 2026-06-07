package com.prison.controller;

import com.prison.dto.EducationCourseDTO;
import com.prison.entity.EducationCourse;
import com.prison.repository.EducationCourseRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/education-courses")
@RequiredArgsConstructor
public class EducationCourseController {

    private final EducationCourseRepository educationCourseRepository;

    @GetMapping
    public ResponseEntity<?> list() {
        List<EducationCourse> courses = educationCourseRepository.findAllByOrderBySortOrderAsc();
        return ResponseEntity.ok(courses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return educationCourseRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody EducationCourseDTO dto) {
        if (educationCourseRepository.findByCourseCode(dto.getCourseCode()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "课程编码已存在"));
        }
        EducationCourse course = new EducationCourse();
        course.setCourseCode(dto.getCourseCode());
        course.setCourseName(dto.getCourseName());
        course.setCourseHours(dto.getCourseHours());
        course.setDescription(dto.getDescription());
        course.setRequired(dto.getRequired() != null ? dto.getRequired() : true);
        course.setSortOrder(dto.getSortOrder());
        EducationCourse saved = educationCourseRepository.save(course);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody EducationCourseDTO dto) {
        return educationCourseRepository.findById(id).map(course -> {
            if (dto.getCourseName() != null) course.setCourseName(dto.getCourseName());
            if (dto.getCourseHours() != null) course.setCourseHours(dto.getCourseHours());
            if (dto.getDescription() != null) course.setDescription(dto.getDescription());
            if (dto.getRequired() != null) course.setRequired(dto.getRequired());
            if (dto.getSortOrder() != null) course.setSortOrder(dto.getSortOrder());
            return ResponseEntity.ok(educationCourseRepository.save(course));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        if (!educationCourseRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        educationCourseRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
