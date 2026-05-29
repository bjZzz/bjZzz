package com.nanda.integration.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nanda.integration.domain.entity.IntWritebackLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface IntWritebackLogMapper extends BaseMapper<IntWritebackLog> {
}
