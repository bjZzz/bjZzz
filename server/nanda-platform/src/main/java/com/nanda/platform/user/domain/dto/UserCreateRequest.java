package com.nanda.platform.user.domain.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
public class UserCreateRequest {

    @NotBlank
    private String username;

    @NotBlank
    private String password;

    @NotBlank
    private String displayName;

    private Long primaryOrgId;

    @NotEmpty
    private List<Long> roleIds;

    private List<Long> orgIds;
}
