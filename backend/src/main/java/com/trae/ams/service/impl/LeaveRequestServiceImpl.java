package com.trae.ams.service.impl;

import com.trae.ams.common.exception.BusinessException;
import com.trae.ams.dto.attendance.LeaveQueryDTO;
import com.trae.ams.entity.AmsLeaveRequest;
import com.trae.ams.mapper.AmsLeaveRequestMapper;
import com.trae.ams.service.LeaveRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class LeaveRequestServiceImpl implements LeaveRequestService {

    @Autowired
    private AmsLeaveRequestMapper leaveRequestMapper;

    @Override
    public void createRequest(AmsLeaveRequest request) {
        request.setStatus("PENDING");
        request.setCreateTime(LocalDateTime.now());
        leaveRequestMapper.insert(request);
    }

    @Override
    @Transactional
    public void approveRequest(Long id, Long approverId) {
        AmsLeaveRequest request = leaveRequestMapper.selectById(id);
        if (request == null) throw new BusinessException("申请不存在");
        
        request.setStatus("APPROVED");
        request.setAuditBy(approverId);
        request.setAuditTime(LocalDateTime.now());
        request.setUpdateTime(LocalDateTime.now());
        leaveRequestMapper.update(request);
    }

    @Override
    public void rejectRequest(Long id, Long approverId, String reason) {
        AmsLeaveRequest request = leaveRequestMapper.selectById(id);
        if (request == null) throw new BusinessException("申请不存在");
        
        request.setStatus("REJECTED");
        request.setAuditBy(approverId);
        request.setAuditTime(LocalDateTime.now());
        request.setUpdateTime(LocalDateTime.now());
        leaveRequestMapper.update(request);
    }

    @Override
    public List<AmsLeaveRequest> listMyRequests(Long techId) {
        LeaveQueryDTO query = new LeaveQueryDTO();
        query.setTechId(techId);
        return leaveRequestMapper.selectList(query);
    }

    @Override
    public List<AmsLeaveRequest> listPendingRequests(Long storeId) {
        LeaveQueryDTO query = new LeaveQueryDTO();
        query.setStoreId(storeId);
        query.setStatus("PENDING");
        return leaveRequestMapper.selectList(query);
    }
}
