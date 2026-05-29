package com.nanda.platform.user.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("sys_user_role")
public class SysUserRole {

    @TableId
    private Long id;
    private Long userId;
    private Long roleId;

    @TableLogic
    private Integer deleted;
}
