package com.nanda.analytics.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ana_search_query")
public class AnaSearchQuery {

    @TableId
    private Long id;
    private String queryName;
    private String queryJson;
    private String scope;
    private Long scopeId;
    private Long userId;
    private Long orgId;
    private LocalDateTime createdAt;
}
