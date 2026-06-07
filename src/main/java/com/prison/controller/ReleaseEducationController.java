package com.prison.controller;

import com.prison.dto.ReleaseEducationDTO;
import com.prison.entity.EducationCourse;
import com.prison.entity.Inmate;
import com.prison.entity.ReleaseEducation;
import com.prison.repository.EducationCourseRepository;
import com.prison.repository.InmateRepository;
import com.prison.repository.ReleaseEducationRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/release-education")
@RequiredArgsConstructor
public class ReleaseEducationController {

    private final ReleaseEducationRepository releaseEducationRepository;
    private final EducationCourseRepository educationCourseRepository;
    private final InmateRepository inmateRepository;

    @PostMapping("/plan")
    public ResponseEntity<?> createEducationPlan(@Valid @RequestBody ReleaseEducationDTO dto) {
        Optional<Inmate> inmateOpt = inmateRepository.findById(dto.getInmateId());
        if (inmateOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "服刑人员不存在"));
        }
        Inmate inmate = inmateOpt.get();

        Optional<EducationCourse> courseOpt = educationCourseRepository.findByCourseCode(dto.getCourseCode());
        if (courseOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "课程不存在"));
        }
        EducationCourse course = courseOpt.get();

        if (releaseEducationRepository.findByInmateIdAndCourseCode(dto.getInmateId(), dto.getCourseCode()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "该课程教育计划已存在"));
        }

        ReleaseEducation education = new ReleaseEducation();
        education.setInmateId(inmate.getId());
        education.setInmateNo(inmate.getInmateNo());
        education.setInmateName(inmate.getName());
        education.setCourseCode(course.getCourseCode());
        education.setCourseName(course.getCourseName());
        education.setCourseHours(course.getCourseHours());
        education.setPlanDate(LocalDate.parse(dto.getPlanDate()));
        education.setTeacherName(dto.getTeacherName());
        education.setRemark(dto.getRemark());

        ReleaseEducation saved = releaseEducationRepository.save(education);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PostMapping("/plan/batch")
    public ResponseEntity<?> createBatchEducationPlan(@RequestBody Map<String, Object> params) {
        Long inmateId = Long.valueOf(params.get("inmateId").toString());
        Optional<Inmate> inmateOpt = inmateRepository.findById(inmateId);
        if (inmateOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "服刑人员不存在"));
        }
        Inmate inmate = inmateOpt.get();

        List<EducationCourse> courses = educationCourseRepository.findByRequiredTrueOrderBySortOrderAsc();
        List<ReleaseEducation> created = new ArrayList<>();

        LocalDate baseDate = inmate.getExpectedRelease() != null
                ? inmate.getExpectedRelease().minusDays(60)
                : LocalDate.now().plusDays(30);

        for (EducationCourse course : courses) {
            if (releaseEducationRepository.findByInmateIdAndCourseCode(inmateId, course.getCourseCode()).isPresent()) {
                continue;
            }
            ReleaseEducation education = new ReleaseEducation();
            education.setInmateId(inmate.getId());
            education.setInmateNo(inmate.getInmateNo());
            education.setInmateName(inmate.getName());
            education.setCourseCode(course.getCourseCode());
            education.setCourseName(course.getCourseName());
            education.setCourseHours(course.getCourseHours());
            education.setPlanDate(baseDate.plusDays(course.getSortOrder() * 7L));
            created.add(releaseEducationRepository.save(education));
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/inmate/{inmateId}")
    public ResponseEntity<?> getByInmate(@PathVariable Long inmateId) {
        List<ReleaseEducation> list = releaseEducationRepository.findByInmateId(inmateId);

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);

        long total = list.size();
        long completed = list.stream().filter(e -> "已完成".equals(e.getStatus())).count();
        long passed = list.stream().filter(e -> "通过".equals(e.getExamResult())).count();

        result.put("totalCourses", total);
        result.put("completedCount", completed);
        result.put("passedCount", passed);
        result.put("allPassed", total > 0 && passed == total);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/list")
    public ResponseEntity<?> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long inmateId) {

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<ReleaseEducation> result;

        if (inmateId != null) {
            result = releaseEducationRepository.findByInmateId(inmateId, pageRequest);
        } else if (status != null && !status.isBlank()) {
            result = releaseEducationRepository.findByStatus(status, pageRequest);
        } else {
            result = releaseEducationRepository.findAll(pageRequest);
        }

        return ResponseEntity.ok(result);
    }

    @PutMapping("/{id}/attendance")
    public ResponseEntity<?> updateAttendance(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        return releaseEducationRepository.findById(id).map(education -> {
            Integer attendanceCount = params.get("attendanceCount") != null
                    ? Integer.valueOf(params.get("attendanceCount").toString())
                    : education.getAttendanceCount() + 1;
            education.setAttendanceCount(attendanceCount);

            if (education.getStatus().equals("未开始")) {
                education.setStatus("进行中");
            }

            return ResponseEntity.ok(releaseEducationRepository.save(education));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/exam")
    public ResponseEntity<?> updateExamResult(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        return releaseEducationRepository.findById(id).map(education -> {
            String examResult = (String) params.get("examResult");
            education.setExamResult(examResult);

            if ("通过".equals(examResult)) {
                education.setStatus("已完成");
                education.setActualCompleteDate(LocalDate.now());
            } else {
                education.setStatus("进行中");
            }

            ReleaseEducation saved = releaseEducationRepository.save(education);

            checkAndUpdateEducationComplete(education.getInmateId());

            return ResponseEntity.ok(saved);
        }).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Map<String, Object> updates) {
        return releaseEducationRepository.findById(id).map(education -> {
            if (updates.containsKey("planDate")) {
                education.setPlanDate(LocalDate.parse((String) updates.get("planDate")));
            }
            if (updates.containsKey("teacherName")) {
                education.setTeacherName((String) updates.get("teacherName"));
            }
            if (updates.containsKey("remark")) {
                education.setRemark((String) updates.get("remark"));
            }
            if (updates.containsKey("status")) {
                education.setStatus((String) updates.get("status"));
            }
            return ResponseEntity.ok(releaseEducationRepository.save(education));
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return releaseEducationRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        if (!releaseEducationRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        releaseEducationRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private void checkAndUpdateEducationComplete(Long inmateId) {
        List<ReleaseEducation> all = releaseEducationRepository.findByInmateId(inmateId);
        if (all.isEmpty()) return;

        long requiredCount = educationCourseRepository.findByRequiredTrueOrderBySortOrderAsc().size();
        long passedCount = all.stream()
                .filter(e -> "通过".equals(e.getExamResult()))
                .count();

        if (passedCount >= requiredCount) {
        }
    }
}
