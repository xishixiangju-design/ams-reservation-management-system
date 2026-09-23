package com.trae.ams.service;

import com.trae.ams.entity.AmsLeaveRequest;
import java.util.List;

public interface LeaveRequestService {
    void createRequest(AmsLeaveRequest request);
    
    void approveRequest(Long id, Long approverId);
    
    void rejectRequest(Long id, Long approverId, String reason);
    
    List<AmsLeaveRequest> listMyRequests(Long techId);
    
    List<AmsLeaveRequest> listPendingRequests(Long storeId);
}
