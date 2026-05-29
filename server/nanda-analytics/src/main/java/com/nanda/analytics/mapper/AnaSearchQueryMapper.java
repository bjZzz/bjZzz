package com.nanda.analytics.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nanda.analytics.domain.entity.AnaSearchQuery;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AnaSearchQueryMapper extends BaseMapper<AnaSearchQuery> {
}
