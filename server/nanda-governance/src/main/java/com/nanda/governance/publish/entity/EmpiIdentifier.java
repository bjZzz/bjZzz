package com.nanda.governance.publish.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("empi_identifier")
public class EmpiIdentifier {

    @TableId
    private Long id;
    private Long empiId;
    private String idType;
    private String idValueEnc;
    private String idHash;
    private String sourceSystem;
    private Integer isPrimary;
}
