package com.nanda.platform.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nanda.platform.user.domain.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    @Select("SELECT p.perm_code FROM sys_permission p " +
            "INNER JOIN sys_role_permission rp ON p.id = rp.perm_id AND rp.deleted = 0 " +
            "INNER JOIN sys_user_role ur ON rp.role_id = ur.role_id AND ur.deleted = 0 " +
            "WHERE ur.user_id = #{userId} AND p.deleted = 0")
    List<String> selectPermCodesByUserId(Long userId);
}
