package com.prison.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReleaseProcedureDTO {

    @NotNull(message = "服刑人员ID不能为空")
    private Long inmateId;

    private Boolean idCardReturned;

    private Boolean personalItemsReturned;

    private String personalItemsList;

    private Boolean releaseCertificateIssued;

    private String certificateNo;

    private String householdMigrationStatus;

    private Boolean obligationNoticeSigned;

    private String remark;

    private String confirmedBy;
}
