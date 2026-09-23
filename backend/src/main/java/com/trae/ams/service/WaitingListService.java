package com.trae.ams.service;

import com.github.pagehelper.PageInfo;
import com.trae.ams.dto.waitinglist.JoinWaitlistDTO;
import com.trae.ams.dto.waitinglist.WaitingListDTO;
import com.trae.ams.dto.waitinglist.WaitingListQuery;
import com.trae.ams.entity.AmsWaitingList;

import java.time.LocalDateTime;
import java.util.List;

public interface WaitingListService {
    /**
     * 加入候补队列
     */
    void joinWaitlist(JoinWaitlistDTO request);

    /**
     * 检查候补队列并尝试通知或转正
     * 当有预约取消释放资源时调用
     */
    void checkWaitlistOnCancel(Long storeId, LocalDateTime start, LocalDateTime end);

    /**
     * 获取当前用户的候补列表
     */
    List<WaitingListDTO> getMyWaitlist();

    /**
     * 管理端查询候补列表
     */
    PageInfo<WaitingListDTO> getWaitingList(Long storeId, WaitingListQuery query);

    /**
     * 候补转正
     */
    void convert(Long waitingListId);
}
