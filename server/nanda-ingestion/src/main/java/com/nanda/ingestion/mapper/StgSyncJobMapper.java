package com.nanda.ingestion.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nanda.ingestion.domain.entity.StgSyncJob;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface StgSyncJobMapper extends BaseMapper<StgSyncJob> {
}
