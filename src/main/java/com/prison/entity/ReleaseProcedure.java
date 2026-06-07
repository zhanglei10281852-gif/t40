package com.prison.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "release_procedures")
public class ReleaseProcedure {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long inmateId;

    @Column(nullable = false, length = 20)
    private String inmateNo;

    @Column(nullable = false, length = 50)
    private String inmateName;

    @Column(nullable = false)
    private LocalDate releaseDate;

    @Column(nullable = false)
    private Boolean idCardReturned;

    @Column(nullable = false)
    private Boolean personalItemsReturned;

    @Column(length = 1000)
    private String personalItemsList;

    @Column(nullable = false)
    private Boolean releaseCertificateIssued;

    @Column(length = 50)
    private String certificateNo;

    @Column(nullable = false, length = 20)
    private String householdMigrationStatus;

    @Column(nullable = false)
    private Boolean obligationNoticeSigned;

    @Column(length = 500)
    private String remark;

    @Column(nullable = false, length = 20)
    private String status;

    private LocalDateTime confirmedAt;

    @Column(length = 50)
    private String confirmedBy;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = "办理中";
        }
        if (this.idCardReturned == null) {
            this.idCardReturned = false;
        }
        if (this.personalItemsReturned == null) {
            this.personalItemsReturned = false;
        }
        if (this.releaseCertificateIssued == null) {
            this.releaseCertificateIssued = false;
        }
        if (this.householdMigrationStatus == null) {
            this.householdMigrationStatus = "未办理";
        }
        if (this.obligationNoticeSigned == null) {
            this.obligationNoticeSigned = false;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
