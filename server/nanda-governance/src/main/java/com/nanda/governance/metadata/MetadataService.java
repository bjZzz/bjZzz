package com.nanda.governance.metadata;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nanda.common.security.context.AuthContextHolder;
import com.nanda.governance.domain.dto.LineageEdgeVO;
import com.nanda.governance.domain.dto.MetadataCatalogVO;
import com.nanda.governance.domain.entity.GovMetadataCatalog;
import com.nanda.governance.domain.entity.GovMetadataLineageEdge;
import com.nanda.governance.mapper.GovMetadataCatalogMapper;
import com.nanda.governance.mapper.GovMetadataLineageEdgeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MetadataService {

    private final GovMetadataCatalogMapper govMetadataCatalogMapper;
    private final GovMetadataLineageEdgeMapper govMetadataLineageEdgeMapper;

    public List<MetadataCatalogVO> listCatalog() {
        Long orgId = AuthContextHolder.get().getOrgId();
        List<GovMetadataCatalog> list = govMetadataCatalogMapper.selectList(new LambdaQueryWrapper<GovMetadataCatalog>()
                .eq(GovMetadataCatalog::getDeleted, 0)
                .and(w -> w.eq(GovMetadataCatalog::getOrgId, orgId).or().isNull(GovMetadataCatalog::getOrgId)));
        List<MetadataCatalogVO> result = new ArrayList<MetadataCatalogVO>();
        for (GovMetadataCatalog c : list) {
            MetadataCatalogVO vo = new MetadataCatalogVO();
            vo.setId(c.getId());
            vo.setCatalogCode(c.getCatalogCode());
            vo.setCatalogName(c.getCatalogName());
            vo.setParentId(c.getParentId());
            result.add(vo);
        }
        return result;
    }

    public List<LineageEdgeVO> queryLineage(String sourceType, String sourceId) {
        Long orgId = AuthContextHolder.get().getOrgId();
        LambdaQueryWrapper<GovMetadataLineageEdge> wrapper = new LambdaQueryWrapper<GovMetadataLineageEdge>()
                .eq(GovMetadataLineageEdge::getOrgId, orgId);
        if (StringUtils.hasText(sourceType)) {
            wrapper.eq(GovMetadataLineageEdge::getSourceType, sourceType);
        }
        if (StringUtils.hasText(sourceId)) {
            wrapper.eq(GovMetadataLineageEdge::getSourceId, sourceId);
        }
        List<GovMetadataLineageEdge> edges = govMetadataLineageEdgeMapper.selectList(wrapper);
        List<LineageEdgeVO> result = new ArrayList<LineageEdgeVO>();
        for (GovMetadataLineageEdge edge : edges) {
            LineageEdgeVO vo = new LineageEdgeVO();
            vo.setId(edge.getId());
            vo.setSourceType(edge.getSourceType());
            vo.setSourceId(edge.getSourceId());
            vo.setTargetType(edge.getTargetType());
            vo.setTargetId(edge.getTargetId());
            result.add(vo);
        }
        return result;
    }
}
