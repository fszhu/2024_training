package com.winter.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CollectorController {

    /**
     * 主动查询接口
     *  * */
    @GetMapping("/queryCPU")
    public String queryCPU(){
        return "hello !!!";
    }
}
