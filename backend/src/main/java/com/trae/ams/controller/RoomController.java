package com.trae.ams.controller;

import com.trae.ams.common.result.Result;
import com.trae.ams.dto.room.RoomDTO;
import com.trae.ams.entity.AmsRoom;
import com.trae.ams.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rooms")
public class RoomController {

    @Autowired
    private RoomService roomService;

    @PostMapping
    public Result<Void> create(@RequestBody RoomDTO dto) {
        roomService.createRoom(dto);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody RoomDTO dto) {
        roomService.updateRoom(id, dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        roomService.deleteRoom(id);
        return Result.success();
    }

    @GetMapping("/{id}")
    public Result<AmsRoom> get(@PathVariable Long id) {
        return Result.success(roomService.getRoom(id));
    }

    @GetMapping
    public Result<List<AmsRoom>> list(
            @RequestParam(required = false) Long storeId,
            @RequestParam(required = false) Integer status) {
        return Result.success(roomService.listRooms(storeId, status));
    }
}
