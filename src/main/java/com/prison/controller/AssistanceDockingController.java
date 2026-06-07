package com.prison.controller;

import com.prison.dto.AssistanceDockingDTO;
import com.prison.entity.AssistanceDocking;
import com.prison.entity.Inmate;
import com.prison.entity.ReleaseDestination;
import com.prison.repository.AssistanceDockingRepository;
import com.prison.repository.InmateRepository;
import com.prison.repository.ReleaseDestinationRepository;
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
import java.time.Period;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/assistance-docking")
@RequiredArgsConstructor
public class AssistanceDockingController {

    private final AssistanceDockingRepository assistanceDockingRepository;
    private final InmateRepository inmateRepository;
    private final ReleaseDestinationRepository releaseDestinationRepository;

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody AssistanceDockingDTO dto) {
        Optional<Inmate> inmateOpt = inmateRepository.findById(dto.getInmateId());
        if (inmateOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "服刑人员不存在"));
        }
        Inmate inmate = inmateOpt.get();

        String dockingNo = "BF" + System.currentTimeMillis();

        AssistanceDocking docking = new AssistanceDocking();
        docking.setDockingNo(dockingNo);
        docking.setInmateId(inmate.getId());
        docking.setInmateNo(inmate.getInmateNo());
        docking.setInmateName(inmate.getName());
        docking.setGender(inmate.getGender());
        docking.setAge(Period.between(inmate.getBirthDate(), LocalDate.now()).getYears());
        docking.setReleaseDate(inmate.getExpectedRelease());
        docking.setReceivingUnit(dto.getReceivingUnit());
        docking.setHasMentalIllness(dto.getHasMentalIllness());
        docking.setHasEconomicDifficulty(dto.getHasEconomicDifficulty());
        docking.setHasSkillSpecialty(dto.getHasSkillSpecialty());
        docking.setSkillDescription(dto.getSkillDescription());
        docking.setSuggestedMeasures(dto.getSuggestedMeasures());
        docking.setSpecialSituation(dto.getSpecialSituation());
        docking.setRemark(dto.getRemark());

        releaseDestinationRepository.findByInmateId(inmate.getId()).ifPresent(dest -> {
            docking.setDestinationType(dest.getDestinationType());
            docking.setDestinationAddress(dest.getDestinationAddress());
        });

        AssistanceDocking saved = assistanceDockingRepository.save(docking);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PostMapping("/generate/{inmateId}")
    public ResponseEntity<?> generateDockingLetter(@PathVariable Long inmateId) {
        Optional<Inmate> inmateOpt = inmateRepository.findById(inmateId);
        if (inmateOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "服刑人员不存在"));
        }
        Inmate inmate = inmateOpt.get();

        Optional<ReleaseDestination> destOpt = releaseDestinationRepository.findByInmateId(inmateId);
        ReleaseDestination dest = destOpt.orElse(null);

        List<AssistanceDocking> dockings = assistanceDockingRepository.findByInmateId(inmateId);
        AssistanceDocking latestDocking = dockings.isEmpty() ? null : dockings.get(dockings.size() - 1);

        int age = Period.between(inmate.getBirthDate(), LocalDate.now()).getYears();

        Map<String, Object> letter = new LinkedHashMap<>();
        letter.put("dockingNo", latestDocking != null ? latestDocking.getDockingNo() : "BF" + System.currentTimeMillis());
        letter.put("generateDate", LocalDate.now().toString());

        Map<String, Object> inmateInfo = new LinkedHashMap<>();
        inmateInfo.put("name", inmate.getName());
        inmateInfo.put("gender", inmate.getGender());
        inmateInfo.put("age", age);
        letter.put("inmateInfo", inmateInfo);

        letter.put("releaseDate", inmate.getExpectedRelease() != null ? inmate.getExpectedRelease().toString() : null);

        Map<String, Object> destination = new LinkedHashMap<>();
        if (latestDocking != null && latestDocking.getDestinationType() != null) {
            destination.put("type", latestDocking.getDestinationType());
            destination.put("address", latestDocking.getDestinationAddress());
        } else if (dest != null) {
            destination.put("type", dest.getDestinationType());
            destination.put("address", dest.getDestinationAddress());
            destination.put("community", dest.getCommunityName());
            destination.put("policeStation", dest.getPoliceStationName());
        }
        letter.put("releaseDestination", destination);

        Map<String, Object> specialSituation = new LinkedHashMap<>();
        if (latestDocking != null) {
            specialSituation.put("hasMentalIllness", Boolean.TRUE.equals(latestDocking.getHasMentalIllness()));
            specialSituation.put("hasEconomicDifficulty", Boolean.TRUE.equals(latestDocking.getHasEconomicDifficulty()));
            specialSituation.put("hasSkillSpecialty", Boolean.TRUE.equals(latestDocking.getHasSkillSpecialty()));
            specialSituation.put("skillDescription", latestDocking.getSkillDescription());
            specialSituation.put("description", latestDocking.getSpecialSituation());
        } else {
            specialSituation.put("hasMentalIllness", false);
            specialSituation.put("hasEconomicDifficulty", false);
            specialSituation.put("hasSkillSpecialty", false);
        }
        letter.put("specialSituation", specialSituation);

        if (latestDocking != null && latestDocking.getSuggestedMeasures() != null
                && !latestDocking.getSuggestedMeasures().isBlank()) {
            letter.put("suggestedMeasures", Arrays.asList(latestDocking.getSuggestedMeasures().split("；|;|\\n")));
        } else {
            letter.put("suggestedMeasures", Arrays.asList(
                    "开展就业指导与技能培训",
                    "落实社会保障政策",
                    "定期回访跟踪帮扶"
            ));
        }

        if (latestDocking != null) {
            letter.put("receivingUnit", latestDocking.getReceivingUnit());
            letter.put("status", latestDocking.getStatus());
        }

        return ResponseEntity.ok(letter);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return assistanceDockingRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/list")
    public ResponseEntity<?> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long inmateId) {

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<AssistanceDocking> result;

        if (inmateId != null) {
            result = assistanceDockingRepository.findByInmateId(inmateId, pageRequest);
        } else if (status != null && !status.isBlank()) {
            result = assistanceDockingRepository.findByStatus(status, pageRequest);
        } else {
            result = assistanceDockingRepository.findAll(pageRequest);
        }

        return ResponseEntity.ok(result);
    }

    @PutMapping("/{id}/send")
    public ResponseEntity<?> sendDocking(@PathVariable Long id) {
        return assistanceDockingRepository.findById(id).map(docking -> {
            docking.setStatus("已发送");
            docking.setSendDate(LocalDate.now());
            return ResponseEntity.ok(assistanceDockingRepository.save(docking));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/receipt")
    public ResponseEntity<?> receipt(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        return assistanceDockingRepository.findById(id).map(docking -> {
            Boolean accepted = params.get("accepted") != null
                    ? Boolean.valueOf(params.get("accepted").toString())
                    : null;
            String assistancePlan = (String) params.get("assistancePlan");
            String receiverName = (String) params.get("receiverName");
            String receiverPhone = (String) params.get("receiverPhone");
            String remark = (String) params.get("remark");

            if (accepted != null) {
                docking.setReceiptAccepted(accepted);
                docking.setReceiptDate(LocalDate.now());
                docking.setStatus(accepted ? "已接收" : "未接收");
            }
            if (assistancePlan != null) docking.setAssistancePlan(assistancePlan);
            if (receiverName != null) docking.setReceiverName(receiverName);
            if (receiverPhone != null) docking.setReceiverPhone(receiverPhone);
            if (remark != null) docking.setRemark(remark);

            return ResponseEntity.ok(assistanceDockingRepository.save(docking));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody AssistanceDockingDTO dto) {
        return assistanceDockingRepository.findById(id).map(docking -> {
            if (dto.getReceivingUnit() != null) docking.setReceivingUnit(dto.getReceivingUnit());
            if (dto.getHasMentalIllness() != null) docking.setHasMentalIllness(dto.getHasMentalIllness());
            if (dto.getHasEconomicDifficulty() != null) docking.setHasEconomicDifficulty(dto.getHasEconomicDifficulty());
            if (dto.getHasSkillSpecialty() != null) docking.setHasSkillSpecialty(dto.getHasSkillSpecialty());
            if (dto.getSkillDescription() != null) docking.setSkillDescription(dto.getSkillDescription());
            if (dto.getSuggestedMeasures() != null) docking.setSuggestedMeasures(dto.getSuggestedMeasures());
            if (dto.getSpecialSituation() != null) docking.setSpecialSituation(dto.getSpecialSituation());
            if (dto.getRemark() != null) docking.setRemark(dto.getRemark());
            return ResponseEntity.ok(assistanceDockingRepository.save(docking));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        if (!assistanceDockingRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        assistanceDockingRepository.deleteById(id);
        return ResponseEntity.noContent().build();
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
        stats.put("total", assistanceDockingRepository.count());
        stats.put("pending", assistanceDockingRepository.countByStatus("待发送"));
        stats.put("sent", assistanceDockingRepository.countByStatus("已发送"));
        stats.put("accepted", assistanceDockingRepository.countByStatus("已接收"));
        stats.put("rejected", assistanceDockingRepository.countByStatus("未接收"));
        stats.put("receiptAccepted", assistanceDockingRepository.countByReceiptAcceptedTrue());
        stats.put("monthlySent", assistanceDockingRepository.countBySendDateBetween(startDate, endDate));

        return ResponseEntity.ok(stats);
    }
}
