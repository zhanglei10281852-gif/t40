package com.prison.controller;

import com.prison.entity.Inmate;
import com.prison.repository.InmateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/release-preview")
@RequiredArgsConstructor
public class ReleasePreviewController {

    private final InmateRepository inmateRepository;

    @GetMapping("/list")
    public ResponseEntity<?> getUpcomingReleaseList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Integer days,
            @RequestParam(required = false) String ward) {

        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusDays(days != null ? days : 90);

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("expectedRelease").ascending());
        Page<Inmate> inmates = inmateRepository.findUpcomingRelease(today, endDate, pageRequest);

        List<Map<String, Object>> result = inmates.getContent().stream()
                .map(this::enrichWithReminderLevel)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("content", result);
        response.put("totalElements", inmates.getTotalElements());
        response.put("totalPages", inmates.getTotalPages());
        response.put("page", inmates.getNumber());
        response.put("size", inmates.getSize());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getPreviewStats() {
        LocalDate today = LocalDate.now();

        long within90Days = inmateRepository.countUpcomingRelease(today, today.plusDays(90));
        long within30Days = inmateRepository.countUpcomingRelease(today, today.plusDays(30));
        long within7Days = inmateRepository.countUpcomingRelease(today, today.plusDays(7));

        Map<String, Object> stats = new HashMap<>();
        stats.put("within90Days", within90Days);
        stats.put("within30Days", within30Days);
        stats.put("within7Days", within7Days);

        return ResponseEntity.ok(stats);
    }

    @GetMapping("/levels")
    public ResponseEntity<?> getByReminderLevel() {
        LocalDate today = LocalDate.now();

        List<Inmate> within90Days = inmateRepository.findUpcomingRelease(today, today.plusDays(90));
        List<Inmate> within30Days = inmateRepository.findUpcomingRelease(today, today.plusDays(30));
        List<Inmate> within7Days = inmateRepository.findUpcomingRelease(today, today.plusDays(7));

        List<Map<String, Object>> level90 = within90Days.stream()
                .filter(i -> ChronoUnit.DAYS.between(today, i.getExpectedRelease()) > 30)
                .map(this::enrichWithReminderLevel)
                .collect(Collectors.toList());

        List<Map<String, Object>> level30 = within30Days.stream()
                .filter(i -> ChronoUnit.DAYS.between(today, i.getExpectedRelease()) > 7)
                .map(this::enrichWithReminderLevel)
                .collect(Collectors.toList());

        List<Map<String, Object>> level7 = within7Days.stream()
                .map(this::enrichWithReminderLevel)
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("level90", level90);
        result.put("level30", level30);
        result.put("level7", level7);

        return ResponseEntity.ok(result);
    }

    private Map<String, Object> enrichWithReminderLevel(Inmate inmate) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", inmate.getId());
        map.put("inmateNo", inmate.getInmateNo());
        map.put("name", inmate.getName());
        map.put("gender", inmate.getGender());
        map.put("crime", inmate.getCrime());
        map.put("ward", inmate.getWard());
        map.put("cellNo", inmate.getCellNo());
        map.put("expectedRelease", inmate.getExpectedRelease());
        map.put("status", inmate.getStatus());

        long daysLeft = ChronoUnit.DAYS.between(LocalDate.now(), inmate.getExpectedRelease());
        map.put("daysLeft", daysLeft);

        String reminderLevel;
        String action;
        if (daysLeft <= 7) {
            reminderLevel = "7天提醒";
            action = "需完成释放手续准备";
        } else if (daysLeft <= 30) {
            reminderLevel = "30天提醒";
            action = "需落实安置帮教";
        } else {
            reminderLevel = "90天提醒";
            action = "需启动出监教育";
        }
        map.put("reminderLevel", reminderLevel);
        map.put("action", action);

        return map;
    }
}
