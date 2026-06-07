package com.prison.repository;

import com.prison.entity.AssistanceDocking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AssistanceDockingRepository extends JpaRepository<AssistanceDocking, Long> {

    Optional<AssistanceDocking> findByDockingNo(String dockingNo);

    List<AssistanceDocking> findByInmateId(Long inmateId);

    Page<AssistanceDocking> findByInmateId(Long inmateId, Pageable pageable);

    Page<AssistanceDocking> findByStatus(String status, Pageable pageable);

    long countByStatus(String status);

    long countByReceiptAcceptedTrue();

    List<AssistanceDocking> findBySendDateBetween(LocalDate startDate, LocalDate endDate);

    long countBySendDateBetween(LocalDate startDate, LocalDate endDate);
}
