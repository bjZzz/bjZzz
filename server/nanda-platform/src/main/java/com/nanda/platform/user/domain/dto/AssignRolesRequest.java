package com.nanda.platform.user.domain.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
public class AssignRolesRequest {

    @NotEmpty
    private List<Long> roleIds;
}
