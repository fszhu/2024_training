package com.winter.controller;

import com.winter.domain.Performance;
import com.winter.mapper.PerformanceMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class PerformanceController {

    @Autowired
    private PerformanceMapper performanceMapper;

    @GetMapping("/findAll")
    public List<Performance> findAll(){
        List<Performance> performanceList = performanceMapper.findAll();
        return performanceList;
    }
}
