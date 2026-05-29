package com.nanda.analytics.sandbox;

import com.nanda.analytics.domain.dto.AnalyticsW8Dtos.DatasetMountRequest;
import com.nanda.analytics.domain.dto.AnalyticsW8Dtos.DatasetVO;
import com.nanda.analytics.domain.entity.AnaSandboxDataset;
import com.nanda.analytics.mapper.AnaSandboxDatasetMapper;
import com.nanda.analytics.service.AnalyticsOrgContext;
import com.nanda.common.util.IdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 数据集挂载：MVP 写入 ana_sandbox_dataset 并通知 ComputePlane。
 * MinIO Parquet 聚合在 W9 联调阶段替换为真实实现。
 */
@Service
@RequiredArgsConstructor
public class DatasetMountService {

    private final AnaSandboxDatasetMapper anaSandboxDatasetMapper;
    private final SandboxClient sandboxClient;

    @Transactional
    public DatasetVO mount(DatasetMountRequest request) {
        Long orgId = AnalyticsOrgContext.requireOrgId();
        Long userId = AnalyticsOrgContext.currentUserId();
        String datasetId = "ds-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        int rowLimit = request.getRowLimit() != null ? request.getRowLimit() : 1000;

        Map<String, Object> mountSpec = new LinkedHashMap<String, Object>();
        mountSpec.put("sourceType", request.getSourceType());
        mountSpec.put("searchQueryId", request.getSearchQueryId());
        mountSpec.put("rowLimit", rowLimit);
        sandboxClient.mountDataset(datasetId, orgId, userId, mountSpec);

        AnaSandboxDataset entity = new AnaSandboxDataset();
        entity.setId(IdGenerator.nextId());
        entity.setDatasetId(datasetId);
        entity.setOrgId(orgId);
        entity.setSourceType(request.getSourceType());
        entity.setMinioPath("minio://sandbox/" + orgId + "/" + datasetId + ".parquet");
        entity.setRowCount(rowLimit);
        entity.setExpiresAt(LocalDateTime.now().plusDays(7));
        entity.setCreatedAt(LocalDateTime.now());
        anaSandboxDatasetMapper.insert(entity);
        return toVO(entity);
    }

    private DatasetVO toVO(AnaSandboxDataset entity) {
        DatasetVO vo = new DatasetVO();
        vo.setDatasetId(entity.getDatasetId());
        vo.setSourceType(entity.getSourceType());
        vo.setRowCount(entity.getRowCount());
        vo.setExpiresAt(entity.getExpiresAt());
        vo.setCreatedAt(entity.getCreatedAt());
        return vo;
    }
}
