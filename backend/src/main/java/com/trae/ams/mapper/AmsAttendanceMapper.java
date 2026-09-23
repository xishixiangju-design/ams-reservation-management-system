package com.trae.ams.mapper;

import com.trae.ams.dto.attendance.AttendanceQueryDTO;
import com.trae.ams.entity.AmsAttendance;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AmsAttendanceMapper {
    int insert(AmsAttendance record);
    
    int update(AmsAttendance record);
    
    List<AmsAttendance> selectList(AttendanceQueryDTO query);
}
