package com.trae.ams.mapper;

import com.trae.ams.entity.AmsViolationRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AmsViolationRecordMapper {
    int insert(AmsViolationRecord record);

    List<AmsViolationRecord> selectByUserId(@Param("userId") Long userId);
    
    int countViolation(@Param("userId") Long userId, @Param("storeId") Long storeId);
}
