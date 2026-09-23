package com.trae.ams.service;

import com.github.pagehelper.PageInfo;
import com.trae.ams.entity.SysNotification;

public interface NotificationService {
    /**
     * 发送通知 (异步)
     */
    void sendNotification(Long userId, String title, String content);

    /**
     * 发送站内信 (同步/轻量级)
     */
    void sendStationNotification(Long userId, String title, String content);

    /**
     * 获取我的通知列表
     */
    PageInfo<SysNotification> getMyNotifications(Long userId, int page, int size);

    /**
     * 标记为已读
     */
    void markAsRead(Long userId, Long notificationId);

    /**
     * 全部标记为已读
     */
    void markAllAsRead(Long userId);

    /**
     * 删除通知
     */
    void deleteNotification(Long userId, Long notificationId);
    
    /**
     * 获取未读数量
     */
    long getUnreadCount(Long userId);
}
