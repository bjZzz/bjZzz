package com.nanda.platform.audit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nanda.platform.audit.domain.entity.SysAuditLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SysAuditLogMapper extends BaseMapper<SysAuditLog> {
}
