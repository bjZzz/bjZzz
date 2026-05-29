package com.nanda.asset.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("qc_sample_record")
public class QcSampleRecord {

    @TableId
    private Long id;
    private Long batchId;
    private Long patientId;
    private Long orgId;
}
