package com.trae.ams.service;

import com.github.pagehelper.PageInfo;
import com.trae.ams.entity.SysUser;
import java.util.List;

public interface CustomerService {
    List<SysUser> search(String keyword);
    
    /**
     * 分页查询用户列表
     */
    PageInfo<SysUser> list(Integer pageNum, Integer pageSize, String keyword);
    
    /**
     * 更新用户信息 (会员等级、折扣、违约次数)
     */
    void update(SysUser user);
    
    /**
     * 根据ID获取用户
     */
    SysUser getById(Long id);

    /**
     * 新增用户
     */
    void add(SysUser user);
}
