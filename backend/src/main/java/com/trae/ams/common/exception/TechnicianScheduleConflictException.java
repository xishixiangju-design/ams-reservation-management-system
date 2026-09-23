package com.trae.ams.common.exception;

import com.trae.ams.dto.technician.TechnicianConflictDTO;
import lombok.Getter;

import java.util.List;

/**
 * 技师日程冲突异常
 * 
 * 功能说明：
 * 当删除或修改技师信息时，如果该技师仍有时间冲突的预约，
 * 则抛出此异常，并携带冲突预约的详细信息列表。
 * 
 * 使用场景：
 * - 删除技师时，该技师仍有未完成的预约
 * - 修改技师排班时，与新排班时间存在冲突的预约
 * 
 * 错误码409表示HTTP冲突状态
 */
@Getter
public class TechnicianScheduleConflictException extends BusinessException {

    /** 冲突预约信息列表，包含冲突预约的详细数据 */
    private final List<TechnicianConflictDTO> conflictList;

    /**
     * 构造方法
     * 
     * @param conflictList 冲突的预约信息列表
     */
    public TechnicianScheduleConflictException(List<TechnicianConflictDTO> conflictList) {
        super(409, "技师日程冲突，请先处理以下预约");
        this.conflictList = conflictList;
    }
}
