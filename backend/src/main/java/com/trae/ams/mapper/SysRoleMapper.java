package com.trae.ams.mapper;

import com.trae.ams.entity.SysRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SysRoleMapper {
    int insert(SysRole sysRole);

    SysRole selectByCode(@Param("code") String code);
}
