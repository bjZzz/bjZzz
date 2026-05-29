package com.nanda.governance.publish.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nanda.governance.publish.entity.EmpiMaster;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface EmpiMasterMapper extends BaseMapper<EmpiMaster> {

    @Select("SELECT empi_id FROM empi_identifier WHERE id_type = #{idType} AND id_hash = #{idHash} LIMIT 1")
    Long findEmpiIdByHash(String idType, String idHash);
}
