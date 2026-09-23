package com.trae.ams.service.impl;

import com.trae.ams.entity.AmsMemberLevel;
import com.trae.ams.entity.AmsMemberLevelLog;
import com.trae.ams.entity.SysUser;
import com.trae.ams.mapper.AmsMemberLevelLogMapper;
import com.trae.ams.mapper.AmsMemberLevelMapper;
import com.trae.ams.mapper.SysUserMapper;
import com.trae.ams.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MemberServiceImpl implements MemberService {

    @Autowired
    private SysUserMapper sysUserMapper;
    
    @Autowired
    private AmsMemberLevelMapper memberLevelMapper;
    
    @Autowired
    private AmsMemberLevelLogMapper memberLevelLogMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void accumulateConsumption(Long userId, BigDecimal amount) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) return;

        BigDecimal oldTotal = user.getTotalConsumption() == null ? BigDecimal.ZERO : user.getTotalConsumption();
        BigDecimal newTotal = oldTotal.add(amount);
        
        user.setTotalConsumption(newTotal);
        
        // Check for upgrade
        List<AmsMemberLevel> levels = memberLevelMapper.selectAll(); // Ordered by min_consumption ASC
        AmsMemberLevel targetLevel = null;
        
        for (AmsMemberLevel level : levels) {
            if (newTotal.compareTo(level.getMinConsumption()) >= 0) {
                targetLevel = level;
            }
        }
        
        if (targetLevel != null) {
            String currentLevelCode = user.getMembershipLevel();
            // Assuming default is NORMAL if null
            if (currentLevelCode == null) currentLevelCode = "NORMAL";
            
            // If level changed (assuming strictly upgrading, or maybe just syncing to correct level)
            // Here we check if the new target level is different from current.
            // Also we should ensure we don't downgrade automatically unless that's desired.
            // Requirement says "accumulate... auto judge upgrade". Usually upgrades are permanent or re-calculated.
            // Let's assume re-calculation based on total consumption is fine.
            if (!targetLevel.getCode().equals(currentLevelCode)) {
                // Check if it is really an upgrade (higher threshold)
                AmsMemberLevel currentLevelRule = memberLevelMapper.selectByCode(currentLevelCode);
                BigDecimal currentThreshold = currentLevelRule != null ? currentLevelRule.getMinConsumption() : BigDecimal.ZERO;
                
                if (targetLevel.getMinConsumption().compareTo(currentThreshold) > 0) {
                     // Upgrade!
                     user.setMembershipLevel(targetLevel.getCode());
                     user.setDiscountRate(targetLevel.getDiscountRate());
                     
                     // Log
                     AmsMemberLevelLog log = new AmsMemberLevelLog();
                     log.setUserId(userId);
                     log.setOldLevel(currentLevelCode);
                     log.setNewLevel(targetLevel.getCode());
                     log.setTriggerAmount(amount);
                     log.setTotalConsumption(newTotal);
                     memberLevelLogMapper.insert(log);
                }
            }
        }
        
        sysUserMapper.update(user);
    }

    @Override
    public BigDecimal calculatePrice(Long userId, BigDecimal originalPrice) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) return originalPrice;
        
        BigDecimal rate = BigDecimal.ONE;
        if (user.getDiscountRate() != null) {
            rate = user.getDiscountRate();
        } else {
            // Fallback to level rule if user has no specific rate
            String levelCode = user.getMembershipLevel();
            if (levelCode != null) {
                AmsMemberLevel level = memberLevelMapper.selectByCode(levelCode);
                if (level != null) {
                    rate = level.getDiscountRate();
                }
            }
        }
        
        return originalPrice.multiply(rate).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public Map<String, Object> getMemberInfo(Long userId) {
        SysUser user = sysUserMapper.selectById(userId);
        Map<String, Object> result = new HashMap<>();
        if (user == null) return result;
        
        BigDecimal total = user.getTotalConsumption() == null ? BigDecimal.ZERO : user.getTotalConsumption();
        String currentLevelCode = user.getMembershipLevel() == null ? "NORMAL" : user.getMembershipLevel();
        
        result.put("totalConsumption", total);
        result.put("currentLevel", currentLevelCode);
        result.put("discountRate", user.getDiscountRate());
        
        // Find next level
        List<AmsMemberLevel> levels = memberLevelMapper.selectAll();
        AmsMemberLevel currentRule = null;
        AmsMemberLevel nextRule = null;
        
        for (AmsMemberLevel level : levels) {
            if (level.getCode().equals(currentLevelCode)) {
                currentRule = level;
            }
            if (level.getMinConsumption().compareTo(total) > 0) {
                if (nextRule == null) {
                    nextRule = level;
                }
            }
        }
        
        result.put("levelName", currentRule != null ? currentRule.getName() : "普通会员");
        result.put("iconUrl", currentRule != null ? currentRule.getIconUrl() : "");
        
        if (nextRule != null) {
            result.put("nextLevelName", nextRule.getName());
            result.put("nextLevelThreshold", nextRule.getMinConsumption());
            result.put("needAmount", nextRule.getMinConsumption().subtract(total));
            // Progress percentage
            BigDecimal prevThreshold = currentRule != null ? currentRule.getMinConsumption() : BigDecimal.ZERO;
            BigDecimal gap = nextRule.getMinConsumption().subtract(prevThreshold);
            BigDecimal currentProgress = total.subtract(prevThreshold);
            
            if (gap.compareTo(BigDecimal.ZERO) > 0) {
                 result.put("progress", currentProgress.divide(gap, 2, RoundingMode.HALF_UP).multiply(new BigDecimal(100)));
            } else {
                 result.put("progress", 0);
            }
        } else {
            result.put("nextLevelName", null);
            result.put("progress", 100);
            result.put("isMax", true);
        }
        
        return result;
    }

    @Override
    public Object getLevelRules() {
        return memberLevelMapper.selectAll();
    }
}
