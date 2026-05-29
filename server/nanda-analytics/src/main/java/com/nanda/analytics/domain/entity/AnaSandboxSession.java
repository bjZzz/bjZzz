package com.nanda.analytics.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ana_sandbox_session")
public class AnaSandboxSession {

    @TableId
    private Long id;
    private Long userId;
    private Long orgId;
    private String workspaceId;
    private String status;
    private String kernelStatus;
    private LocalDateTime lastActiveAt;
}
