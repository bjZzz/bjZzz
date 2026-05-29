package com.nanda.platform.user.domain.dto;

import lombok.Data;

@Data
public class UserUpdateRequest {

    private String displayName;
    private Long primaryOrgId;
}
