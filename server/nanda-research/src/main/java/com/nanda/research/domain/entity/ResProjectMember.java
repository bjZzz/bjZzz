package com.nanda.research.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("res_project_member")
public class ResProjectMember {

    @TableId
    private Long id;
    private Long projectId;
    private Long userId;
    private String roleInProject;
    private Long orgId;

    @TableLogic
    private Integer deleted;
}
