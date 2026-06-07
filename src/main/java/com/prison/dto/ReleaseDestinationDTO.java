package com.prison.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReleaseDestinationDTO {

    @NotNull(message = "服刑人员ID不能为空")
    private Long inmateId;

    @NotBlank(message = "去向类型不能为空")
    private String destinationType;

    @NotBlank(message = "目的地地址不能为空")
    private String destinationAddress;

    private String communityName;

    private String communityContact;

    private String communityPhone;

    private String policeStationName;

    private String policeStationPhone;

    @NotNull(message = "是否家属来接不能为空")
    private Boolean familyPickup;

    private String familyName;

    private String familyPhone;

    @NotNull(message = "是否有明确接收地不能为空")
    private Boolean hasClearDestination;

    private String specialRemark;

    private String status;
}
