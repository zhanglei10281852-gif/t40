package com.prison.repository;

import com.prison.entity.ReleaseProcedure;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface ReleaseProcedureRepository extends JpaRepository<ReleaseProcedure, Long> {

    Optional<ReleaseProcedure> findByInmateId(Long inmateId);

    Page<ReleaseProcedure> findByStatus(String status, Pageable pageable);

    long countByStatus(String status);

    long countByReleaseDateBetween(LocalDate startDate, LocalDate endDate);

    Page<ReleaseProcedure> findByReleaseDateBetween(LocalDate startDate, LocalDate endDate, Pageable pageable);
}
