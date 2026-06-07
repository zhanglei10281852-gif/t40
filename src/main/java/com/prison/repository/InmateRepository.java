package com.prison.repository;

import com.prison.entity.Inmate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface InmateRepository extends JpaRepository<Inmate, Long> {
    Optional<Inmate> findByInmateNo(String inmateNo);

    Page<Inmate> findByWard(String ward, Pageable pageable);

    Page<Inmate> findByStatus(String status, Pageable pageable);

    @Query("SELECT i FROM Inmate i WHERE i.name LIKE %:keyword% OR i.inmateNo LIKE %:keyword%")
    Page<Inmate> search(@Param("keyword") String keyword, Pageable pageable);

    long countByStatus(String status);

    long countByWard(String ward);

    @Query("SELECT i FROM Inmate i WHERE i.status = '在押' AND i.expectedRelease BETWEEN :startDate AND :endDate ORDER BY i.expectedRelease ASC")
    List<Inmate> findUpcomingRelease(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT i FROM Inmate i WHERE i.status = '在押' AND i.expectedRelease BETWEEN :startDate AND :endDate ORDER BY i.expectedRelease ASC")
    Page<Inmate> findUpcomingRelease(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate, Pageable pageable);

    @Query("SELECT COUNT(i) FROM Inmate i WHERE i.status = '在押' AND i.expectedRelease BETWEEN :startDate AND :endDate")
    long countUpcomingRelease(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT i FROM Inmate i WHERE i.status = '已释放' AND FUNCTION('DATE', i.updatedAt) BETWEEN :startDate AND :endDate")
    List<Inmate> findReleasedBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT COUNT(i) FROM Inmate i WHERE i.status = '已释放' AND FUNCTION('DATE', i.updatedAt) BETWEEN :startDate AND :endDate")
    long countReleasedBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
