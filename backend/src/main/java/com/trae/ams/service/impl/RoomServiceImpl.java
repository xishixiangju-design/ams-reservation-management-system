package com.trae.ams.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.trae.ams.common.context.UserContext;
import com.trae.ams.common.exception.BusinessException;
import com.trae.ams.dto.room.RoomDTO;
import com.trae.ams.entity.AmsRoom;
import com.trae.ams.mapper.AmsRoomMapper;
import com.trae.ams.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RoomServiceImpl implements RoomService {

    @Autowired
    private AmsRoomMapper amsRoomMapper;

    @Autowired
    private com.trae.ams.mapper.AmsAppointmentItemMapper appointmentItemMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createRoom(RoomDTO dto) {
        AmsRoom room = new AmsRoom();
        BeanUtil.copyProperties(dto, room);
        
        // 默认值
        room.setStoreId(UserContext.getStoreIdOrDefault()); 
        if (room.getStatus() == null) {
            room.setStatus(1);
        }
        if (room.getCapacity() == null) {
            // 根据类型设置默认容量
            if ("SINGLE".equalsIgnoreCase(room.getType())) {
                room.setCapacity(1);
            } else if ("DOUBLE".equalsIgnoreCase(room.getType())) {
                room.setCapacity(2);
            } else {
                room.setCapacity(1);
            }
        }

        amsRoomMapper.insert(room);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRoom(Long id, RoomDTO dto) {
        AmsRoom exist = amsRoomMapper.selectById(id);
        if (exist == null) {
            throw new BusinessException("房间不存在");
        }
        
        AmsRoom update = new AmsRoom();
        BeanUtil.copyProperties(dto, update);
        update.setId(id);
        
        amsRoomMapper.update(update);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRoom(Long id) {
        AmsRoom exist = amsRoomMapper.selectById(id);
        if (exist == null) {
            throw new BusinessException("房间不存在");
        }
        
        // 检查是否有关联的预约
        if (appointmentItemMapper.countByRoomId(id) > 0) {
            throw new BusinessException("该房间已被预约使用，无法删除");
        }

        amsRoomMapper.deleteById(id);
    }

    @Override
    public AmsRoom getRoom(Long id) {
        return amsRoomMapper.selectById(id);
    }

    @Override
    public List<AmsRoom> listRooms(Long storeId, Integer status) {
        return amsRoomMapper.selectList(storeId, status);
    }
}
