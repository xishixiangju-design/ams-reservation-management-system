package com.trae.ams.mapper;

import com.trae.ams.entity.AmsMemberLevelLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AmsMemberLevelLogMapper {
    int insert(AmsMemberLevelLog log);
}
