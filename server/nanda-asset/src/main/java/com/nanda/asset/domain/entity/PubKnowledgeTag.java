package com.nanda.asset.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("pub_knowledge_tag")
public class PubKnowledgeTag {

    @TableId
    private Long id;
    private Long documentId;
    private String tagName;
}
