package com.prison.controller;

import com.prison.dto.FollowUpRecordDTO;
import com.prison.entity.FollowUpRecord;
import com.prison.entity.Inmate;
import com.prison.repository.FollowUpRecordRepository;
import com.prison.repository.InmateRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/follow-up")
@RequiredArgsConstructor
public class FollowUpController {

    private final FollowUpRecordRepository followUpRecordRepository;
    private final InmateRepository inmateRepository;

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody FollowUpRecordDTO dto) {
        Optional<Inmate> inmateOpt = inmateRepository.findById(dto.getInmateId());
        if (inmateOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "服刑人员不存在"));
        }
        Inmate inmate = inmateOpt.get();

        FollowUpRecord record = new FollowUpRecord();
        record.setInmateId(inmate.getId());
        record.setInmateNo(inmate.getInmateNo());
        record.setInmateName(inmate.getName());
        record.setFollowUpType(dto.getFollowUpType());
        record.setFollowUpDate(LocalDate.parse(dto.getFollowUpDate()));
        record.setContactValid(dto.getContactValid());
        record.setPhoneNumber(dto.getPhoneNumber());
        record.setCurrentStatus(dto.getCurrentStatus());
        record.setEmploymentInfo(dto.getEmploymentInfo());
        record.setLifeDescription(dto.getLifeDescription());
        record.setHasReoffended(dto.getHasReoffended());
        record.setReoffenseInfo(dto.getReoffenseInfo());
        record.setRemark(dto.getRemark());
        record.setFollowUpBy(dto.getFollowUpBy());

        if (dto.getCurrentStatus() != null && "失联".equals(dto.getCurrentStatus())) {
            record.setStatus("失联上报");
        }

        FollowUpRecord saved = followUpRecordRepository.save(record);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return followUpRecordRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/inmate/{inmateId}")
    public ResponseEntity<?> getByInmateId(@PathVariable Long inmateId) {
        List<FollowUpRecord> list = followUpRecordRepository.findByInmateIdOrderByFollowUpDateDesc(inmateId);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/list")
    public ResponseEntity<?> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String followUpType,
            @RequestParam(required = false) String currentStatus,
            @RequestParam(required = false) Long inmateId) {

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("followUpDate").descending());
        Page<FollowUpRecord> result;

        if (inmateId != null) {
            result = followUpRecordRepository.findByInmateId(inmateId, pageRequest);
        } else if (followUpType != null && !followUpType.isBlank()) {
            result = followUpRecordRepository.findByFollowUpType(followUpType, pageRequest);
        } else if (currentStatus != null && !currentStatus.isBlank()) {
            result = followUpRecordRepository.findByCurrentStatus(currentStatus, pageRequest);
        } else {
            result = followUpRecordRepository.findAll(pageRequest);
        }

        return ResponseEntity.ok(result);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody FollowUpRecordDTO dto) {
        return followUpRecordRepository.findById(id).map(record -> {
            if (dto.getFollowUpType() != null) record.setFollowUpType(dto.getFollowUpType());
            if (dto.getFollowUpDate() != null) record.setFollowUpDate(LocalDate.parse(dto.getFollowUpDate()));
            if (dto.getContactValid() != null) record.setContactValid(dto.getContactValid());
            if (dto.getPhoneNumber() != null) record.setPhoneNumber(dto.getPhoneNumber());
            if (dto.getCurrentStatus() != null) record.setCurrentStatus(dto.getCurrentStatus());
            if (dto.getEmploymentInfo() != null) record.setEmploymentInfo(dto.getEmploymentInfo());
            if (dto.getLifeDescription() != null) record.setLifeDescription(dto.getLifeDescription());
            if (dto.getHasReoffended() != null) record.setHasReoffended(dto.getHasReoffended());
            if (dto.getReoffenseInfo() != null) record.setReoffenseInfo(dto.getReoffenseInfo());
            if (dto.getRemark() != null) record.setRemark(dto.getRemark());
            if (dto.getFollowUpBy() != null) record.setFollowUpBy(dto.getFollowUpBy());

            if (dto.getCurrentStatus() != null && "失联".equals(dto.getCurrentStatus())) {
                record.setStatus("失联上报");
            } else if (dto.getCurrentStatus() != null) {
                record.setStatus("已完成");
            }

            return ResponseEntity.ok(followUpRecordRepository.save(record));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        if (!followUpRecordRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        followUpRecordRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/pending")
    public ResponseEntity<?> getPendingFollowUps() {
        List<Inmate> releasedInmates = inmateRepository.findByStatus("已释放", PageRequest.of(0, 1000)).getContent();

        List<Map<String, Object>> pendingList = new ArrayList<>();

        for (Inmate inmate : releasedInmates) {
            LocalDate releaseDate = inmate.getExpectedRelease();
            if (releaseDate == null) continue;

            LocalDate now = LocalDate.now();
            long monthsSinceRelease = java.time.temporal.ChronoUnit.MONTHS.between(releaseDate, now);

            List<FollowUpRecord> records = followUpRecordRepository.findByInmateIdOrderByFollowUpDateDesc(inmate.getId());
            Set<String> doneTypes = records.stream()
                    .map(FollowUpRecord::getFollowUpType)
                    .collect(Collectors.toSet());

            List<String> pendingTypes = new ArrayList<>();
            if (monthsSinceRelease >= 3 && !doneTypes.contains("3个月回访")) {
                pendingTypes.add("3个月回访");
            }
            if (monthsSinceRelease >= 6 && !doneTypes.contains("6个月回访")) {
                pendingTypes.add("6个月回访");
            }
            if (monthsSinceRelease >= 12 && !doneTypes.contains("12个月回访")) {
                pendingTypes.add("12个月回访");
            }

            if (!pendingTypes.isEmpty()) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("inmateId", inmate.getId());
                item.put("inmateNo", inmate.getInmateNo());
                item.put("name", inmate.getName());
                item.put("releaseDate", releaseDate);
                item.put("monthsSinceRelease", monthsSinceRelease);
                item.put("pendingTypes", pendingTypes);
                pendingList.add(item);
            }
        }

        return ResponseEntity.ok(pendingList);
    }

    @GetMapping("/stats")
    public ResponseEntity<?> stats(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        if (startDate == null) {
            startDate = LocalDate.now().withDayOfMonth(1);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        Map<String, Object> stats = new HashMap<>();
        long total = followUpRecordRepository.countByFollowUpDateBetween(startDate, endDate);
        long contactValid = followUpRecordRepository.countByContactValidTrueAndFollowUpDateBetween(startDate, endDate);
        long employed = followUpRecordRepository.countByCurrentStatusAndFollowUpDateBetween("已就业", startDate, endDate);
        long unemployed = followUpRecordRepository.countByCurrentStatusAndFollowUpDateBetween("未就业", startDate, endDate);
        long lostContact = followUpRecordRepository.countByCurrentStatusAndFollowUpDateBetween("失联", startDate, endDate);
        long reoffended = followUpRecordRepository.countByHasReoffendedTrueAndFollowUpDateBetween(startDate, endDate);

        stats.put("totalFollowUps", total);
        stats.put("contactValid", contactValid);
        stats.put("contactValidRate", contactValid > 0 && total > 0 ? (double) contactValid / total * 100 : 0);
        stats.put("employed", employed);
        stats.put("unemployed", unemployed);
        stats.put("lostContact", lostContact);
        stats.put("reoffended", reoffended);
        stats.put("employmentRate", contactValid > 0 ? (double) employed / contactValid * 100 : 0);

        return ResponseEntity.ok(stats);
    }
}
