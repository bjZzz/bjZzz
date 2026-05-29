package com.nanda.platform.user.domain.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class UserVO {

    private Long id;
    private String username;
    private String displayName;
    private Long primaryOrgId;
    private String status;
    private Long orgId;
    private List<Long> roleIds;
    private List<Long> orgIds;
    private LocalDateTime createdAt;
}
