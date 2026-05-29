package com.nanda.ingestion.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nanda.ingestion.domain.entity.StgBatch;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface StgBatchMapper extends BaseMapper<StgBatch> {
}
