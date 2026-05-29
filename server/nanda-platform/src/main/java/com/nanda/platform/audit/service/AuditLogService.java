package com.nanda.platform.audit.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nanda.common.core.result.PageQuery;
import com.nanda.common.core.result.PageResult;
import com.nanda.platform.audit.domain.dto.AuditLogVO;
import com.nanda.platform.audit.domain.entity.SysAuditLog;
import com.nanda.platform.audit.mapper.SysAuditLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final SysAuditLogMapper sysAuditLogMapper;

    public PageResult<AuditLogVO> list(PageQuery query, Long userId, String dateFrom, String dateTo) {
        LambdaQueryWrapper<SysAuditLog> wrapper = new LambdaQueryWrapper<SysAuditLog>()
                .eq(userId != null, SysAuditLog::getUserId, userId)
                .orderByDesc(SysAuditLog::getCreatedAt);
        if (StringUtils.hasText(dateFrom)) {
            wrapper.ge(SysAuditLog::getCreatedAt, LocalDateTime.parse(dateFrom + " 00:00:00", DATE_TIME));
        }
        if (StringUtils.hasText(dateTo)) {
            wrapper.le(SysAuditLog::getCreatedAt, LocalDateTime.parse(dateTo + " 23:59:59", DATE_TIME));
        }
        Page<SysAuditLog> page = sysAuditLogMapper.selectPage(
                new Page<SysAuditLog>(query.getPage(), query.getSize()), wrapper);
        List<AuditLogVO> items = new ArrayList<AuditLogVO>();
        for (SysAuditLog log : page.getRecords()) {
            items.add(toVO(log));
        }
        return PageResult.of(items, query.getPage(), query.getSize(), page.getTotal());
    }

    private AuditLogVO toVO(SysAuditLog log) {
        AuditLogVO vo = new AuditLogVO();
        vo.setId(log.getId());
        vo.setUserId(log.getUserId());
        vo.setAction(log.getAction());
        vo.setResourceType(log.getResourceType());
        vo.setResourceId(log.getResourceId());
        vo.setDetailJson(log.getDetailJson());
        vo.setIp(log.getIp());
        vo.setOrgId(log.getOrgId());
        vo.setCreatedAt(log.getCreatedAt());
        return vo;
    }
}
