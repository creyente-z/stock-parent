package com.ming.stock.controller;

import com.ming.stock.service.LogService;
import com.ming.stock.vo.req.LogPageReqVo;
import com.ming.stock.vo.resp.PageResult;
import com.ming.stock.vo.resp.R;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

/**
 * @author by Ming
 * @Description
 */
@RestController
@RequestMapping("/api")
public class LogController {

    @Autowired
    private LogService logService;

    /**
     * 日志信息综合查询
     * @param vo
     * @return
     */
    @PostMapping("/logs")
    public R<PageResult> logPageQuery(@RequestBody LogPageReqVo vo){
        return logService.logPageQuery(vo);
    }

    /**
     * 批量删除日志信息功能
     * @param logIds
     * @return
     */
    @DeleteMapping("/log")
    @PreAuthorize("hasAuthority('')")
    public R<String> deleteBatch(@RequestBody List<Long> logIds){
        return this.logService.deleteBatch(logIds);
    }


//    @PostMapping("/create")
//    public  R addNewReservation (@RequestBody ReservationDomain reservationDomain ){
//    }
//
//    @GetMapping("/todayConferenceRoomAll/{time}")
//    public R selectTodayConferenceRoomAll (@PathVariable String time){
//    }
//
//    @GetMapping("/detectConflict")
//    public R detectConflict(@RequestBody ReservationDomainDto  reservationDomainDto){
//    }



}