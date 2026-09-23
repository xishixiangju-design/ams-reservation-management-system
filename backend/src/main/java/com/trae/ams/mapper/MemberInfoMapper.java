package com.trae.ams.mapper;

import com.trae.ams.entity.MemberInfo;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MemberInfoMapper {
    int insert(MemberInfo memberInfo);
}
