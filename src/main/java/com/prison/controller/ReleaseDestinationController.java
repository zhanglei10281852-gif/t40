package com.prison.controller;

import com.prison.dto.ReleaseDestinationDTO;
import com.prison.entity.Inmate;
import com.prison.entity.ReleaseDestination;
import com.prison.repository.InmateRepository;
import com.prison.repository.ReleaseDestinationRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/release-destinations")
@RequiredArgsConstructor
public class ReleaseDestinationController {

    private final ReleaseDestinationRepository releaseDestinationRepository;
    private final InmateRepository inmateRepository;

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody ReleaseDestinationDTO dto) {
        Optional<Inmate> inmateOpt = inmateRepository.findById(dto.getInmateId());
        if (inmateOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "服刑人员不存在"));
        }
        Inmate inmate = inmateOpt.get();

        if (releaseDestinationRepository.findByInmateId(dto.getInmateId()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "释放去向登记已存在"));
        }

        ReleaseDestination destination = new ReleaseDestination();
        destination.setInmateId(inmate.getId());
        destination.setInmateNo(inmate.getInmateNo());
        destination.setInmateName(inmate.getName());
        destination.setDestinationType(dto.getDestinationType());
        destination.setDestinationAddress(dto.getDestinationAddress());
        destination.setCommunityName(dto.getCommunityName());
        destination.setCommunityContact(dto.getCommunityContact());
        destination.setCommunityPhone(dto.getCommunityPhone());
        destination.setPoliceStationName(dto.getPoliceStationName());
        destination.setPoliceStationPhone(dto.getPoliceStationPhone());
        destination.setFamilyPickup(dto.getFamilyPickup());
        destination.setFamilyName(dto.getFamilyName());
        destination.setFamilyPhone(dto.getFamilyPhone());
        destination.setHasClearDestination(dto.getHasClearDestination());
        destination.setSpecialRemark(dto.getSpecialRemark());
        if (dto.getStatus() != null) {
            destination.setStatus(dto.getStatus());
        }

        ReleaseDestination saved = releaseDestinationRepository.save(destination);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return releaseDestinationRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/inmate/{inmateId}")
    public ResponseEntity<?> getByInmateId(@PathVariable Long inmateId) {
        return releaseDestinationRepository.findByInmateId(inmateId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/list")
    public ResponseEntity<?> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String destinationType,
            @RequestParam(required = false) String status) {

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<ReleaseDestination> result;

        if (destinationType != null && !destinationType.isBlank()) {
            result = releaseDestinationRepository.findByDestinationType(destinationType, pageRequest);
        } else if (status != null && !status.isBlank()) {
            result = releaseDestinationRepository.findByStatus(status, pageRequest);
        } else {
            result = releaseDestinationRepository.findAll(pageRequest);
        }

        return ResponseEntity.ok(result);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody ReleaseDestinationDTO dto) {
        return releaseDestinationRepository.findById(id).map(dest -> {
            if (dto.getDestinationType() != null) dest.setDestinationType(dto.getDestinationType());
            if (dto.getDestinationAddress() != null) dest.setDestinationAddress(dto.getDestinationAddress());
            if (dto.getCommunityName() != null) dest.setCommunityName(dto.getCommunityName());
            if (dto.getCommunityContact() != null) dest.setCommunityContact(dto.getCommunityContact());
            if (dto.getCommunityPhone() != null) dest.setCommunityPhone(dto.getCommunityPhone());
            if (dto.getPoliceStationName() != null) dest.setPoliceStationName(dto.getPoliceStationName());
            if (dto.getPoliceStationPhone() != null) dest.setPoliceStationPhone(dto.getPoliceStationPhone());
            if (dto.getFamilyPickup() != null) dest.setFamilyPickup(dto.getFamilyPickup());
            if (dto.getFamilyName() != null) dest.setFamilyName(dto.getFamilyName());
            if (dto.getFamilyPhone() != null) dest.setFamilyPhone(dto.getFamilyPhone());
            if (dto.getHasClearDestination() != null) dest.setHasClearDestination(dto.getHasClearDestination());
            if (dto.getSpecialRemark() != null) dest.setSpecialRemark(dto.getSpecialRemark());
            if (dto.getStatus() != null) dest.setStatus(dto.getStatus());
            return ResponseEntity.ok(releaseDestinationRepository.save(dest));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        if (!releaseDestinationRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        releaseDestinationRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stats")
    public ResponseEntity<?> stats() {
        var stats = new java.util.HashMap<String, Object>();
        stats.put("total", releaseDestinationRepository.count());
        stats.put("returnOrigin", releaseDestinationRepository.countByDestinationType("回原籍"));
        stats.put("relyOnRelatives", releaseDestinationRepository.countByDestinationType("投靠亲友"));
        stats.put("other", releaseDestinationRepository.countByDestinationType("其他"));
        stats.put("noClearDestination", releaseDestinationRepository.countByHasClearDestinationFalse());
        return ResponseEntity.ok(stats);
    }
}
