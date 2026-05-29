package com.nanda.platform.user.domain.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class UpdateUserStatusRequest {

    @NotBlank
    private String status;
}
