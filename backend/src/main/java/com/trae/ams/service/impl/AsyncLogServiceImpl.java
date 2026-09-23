package com.trae.ams.service.impl;

import com.trae.ams.entity.SysOperationLog;
import com.trae.ams.mapper.SysOperationLogMapper;
import com.trae.ams.service.AsyncLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class AsyncLogServiceImpl implements AsyncLogService {

    @Autowired
    private SysOperationLogMapper operationLogMapper;

    /**
     * 异步保存操作日志
     */
    @Async
    @Override
    public void saveSysLog(SysOperationLog operLog) {
        operationLogMapper.insert(operLog);
    }
}
