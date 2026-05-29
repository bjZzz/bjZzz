package com.nanda.asset.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("pub_knowledge_document")
public class PubKnowledgeDocument {

    @TableId
    private Long id;
    private String title;
    private String docType;
    private String fileRef;
    private Long orgId;
    private LocalDateTime createdAt;

    @TableLogic
    private Integer deleted;
}
