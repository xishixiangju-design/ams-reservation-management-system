package com.trae.ams.controller;

import com.github.pagehelper.PageInfo;
import com.trae.ams.common.context.UserContext;
import com.trae.ams.common.result.Result;
import com.trae.ams.dto.waitinglist.JoinWaitlistDTO;
import com.trae.ams.dto.waitinglist.WaitingListDTO;
import com.trae.ams.dto.waitinglist.WaitingListQuery;
import com.trae.ams.entity.AmsWaitingList;
import com.trae.ams.service.WaitingListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/waiting-list")
public class WaitingListController {

    @Autowired
    private WaitingListService waitingListService;

    @PostMapping("/join")
    public Result<Void> joinWaitlist(@RequestBody JoinWaitlistDTO request) {
        waitingListService.joinWaitlist(request);
        return Result.success();
    }

    @GetMapping("/me")
    public Result<List<WaitingListDTO>> getMyWaitlist() {
        return Result.success(waitingListService.getMyWaitlist());
    }

    @PostMapping("/{id}/convert")
    public Result<Void> convert(@PathVariable Long id) {
        waitingListService.convert(id);
        return Result.success();
    }

    @GetMapping("/admin/list")
    public Result<PageInfo<WaitingListDTO>> listAdmin(WaitingListQuery query) {
        Long storeId = UserContext.getStoreIdOrDefault();
        return Result.success(waitingListService.getWaitingList(storeId, query));
    }
}
