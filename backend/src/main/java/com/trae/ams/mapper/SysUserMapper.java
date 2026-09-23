package com.trae.ams.mapper;

import com.trae.ams.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface SysUserMapper {
    int insert(SysUser sysUser);

    int update(SysUser sysUser);

    SysUser selectByUsername(@Param("username") String username);

    SysUser selectById(@Param("id") Long id);

    SysUser selectByEmail(@Param("email") String email);

    List<SysUser> selectList(@Param("keyword") String keyword);

    int deleteById(@Param("id") Long id);
}
