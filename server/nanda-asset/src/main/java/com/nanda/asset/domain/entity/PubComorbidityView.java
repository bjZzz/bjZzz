package com.nanda.asset.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("pub_comorbidity_view")
public class PubComorbidityView {

    @TableId
    private Long id;
    private Long ruleId;
    private Long empiId;
    private String specialtyRecordIds;
    private String comorbidityLabels;
    private Integer refreshVersion;
    private LocalDateTime refreshedAt;
}
