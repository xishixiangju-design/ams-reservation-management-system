package com.trae.ams.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.trae.ams.common.exception.BusinessException;
import com.trae.ams.common.util.PasswordUtil;
import com.trae.ams.entity.MemberInfo;
import com.trae.ams.entity.SysRole;
import com.trae.ams.entity.SysUser;
import com.trae.ams.entity.SysUserRole;
import com.trae.ams.mapper.MemberInfoMapper;
import com.trae.ams.mapper.SysRoleMapper;
import com.trae.ams.mapper.SysUserMapper;
import com.trae.ams.mapper.SysUserRoleMapper;
import com.trae.ams.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CustomerServiceImpl implements CustomerService {
    @Autowired
    private SysUserMapper sysUserMapper;
    
    @Autowired
    private SysRoleMapper sysRoleMapper;
    
    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;
    
    @Autowired
    private MemberInfoMapper memberInfoMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(SysUser user) {
        // 1. Check Username
        SysUser exist = sysUserMapper.selectByUsername(user.getUsername());
        if (exist != null) {
            throw new BusinessException("用户名已存在");
        }

        // 2. Encrypt Password
        String salt = PasswordUtil.generateSalt();
        String encryptedPassword = PasswordUtil.encrypt(user.getPassword(), salt);
        user.setPassword(encryptedPassword);
        user.setSalt(salt);
        user.setStatus(1); // Enable by default
        user.setMembershipLevel("SILVER"); // Default level
        user.setDiscountRate(new BigDecimal("1.0")); // Default discount

        sysUserMapper.insert(user);

        // 3. Bind Role
        SysRole clientRole = sysRoleMapper.selectByCode("ROLE_CUSTOMER");
        if (clientRole == null) {
            throw new BusinessException("默认角色 ROLE_CUSTOMER 不存在");
        }
        sysUserRoleMapper.insert(new SysUserRole(user.getId(), clientRole.getId()));

        // 4. Init Member Info
        MemberInfo memberInfo = new MemberInfo();
        memberInfo.setUserId(user.getId());
        memberInfo.setStoreId(0L);
        memberInfo.setLevel("SILVER");
        memberInfo.setBalance(BigDecimal.ZERO);
        memberInfo.setViolationCountMonth(0);
        memberInfoMapper.insert(memberInfo);
    }

    @Override
    public List<SysUser> search(String keyword) {
        return sysUserMapper.selectList(keyword);
    }
    
    @Override
    public PageInfo<SysUser> list(Integer pageNum, Integer pageSize, String keyword) {
        PageHelper.startPage(pageNum, pageSize);
        List<SysUser> list = sysUserMapper.selectList(keyword);
        return new PageInfo<>(list);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(SysUser user) {
        if (user.getId() == null) {
            throw new BusinessException("用户ID不能为空");
        }
        SysUser exist = sysUserMapper.selectById(user.getId());
        if (exist == null) {
            throw new BusinessException("用户不存在");
        }
        
        // Only update allowed fields
        if (user.getMembershipLevel() != null) exist.setMembershipLevel(user.getMembershipLevel());
        if (user.getDiscountRate() != null) exist.setDiscountRate(user.getDiscountRate());
        if (user.getViolationCount() != null) exist.setViolationCount(user.getViolationCount());
        
        sysUserMapper.update(exist);
    }
    
    @Override
    public SysUser getById(Long id) {
        return sysUserMapper.selectById(id);
    }
}
