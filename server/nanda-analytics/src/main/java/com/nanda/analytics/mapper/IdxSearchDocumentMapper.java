package com.nanda.analytics.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nanda.analytics.domain.entity.IdxSearchDocument;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface IdxSearchDocumentMapper extends BaseMapper<IdxSearchDocument> {

    @Select("SELECT id FROM idx_search_document WHERE empi_id = #{empiId} AND org_id = #{orgId} LIMIT 1")
    Long findIdByEmpiAndOrg(Long empiId, Long orgId);
}
