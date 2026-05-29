package com.nanda.platform.user.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_user_org")
public class SysUserOrg {

    @TableId
    private Long id;
    private Long userId;
    private Long orgId;
    private LocalDateTime createdAt;

    @TableLogic
    private Integer deleted;
}
