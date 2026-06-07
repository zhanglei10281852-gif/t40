package com.prison.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReleaseEducationDTO {

    @NotNull(message = "服刑人员ID不能为空")
    private Long inmateId;

    @NotBlank(message = "课程编码不能为空")
    private String courseCode;

    @NotBlank(message = "计划完成日期不能为空")
    private String planDate;

    private String teacherName;

    private String remark;
}
