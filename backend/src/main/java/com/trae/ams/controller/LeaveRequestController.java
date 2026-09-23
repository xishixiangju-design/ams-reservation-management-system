package com.trae.ams.controller;

import com.trae.ams.common.context.UserContext;
import com.trae.ams.common.result.Result;
import com.trae.ams.entity.AmsLeaveRequest;
import com.trae.ams.service.LeaveRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/leave")
public class LeaveRequestController {

    @Autowired
    private LeaveRequestService leaveRequestService;

    @PostMapping("/create")
    public Result<Void> create(@RequestBody AmsLeaveRequest request) {
        Long userId = UserContext.getUserId();
        if (userId == null) throw new RuntimeException("未登录");
        
        request.setTechId(userId);
        request.setStoreId(UserContext.getStoreIdOrDefault());
        leaveRequestService.createRequest(request);
        return Result.success();
    }

    @GetMapping("/my")
    public Result<List<AmsLeaveRequest>> listMy() {
        Long userId = UserContext.getUserId();
        if (userId == null) throw new RuntimeException("未登录");
        return Result.success(leaveRequestService.listMyRequests(userId));
    }
    
    @GetMapping("/pending")
    public Result<List<AmsLeaveRequest>> listPending() {
        Long storeId = UserContext.getStoreIdOrDefault();
        return Result.success(leaveRequestService.listPendingRequests(storeId));
    }
    
    @PostMapping("/{id}/approve")
    public Result<Void> approve(@PathVariable Long id) {
        Long userId = UserContext.getUserId(); // Approver
        leaveRequestService.approveRequest(id, userId);
        return Result.success();
    }
    
    @PostMapping("/{id}/reject")
    public Result<Void> reject(@PathVariable Long id, @RequestParam(required = false) String reason) {
        Long userId = UserContext.getUserId(); // Approver
        leaveRequestService.rejectRequest(id, userId, reason);
        return Result.success();
    }
}
