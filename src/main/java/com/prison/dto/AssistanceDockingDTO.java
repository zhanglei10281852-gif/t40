package com.prison.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AssistanceDockingDTO {

    @NotNull(message = "服刑人员ID不能为空")
    private Long inmateId;

    private String receivingUnit;

    private Boolean hasMentalIllness;

    private Boolean hasEconomicDifficulty;

    private Boolean hasSkillSpecialty;

    private String skillDescription;

    private String suggestedMeasures;

    private String specialSituation;

    private String remark;
}
