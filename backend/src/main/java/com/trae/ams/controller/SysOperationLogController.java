package com.trae.ams.controller;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.trae.ams.common.context.UserContext;
import com.trae.ams.common.result.Result;
import com.trae.ams.entity.SysOperationLog;
import com.trae.ams.mapper.SysOperationLogMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/log")
public class SysOperationLogController {

    @Autowired
    private SysOperationLogMapper operationLogMapper;

    /**
     * 查询操作日志
     */
    @GetMapping("/list")
    public Result<PageInfo<SysOperationLog>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Integer status) {
        
        if (!UserContext.isAdmin()) {
            return Result.error(403, "无权查看日志");
        }

        PageHelper.startPage(pageNum, pageSize);
        SysOperationLog query = new SysOperationLog();
        query.setModule(module);
        query.setUsername(username);
        query.setStatus(status);
        
        List<SysOperationLog> list = operationLogMapper.selectList(query);
        return Result.success(new PageInfo<>(list));
    }
}
