package com.nanda.ingestion.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nanda.ingestion.domain.entity.StgRecord;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface StgRecordMapper extends BaseMapper<StgRecord> {
}
