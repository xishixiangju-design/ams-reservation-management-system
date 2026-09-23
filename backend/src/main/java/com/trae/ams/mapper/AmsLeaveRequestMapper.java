package com.trae.ams.mapper;

import com.trae.ams.dto.attendance.LeaveQueryDTO;
import com.trae.ams.entity.AmsLeaveRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AmsLeaveRequestMapper {
    int insert(AmsLeaveRequest record);
    
    int update(AmsLeaveRequest record);
    
    AmsLeaveRequest selectById(Long id);
    
    List<AmsLeaveRequest> selectList(LeaveQueryDTO query);
}
