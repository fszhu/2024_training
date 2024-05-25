package com.winter.controller;

import com.winter.Service.PerformanceService;
import com.winter.domain.Performance;
import com.winter.req.QueryPerformanceReq;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 控制层
 * */
@RestController
public class PerformanceController {

    @Autowired
    private PerformanceService performanceService;

    /**
     * 查询全部的数据
     * */
    @GetMapping("/findAll")
    public List<Performance> findAll(){
        List<Performance> performanceList = performanceService.findAll();
        return performanceList;
    }

    /**
     * 添加数据
     * */
    @PostMapping("/add")
    public void add(@RequestBody Performance performance){
        performanceService.add(performance);
    }

    /**
     * 条件查询
     * */
    @GetMapping("/findByCondition")
    public List<Performance> findByCondition(@RequestBody QueryPerformanceReq req){
        return performanceService.findByCondition(req);
    }
}
