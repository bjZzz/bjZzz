package com.nanda.platform.user.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_permission")
public class SysPermission {

    @TableId
    private Long id;
    private String permCode;
    private String permName;
    private String module;
    private LocalDateTime createdAt;

    @TableLogic
    private Integer deleted;
}
