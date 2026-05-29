package com.nanda.governance.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("gov_dict_diagnosis")
public class GovDictDiagnosis {

    @TableId
    private Long id;
    private String code;
    private String nameZh;
    private String nameEn;
    private Long orgId;

    @TableLogic
    private Integer deleted;
}
