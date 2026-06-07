package com.prison.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "assistance_docking")
public class AssistanceDocking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String dockingNo;

    @Column(nullable = false)
    private Long inmateId;

    @Column(nullable = false, length = 20)
    private String inmateNo;

    @Column(nullable = false, length = 50)
    private String inmateName;

    @Column(length = 10)
    private String gender;

    private Integer age;

    @Column(nullable = false)
    private LocalDate releaseDate;

    @Column(length = 20)
    private String destinationType;

    @Column(length = 200)
    private String destinationAddress;

    @Column(length = 100)
    private String receivingUnit;

    private Boolean hasMentalIllness;

    private Boolean hasEconomicDifficulty;

    private Boolean hasSkillSpecialty;

    @Column(length = 500)
    private String skillDescription;

    @Column(length = 1000)
    private String suggestedMeasures;

    @Column(length = 500)
    private String specialSituation;

    @Column(nullable = false, length = 20)
    private String status;

    private LocalDate sendDate;

    private LocalDate receiptDate;

    private Boolean receiptAccepted;

    @Column(length = 1000)
    private String assistancePlan;

    @Column(length = 50)
    private String receiverName;

    @Column(length = 20)
    private String receiverPhone;

    @Column(length = 500)
    private String remark;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = "待发送";
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
