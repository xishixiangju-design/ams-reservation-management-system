package com.trae.ams.mapper;

import com.trae.ams.entity.AmsComboRule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AmsComboRuleMapper {
    List<AmsComboRule> selectActiveRules(@Param("storeId") Long storeId);
}
