package com.trae.ams.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.trae.ams.entity.SysNotification;
import com.trae.ams.entity.SysUser;
import com.trae.ams.mapper.SysNotificationMapper;
import com.trae.ams.mapper.SysUserMapper;
import com.trae.ams.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationServiceImpl.class);

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private SysNotificationMapper notificationMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    @Async // 异步发送，不阻塞主流程
    public void sendNotification(Long userId, String title, String content) {
        // 1. 获取用户信息
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            log.error("User not found for notification: {}", userId);
            return;
        }

        // 2. 确定发送渠道 (优先邮箱，其次站内信)
        String email = user.getEmail(); 
        
        if (email == null || email.isEmpty()) {
             log.warn("User {} has no email configured.", userId);
             // Create a system notification record only
             sendStationNotification(userId, title, content);
             return;
        }

        // 3. 发送邮件
        sendEmail(userId, email, title, content);
        // 同时发送站内信
        sendStationNotification(userId, title, content);
    }

    @Override
    public void sendStationNotification(Long userId, String title, String content) {
        createRecord(userId, "SYSTEM", title, content, "SYSTEM", 1, null);
    }

    @Override
    public PageInfo<SysNotification> getMyNotifications(Long userId, int page, int size) {
        PageHelper.startPage(page, size);
        List<SysNotification> list = notificationMapper.selectByUserIdAndType(userId, "SYSTEM");
        return new PageInfo<>(list);
    }

    @Override
    public void markAsRead(Long userId, Long notificationId) {
        SysNotification notification = notificationMapper.selectById(notificationId);
        if (notification != null && notification.getUserId().equals(userId)) {
            notificationMapper.updateReadStatus(notificationId, 1);
        }
    }

    @Override
    public void markAllAsRead(Long userId) {
        notificationMapper.markAllAsRead(userId);
    }

    @Override
    public void deleteNotification(Long userId, Long notificationId) {
        SysNotification notification = notificationMapper.selectById(notificationId);
        if (notification != null && notification.getUserId().equals(userId)) {
            notificationMapper.deleteById(notificationId);
        }
    }
    
    @Override
    public long getUnreadCount(Long userId) {
        return notificationMapper.countUnreadByUserIdAndType(userId, "SYSTEM");
    }

    private void sendEmail(Long userId, String to, String title, String text) {
        SysNotification record = createRecord(userId, "EMAIL", title, text, to, 0, null);
        
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(title);
            message.setText(text);
            
            mailSender.send(message);
            
            // 更新状态
            record.setStatus(1);
            record.setSendTime(LocalDateTime.now());
            notificationMapper.update(record);
            
            log.info("Email sent to {} for user {}", to, userId);
        } catch (Exception e) {
            log.error("Failed to send email to {}", to, e);
            record.setStatus(2);
            record.setErrorMsg(e.getMessage());
            notificationMapper.update(record);
        }
    }

    private SysNotification createRecord(Long userId, String type, String title, String content, String target, int status, String error) {
        SysNotification record = new SysNotification();
        record.setStoreId(1L); // Default store
        record.setUserId(userId);
        record.setType(type);
        record.setTitle(title);
        record.setContent(content);
        record.setTarget(target);
        record.setStatus(status);
        record.setReadStatus(0); // Default Unread
        record.setErrorMsg(error);
        record.setCreateTime(LocalDateTime.now());
        notificationMapper.insert(record);
        return record;
    }
}
