package com.prison.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "follow_up_records")
public class FollowUpRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long inmateId;

    @Column(nullable = false, length = 20)
    private String inmateNo;

    @Column(nullable = false, length = 50)
    private String inmateName;

    @Column(nullable = false, length = 20)
    private String followUpType;

    @Column(nullable = false)
    private LocalDate followUpDate;

    @Column(nullable = false)
    private Boolean contactValid;

    @Column(length = 20)
    private String phoneNumber;

    @Column(length = 20)
    private String currentStatus;

    @Column(length = 100)
    private String employmentInfo;

    @Column(length = 1000)
    private String lifeDescription;

    private Boolean hasReoffended;

    @Column(length = 500)
    private String reoffenseInfo;

    @Column(length = 500)
    private String remark;

    @Column(length = 20)
    private String status;

    @Column(length = 50)
    private String followUpBy;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = "已完成";
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
