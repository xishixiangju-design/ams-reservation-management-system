package com.trae.ams.service;

import com.trae.ams.dto.room.RoomDTO;
import com.trae.ams.entity.AmsRoom;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@SpringBootTest
@Transactional
public class RoomServiceTest {

    @Autowired
    private RoomService roomService;

    @Test
    public void testRoomCrud() {
        // 1. Create
        RoomDTO dto = new RoomDTO();
        dto.setName("101");
        dto.setType("SINGLE");
        dto.setStatus(1);
        
        roomService.createRoom(dto);

        // 2. List
        List<AmsRoom> list = roomService.listRooms(1L, 1);
        Assertions.assertFalse(list.isEmpty());
        AmsRoom room = list.get(0);
        Assertions.assertEquals("101", room.getName());
        Assertions.assertEquals(1, room.getCapacity()); // Default capacity for SINGLE

        // 3. Update
        RoomDTO updateDto = new RoomDTO();
        updateDto.setName("101 VIP");
        roomService.updateRoom(room.getId(), updateDto);
        
        AmsRoom updated = roomService.getRoom(room.getId());
        Assertions.assertEquals("101 VIP", updated.getName());
    }
}
