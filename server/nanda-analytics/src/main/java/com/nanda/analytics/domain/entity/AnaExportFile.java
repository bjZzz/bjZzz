package com.nanda.analytics.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ana_export_file")
public class AnaExportFile {

    @TableId
    private Long id;
    private Long taskId;
    private String fileRef;
    private Long fileSize;
    private LocalDateTime createdAt;
}
