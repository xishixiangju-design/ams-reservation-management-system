package com.trae.ams.service;

import com.trae.ams.dto.room.RoomDTO;
import com.trae.ams.entity.AmsRoom;

import java.util.List;

public interface RoomService {
    /**
     * 创建房间
     */
    void createRoom(RoomDTO dto);

    /**
     * 更新房间
     */
    void updateRoom(Long id, RoomDTO dto);

    /**
     * 删除房间
     */
    void deleteRoom(Long id);

    /**
     * 获取房间详情
     */
    AmsRoom getRoom(Long id);

    /**
     * 获取房间列表
     */
    List<AmsRoom> listRooms(Long storeId, Integer status);
}
