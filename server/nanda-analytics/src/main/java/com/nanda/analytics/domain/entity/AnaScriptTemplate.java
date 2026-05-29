package com.nanda.analytics.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ana_script_template")
public class AnaScriptTemplate {

    @TableId
    private Long id;
    private String templateCode;
    private String templateName;
    private String scriptContent;
    private Long orgId;
    private LocalDateTime createdAt;
}
