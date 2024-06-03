package com.winter.controller;

import com.winter.domain.LogData;
import com.winter.factory.LogStore;
import com.winter.factory.LogStoreFactory;
import com.winter.req.LogQueryReq;
import com.winter.resp.CommonResp;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 监听上传的日志信息的controller
 * 由于消息的监听使用了MQ，所以上传接口"/log/upload"没有在控制层实现，而是放在mq包中实现
 *
 * 所以在次数，只提供查询接口
 * */
@RestController
@RequestMapping("/log")
public class LogDataController{

    /**
     * 日志查询接口
     * */
    @GetMapping("/query")
    public CommonResp query(LogQueryReq logQueryReq){
        CommonResp resp = new CommonResp();
        //应该查询三种方式存储的日志全部的结果
        LogStore mysql = LogStoreFactory.getStorageMethod("mysql");
        List<LogData> logData = mysql.queryData(logQueryReq);
        System.out.println(logData);
        return resp;
    }
}
