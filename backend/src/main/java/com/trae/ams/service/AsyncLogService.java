package com.trae.ams.service;

import com.trae.ams.entity.SysOperationLog;

public interface AsyncLogService {
    /**
     * 异步保存操作日志
     * @param operLog 日志实体
     */
    void saveSysLog(SysOperationLog operLog);
}
