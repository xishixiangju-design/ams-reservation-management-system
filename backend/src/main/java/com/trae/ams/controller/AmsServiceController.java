package com.trae.ams.controller;

import com.trae.ams.common.result.Result;
import com.trae.ams.dto.service.ServiceDTO;
import com.trae.ams.entity.AmsService;
import com.trae.ams.service.AmsServiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/services")
public class AmsServiceController {

    @Autowired
    private AmsServiceService amsServiceService;

    @PostMapping
    public Result<Void> create(@RequestBody ServiceDTO dto) {
        amsServiceService.createService(dto);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody ServiceDTO dto) {
        amsServiceService.updateService(id, dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        amsServiceService.deleteService(id);
        return Result.success();
    }

    @GetMapping("/{id}")
    public Result<AmsService> get(@PathVariable Long id) {
        return Result.success(amsServiceService.getService(id));
    }

    @GetMapping
    public Result<List<AmsService>> list(
            @RequestParam(required = false) Long storeId,
            @RequestParam(required = false) Integer status) {
        return Result.success(amsServiceService.listServices(storeId, status));
    }
}
