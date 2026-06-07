package com.prison.repository;

import com.prison.entity.ReleaseDestination;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReleaseDestinationRepository extends JpaRepository<ReleaseDestination, Long> {

    Optional<ReleaseDestination> findByInmateId(Long inmateId);

    Page<ReleaseDestination> findByDestinationType(String destinationType, Pageable pageable);

    Page<ReleaseDestination> findByStatus(String status, Pageable pageable);

    long countByDestinationType(String destinationType);

    long countByHasClearDestinationFalse();
}
