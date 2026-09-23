package com.trae.ams.controller;

import com.github.pagehelper.PageInfo;
import com.trae.ams.common.context.UserContext;
import com.trae.ams.common.result.Result;
import com.trae.ams.entity.SysNotification;
import com.trae.ams.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @GetMapping("/me")
    public Result<PageInfo<SysNotification>> getMyNotifications(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = UserContext.getUserId();
        return Result.success(notificationService.getMyNotifications(userId, page, size));
    }

    @PutMapping("/{id}/read")
    public Result<Void> markAsRead(@PathVariable Long id) {
        Long userId = UserContext.getUserId();
        notificationService.markAsRead(userId, id);
        return Result.success();
    }

    @PutMapping("/read-all")
    public Result<Void> markAllAsRead() {
        Long userId = UserContext.getUserId();
        notificationService.markAllAsRead(userId);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteNotification(@PathVariable Long id) {
        Long userId = UserContext.getUserId();
        notificationService.deleteNotification(userId, id);
        return Result.success();
    }
    
    @GetMapping("/unread-count")
    public Result<Long> getUnreadCount() {
        Long userId = UserContext.getUserId();
        return Result.success(notificationService.getUnreadCount(userId));
    }
}