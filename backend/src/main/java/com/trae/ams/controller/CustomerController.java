package com.trae.ams.controller;

import com.github.pagehelper.PageInfo;
import com.trae.ams.common.result.Result;
import com.trae.ams.entity.SysUser;
import com.trae.ams.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/customers")
public class CustomerController {
    @Autowired
    private CustomerService customerService;

    @GetMapping("/search")
    public Result<List<SysUser>> search(@RequestParam(required = false) String keyword) {
        return Result.success(customerService.search(keyword));
    }
    
    @GetMapping
    public Result<PageInfo<SysUser>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword) {
        return Result.success(customerService.list(pageNum, pageSize, keyword));
    }
    
    @PostMapping
    public Result<Void> add(@RequestBody SysUser user) {
        if (user.getUsername() == null || user.getPassword() == null) {
            return Result.error("用户名和密码不能为空");
        }
        customerService.add(user);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody SysUser user) {
        user.setId(id);
        customerService.update(user);
        return Result.success();
    }
}
