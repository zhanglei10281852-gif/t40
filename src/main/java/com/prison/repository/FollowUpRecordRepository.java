package com.prison.repository;

import com.prison.entity.FollowUpRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface FollowUpRecordRepository extends JpaRepository<FollowUpRecord, Long> {

    List<FollowUpRecord> findByInmateIdOrderByFollowUpDateDesc(Long inmateId);

    Page<FollowUpRecord> findByInmateId(Long inmateId, Pageable pageable);

    Page<FollowUpRecord> findByFollowUpType(String followUpType, Pageable pageable);

    Page<FollowUpRecord> findByCurrentStatus(String currentStatus, Pageable pageable);

    long countByFollowUpDateBetween(LocalDate startDate, LocalDate endDate);

    long countByCurrentStatusAndFollowUpDateBetween(String currentStatus, LocalDate startDate, LocalDate endDate);

    long countByContactValidTrueAndFollowUpDateBetween(LocalDate startDate, LocalDate endDate);

    long countByHasReoffendedTrueAndFollowUpDateBetween(LocalDate startDate, LocalDate endDate);

    List<FollowUpRecord> findByInmateIdAndFollowUpType(Long inmateId, String followUpType);
}
