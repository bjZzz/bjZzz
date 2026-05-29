package com.nanda.analytics.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("ana_algorithm_registry")
public class AnaAlgorithmRegistry {

    @TableId
    private Long id;
    private String algorithmCode;
    private String algorithmName;
    private String version;
    private String packageRef;
    private String status;
}
