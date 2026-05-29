package com.nanda.asset.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nanda.asset.domain.entity.PubComorbidityView;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface PubComorbidityViewMapper extends BaseMapper<PubComorbidityView> {

    @Select("SELECT id FROM pub_comorbidity_view WHERE rule_id = #{ruleId} AND empi_id = #{empiId} LIMIT 1")
    Long findViewId(Long ruleId, Long empiId);
}
