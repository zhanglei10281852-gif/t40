package com.prison.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "release_education")
public class ReleaseEducation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long inmateId;

    @Column(nullable = false, length = 20)
    private String inmateNo;

    @Column(nullable = false, length = 50)
    private String inmateName;

    @Column(nullable = false, length = 50)
    private String courseCode;

    @Column(nullable = false, length = 100)
    private String courseName;

    @Column(nullable = false)
    private Integer courseHours;

    @Column(nullable = false)
    private LocalDate planDate;

    @Column(length = 50)
    private String teacherName;

    @Column(length = 20)
    private String status;

    private Integer attendanceCount;

    @Column(length = 20)
    private String examResult;

    private LocalDate actualCompleteDate;

    @Column(length = 500)
    private String remark;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = "未开始";
        }
        if (this.attendanceCount == null) {
            this.attendanceCount = 0;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
