package com.trae.ams.mapper;

import com.trae.ams.entity.SysOperationLog;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface SysOperationLogMapper {
    int insert(SysOperationLog log);
    List<SysOperationLog> selectList(SysOperationLog query);
}
