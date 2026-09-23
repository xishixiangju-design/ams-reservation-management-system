package com.trae.ams.mapper;

import com.trae.ams.entity.AmsPricingRule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AmsPricingRuleMapper {
    /**
     * 根据服务ID查询适用的规则（包括全局规则）
     */
    List<AmsPricingRule> selectRulesByService(@Param("storeId") Long storeId, @Param("serviceId") Long serviceId);

    int insert(AmsPricingRule rule);

    int update(AmsPricingRule rule);

    int deleteById(Long id);
    
    AmsPricingRule selectById(Long id);
}
