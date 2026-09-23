package com.trae.ams.mapper;

import com.trae.ams.entity.AmsMemberLevel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface AmsMemberLevelMapper {
    List<AmsMemberLevel> selectAll();
    AmsMemberLevel selectByCode(String code);
}
