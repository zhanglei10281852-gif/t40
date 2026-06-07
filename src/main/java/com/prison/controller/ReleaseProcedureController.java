package com.prison.controller;

import com.prison.dto.ReleaseProcedureDTO;
import com.prison.entity.Inmate;
import com.prison.entity.ReleaseProcedure;
import com.prison.repository.InmateRepository;
import com.prison.repository.ReleaseProcedureRepository;
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
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/release-procedures")
@RequiredArgsConstructor
public class ReleaseProcedureController {

    private final ReleaseProcedureRepository releaseProcedureRepository;
    private final InmateRepository inmateRepository;

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody ReleaseProcedureDTO dto) {
        Optional<Inmate> inmateOpt = inmateRepository.findById(dto.getInmateId());
        if (inmateOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "服刑人员不存在"));
        }
        Inmate inmate = inmateOpt.get();

        if (releaseProcedureRepository.findByInmateId(dto.getInmateId()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "释放手续记录已存在"));
        }

        ReleaseProcedure procedure = new ReleaseProcedure();
        procedure.setInmateId(inmate.getId());
        procedure.setInmateNo(inmate.getInmateNo());
        procedure.setInmateName(inmate.getName());
        procedure.setReleaseDate(inmate.getExpectedRelease() != null ? inmate.getExpectedRelease() : LocalDate.now());

        if (dto.getIdCardReturned() != null) procedure.setIdCardReturned(dto.getIdCardReturned());
        if (dto.getPersonalItemsReturned() != null) procedure.setPersonalItemsReturned(dto.getPersonalItemsReturned());
        if (dto.getPersonalItemsList() != null) procedure.setPersonalItemsList(dto.getPersonalItemsList());
        if (dto.getReleaseCertificateIssued() != null) procedure.setReleaseCertificateIssued(dto.getReleaseCertificateIssued());
        if (dto.getCertificateNo() != null) procedure.setCertificateNo(dto.getCertificateNo());
        if (dto.getHouseholdMigrationStatus() != null) procedure.setHouseholdMigrationStatus(dto.getHouseholdMigrationStatus());
        if (dto.getObligationNoticeSigned() != null) procedure.setObligationNoticeSigned(dto.getObligationNoticeSigned());
        if (dto.getRemark() != null) procedure.setRemark(dto.getRemark());

        ReleaseProcedure saved = releaseProcedureRepository.save(procedure);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return releaseProcedureRepository.findById(id)
                .map(this::enrichWithChecklist)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/inmate/{inmateId}")
    public ResponseEntity<?> getByInmateId(@PathVariable Long inmateId) {
        return releaseProcedureRepository.findByInmateId(inmateId)
                .map(this::enrichWithChecklist)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/list")
    public ResponseEntity<?> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<ReleaseProcedure> result;

        if (startDate != null && endDate != null) {
            result = releaseProcedureRepository.findByReleaseDateBetween(startDate, endDate, pageRequest);
        } else if (status != null && !status.isBlank()) {
            result = releaseProcedureRepository.findByStatus(status, pageRequest);
        } else {
            result = releaseProcedureRepository.findAll(pageRequest);
        }

        return ResponseEntity.ok(result);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody ReleaseProcedureDTO dto) {
        return releaseProcedureRepository.findById(id).map(proc -> {
            if (dto.getIdCardReturned() != null) proc.setIdCardReturned(dto.getIdCardReturned());
            if (dto.getPersonalItemsReturned() != null) proc.setPersonalItemsReturned(dto.getPersonalItemsReturned());
            if (dto.getPersonalItemsList() != null) proc.setPersonalItemsList(dto.getPersonalItemsList());
            if (dto.getReleaseCertificateIssued() != null) proc.setReleaseCertificateIssued(dto.getReleaseCertificateIssued());
            if (dto.getCertificateNo() != null) proc.setCertificateNo(dto.getCertificateNo());
            if (dto.getHouseholdMigrationStatus() != null) proc.setHouseholdMigrationStatus(dto.getHouseholdMigrationStatus());
            if (dto.getObligationNoticeSigned() != null) proc.setObligationNoticeSigned(dto.getObligationNoticeSigned());
            if (dto.getRemark() != null) proc.setRemark(dto.getRemark());
            return ResponseEntity.ok(releaseProcedureRepository.save(proc));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/confirm-release")
    public ResponseEntity<?> confirmRelease(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        return releaseProcedureRepository.findById(id).map(proc -> {
            if (!canRelease(proc)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "释放手续未全部完成，无法确认释放"));
            }

            String confirmedBy = (String) params.get("confirmedBy");

            proc.setStatus("已释放");
            proc.setConfirmedAt(LocalDateTime.now());
            proc.setConfirmedBy(confirmedBy);

            ReleaseProcedure saved = releaseProcedureRepository.save(proc);

            inmateRepository.findById(proc.getInmateId()).ifPresent(inmate -> {
                inmate.setStatus("已释放");
                inmateRepository.save(inmate);
            });

            return ResponseEntity.ok(saved);
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/checklist")
    public ResponseEntity<?> getChecklist(@PathVariable Long id) {
        return releaseProcedureRepository.findById(id)
                .map(this::enrichWithChecklist)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        if (!releaseProcedureRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        releaseProcedureRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private boolean canRelease(ReleaseProcedure proc) {
        return proc.getIdCardReturned()
                && proc.getPersonalItemsReturned()
                && proc.getReleaseCertificateIssued()
                && "已办理".equals(proc.getHouseholdMigrationStatus())
                && proc.getObligationNoticeSigned();
    }

    private Map<String, Object> enrichWithChecklist(ReleaseProcedure proc) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", proc.getId());
        result.put("inmateId", proc.getInmateId());
        result.put("inmateNo", proc.getInmateNo());
        result.put("inmateName", proc.getInmateName());
        result.put("releaseDate", proc.getReleaseDate());
        result.put("status", proc.getStatus());
        result.put("remark", proc.getRemark());
        result.put("confirmedAt", proc.getConfirmedAt());
        result.put("confirmedBy", proc.getConfirmedBy());

        List<Map<String, Object>> checklist = new ArrayList<>();

        Map<String, Object> item1 = new LinkedHashMap<>();
        item1.put("code", "id_card_returned");
        item1.put("name", "身份证件归还");
        item1.put("completed", proc.getIdCardReturned());
        checklist.add(item1);

        Map<String, Object> item2 = new LinkedHashMap<>();
        item2.put("code", "personal_items_returned");
        item2.put("name", "个人物品清点归还");
        item2.put("completed", proc.getPersonalItemsReturned());
        item2.put("detail", proc.getPersonalItemsList());
        checklist.add(item2);

        Map<String, Object> item3 = new LinkedHashMap<>();
        item3.put("code", "release_certificate_issued");
        item3.put("name", "刑满释放证明出具");
        item3.put("completed", proc.getReleaseCertificateIssued());
        item3.put("detail", proc.getCertificateNo());
        checklist.add(item3);

        Map<String, Object> item4 = new LinkedHashMap<>();
        item4.put("code", "household_migration");
        item4.put("name", "户籍迁移手续办理");
        item4.put("completed", "已办理".equals(proc.getHouseholdMigrationStatus()));
        item4.put("status", proc.getHouseholdMigrationStatus());
        checklist.add(item4);

        Map<String, Object> item5 = new LinkedHashMap<>();
        item5.put("code", "obligation_notice_signed");
        item5.put("name", "释放后义务告知书签字");
        item5.put("completed", proc.getObligationNoticeSigned());
        checklist.add(item5);

        result.put("checklist", checklist);
        result.put("allCompleted", canRelease(proc));

        return result;
    }
}
