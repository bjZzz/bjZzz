package com.nanda.ingestion.datasource;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nanda.common.core.exception.BusinessException;
import com.nanda.common.core.exception.ErrorCode;
import com.nanda.common.core.result.PageQuery;
import com.nanda.common.core.result.PageResult;
import com.nanda.common.security.context.AuthContext;
import com.nanda.common.security.context.AuthContextHolder;
import com.nanda.common.util.IdGenerator;
import com.nanda.ingestion.adapter.AdapterRegistry;
import com.nanda.ingestion.adapter.ConnectionTestResult;
import com.nanda.ingestion.adapter.DataSourceConfig;
import com.nanda.ingestion.domain.dto.ConnectionTestVO;
import com.nanda.ingestion.domain.dto.DataSourceCreateRequest;
import com.nanda.ingestion.domain.dto.DataSourceVO;
import com.nanda.ingestion.domain.entity.StgDatasource;
import com.nanda.ingestion.mapper.StgDatasourceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DataSourceService {

    private final StgDatasourceMapper stgDatasourceMapper;
    private final AdapterRegistry adapterRegistry;

    public PageResult<DataSourceVO> list(PageQuery query) {
        Long orgId = requireOrgId();
        Page<StgDatasource> page = stgDatasourceMapper.selectPage(
                new Page<StgDatasource>(query.getPage(), query.getSize()),
                new LambdaQueryWrapper<StgDatasource>()
                        .eq(StgDatasource::getOrgId, orgId)
                        .eq(StgDatasource::getDeleted, 0)
                        .orderByDesc(StgDatasource::getCreatedAt));
        List<DataSourceVO> items = new ArrayList<DataSourceVO>();
        for (StgDatasource ds : page.getRecords()) {
            items.add(toVO(ds));
        }
        return PageResult.of(items, query.getPage(), query.getSize(), page.getTotal());
    }

    public DataSourceVO getById(Long id) {
        return toVO(requireDatasource(id));
    }

    @Transactional
    public DataSourceVO create(DataSourceCreateRequest request) {
        Long orgId = requireOrgId();
        long exists = stgDatasourceMapper.selectCount(new LambdaQueryWrapper<StgDatasource>()
                .eq(StgDatasource::getSourceCode, request.getSourceCode())
                .eq(StgDatasource::getOrgId, orgId)
                .eq(StgDatasource::getDeleted, 0));
        if (exists > 0) {
            throw new BusinessException(ErrorCode.CONFLICT, "数据源编码已存在");
        }
        DataSourceConfig config = adapterRegistry.parseConfig(request.getConfigJson());
        ConnectionTestResult test = adapterRegistry.getAdapter(request.getProtocol()).testConnection(config);
        if (!test.isSuccess()) {
            throw new BusinessException(ErrorCode.INGESTION_CONNECTION_FAILED, test.getMessage());
        }
        AuthContext ctx = AuthContextHolder.get();
        StgDatasource ds = new StgDatasource();
        ds.setId(IdGenerator.nextId());
        ds.setSourceCode(request.getSourceCode());
        ds.setSourceName(request.getSourceName());
        ds.setProtocol(request.getProtocol().toUpperCase());
        ds.setConfigJson(request.getConfigJson());
        ds.setOrgId(orgId);
        ds.setStatus("ACTIVE");
        ds.setCreatedBy(ctx != null ? ctx.getUserId() : null);
        ds.setCreatedAt(LocalDateTime.now());
        ds.setUpdatedAt(LocalDateTime.now());
        stgDatasourceMapper.insert(ds);
        return toVO(ds);
    }

    public ConnectionTestVO testConnection(Long id) {
        StgDatasource ds = requireDatasource(id);
        DataSourceConfig config = adapterRegistry.parseConfig(ds.getConfigJson());
        ConnectionTestResult result = adapterRegistry.getAdapter(ds.getProtocol()).testConnection(config);
        ConnectionTestVO vo = new ConnectionTestVO();
        vo.setSuccess(result.isSuccess());
        vo.setMessage(result.getMessage());
        if (!result.isSuccess()) {
            throw new BusinessException(ErrorCode.INGESTION_CONNECTION_FAILED, result.getMessage());
        }
        return vo;
    }

    private StgDatasource requireDatasource(Long id) {
        StgDatasource ds = stgDatasourceMapper.selectById(id);
        if (ds == null || ds.getDeleted() != null && ds.getDeleted() == 1) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "数据源不存在");
        }
        Long orgId = requireOrgId();
        if (!orgId.equals(ds.getOrgId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权访问该数据源");
        }
        return ds;
    }

    private Long requireOrgId() {
        AuthContext ctx = AuthContextHolder.get();
        if (ctx == null || ctx.getOrgId() == null) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "缺少机构上下文");
        }
        return ctx.getOrgId();
    }

    private DataSourceVO toVO(StgDatasource ds) {
        DataSourceVO vo = new DataSourceVO();
        vo.setId(ds.getId());
        vo.setSourceCode(ds.getSourceCode());
        vo.setSourceName(ds.getSourceName());
        vo.setProtocol(ds.getProtocol());
        vo.setConfigJson(ds.getConfigJson());
        vo.setOrgId(ds.getOrgId());
        vo.setStatus(ds.getStatus());
        vo.setCreatedAt(ds.getCreatedAt());
        return vo;
    }
}
