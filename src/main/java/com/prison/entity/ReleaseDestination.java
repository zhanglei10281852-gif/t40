package com.prison.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "release_destinations")
public class ReleaseDestination {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long inmateId;

    @Column(nullable = false, length = 20)
    private String inmateNo;

    @Column(nullable = false, length = 50)
    private String inmateName;

    @Column(nullable = false, length = 20)
    private String destinationType;

    @Column(nullable = false, length = 200)
    private String destinationAddress;

    @Column(length = 100)
    private String communityName;

    @Column(length = 50)
    private String communityContact;

    @Column(length = 20)
    private String communityPhone;

    @Column(length = 100)
    private String policeStationName;

    @Column(length = 20)
    private String policeStationPhone;

    @Column(nullable = false)
    private Boolean familyPickup;

    @Column(length = 50)
    private String familyName;

    @Column(length = 20)
    private String familyPhone;

    @Column(nullable = false)
    private Boolean hasClearDestination;

    @Column(length = 500)
    private String specialRemark;

    @Column(length = 20)
    private String status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = "待确认";
        }
        if (this.hasClearDestination == null) {
            this.hasClearDestination = true;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
