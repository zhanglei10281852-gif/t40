package com.prison.repository;

import com.prison.entity.ReleaseEducation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReleaseEducationRepository extends JpaRepository<ReleaseEducation, Long> {

    List<ReleaseEducation> findByInmateId(Long inmateId);

    Page<ReleaseEducation> findByInmateId(Long inmateId, Pageable pageable);

    Page<ReleaseEducation> findByStatus(String status, Pageable pageable);

    Optional<ReleaseEducation> findByInmateIdAndCourseCode(Long inmateId, String courseCode);

    @Query("SELECT re FROM ReleaseEducation re WHERE re.inmateId = :inmateId AND re.examResult = '通过'")
    List<ReleaseEducation> findPassedByInmateId(@Param("inmateId") Long inmateId);

    long countByInmateIdAndStatus(Long inmateId, String status);

    long countByInmateIdAndExamResult(Long inmateId, String examResult);

    @Query("SELECT COUNT(DISTINCT re.inmateId) FROM ReleaseEducation re WHERE re.status = '已完成'")
    long countCompletedInmates();

    @Query("SELECT COUNT(DISTINCT re.inmateId) FROM ReleaseEducation re")
    long countTotalInmates();
}
