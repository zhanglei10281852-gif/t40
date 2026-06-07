package com.prison.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FollowUpRecordDTO {

    @NotNull(message = "服刑人员ID不能为空")
    private Long inmateId;

    @NotBlank(message = "回访类型不能为空")
    private String followUpType;

    @NotBlank(message = "回访日期不能为空")
    private String followUpDate;

    @NotNull(message = "联系方式是否有效不能为空")
    private Boolean contactValid;

    private String phoneNumber;

    private String currentStatus;

    private String employmentInfo;

    private String lifeDescription;

    private Boolean hasReoffended;

    private String reoffenseInfo;

    private String remark;

    private String followUpBy;
}
