package com.winter.controller;

import com.winter.Service.PerformanceService;
import com.winter.domain.Performance;
import com.winter.enums.StatusEnum;
import com.winter.req.QueryPerformanceReq;
import com.winter.req.UploadReq;
import com.winter.resp.CommonResp;
import com.winter.resp.Data;
import com.winter.resp.Values;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 控制层
 * */
@RestController
@RequestMapping("/metric")
public class PerformanceController {

    @Autowired
    private PerformanceService performanceService;

    /**
     * 添加数据
     * 此接口的主要用于接受上报的数据
     * */
    @PostMapping("/upload")
    public CommonResp add(@RequestBody UploadReq[] uploadReqs){
        //将UploadReq封装为Performance类
        Performance performance = new Performance();
        performance.setHostname(uploadReqs[0].getEndpoint());  //设置主机名称
        performance.setTimestamp(uploadReqs[0].getTimestamp());  //设置采集时间
        performance.setStep(uploadReqs[0].getStep());  //设置采集步长
        for (int i = 0; i < uploadReqs.length; i++){  //设置具体采集到的值
            if ("cpu.used.percent".equals(uploadReqs[i].getMetric())){
                performance.setCpu_usage(uploadReqs[i].getValue());
            }
            if ("mem.used.percent".equals(uploadReqs[i].getMetric())){
                performance.setMem_usage(uploadReqs[i].getValue());
            }
        }

        //调用service层方法，封装返回值内容
        CommonResp resp = new CommonResp();
        try {
            performanceService.add(performance);  //调用service层，在service完成Performance主键的设置
            resp.setCode(StatusEnum.UPLOAD_SUCCESS.getCode());
            resp.setMessage("ok");
            resp.setContent(null);
        } catch (Exception e){  //如果捕捉到异常
            String message = e.getMessage();  //获取报错的信息
            resp.setCode(StatusEnum.UPLOAD_FAIL.getCode());
            resp.setMessage(message);
            resp.setContent(null);
        }
        return resp;
    }

    /**
     * 条件查询
     * 主要给用户端查询使用
     * */
    @GetMapping("/query")
    public CommonResp findByCondition(@RequestBody QueryPerformanceReq req){
        CommonResp resp = new CommonResp();
        try {
            //请求成功
            List<Performance> performanceList = performanceService.findByCondition(req);

            //对查询得到的结果进行封装,init
            Data[] data = new Data[2];  //两个指标cpu和mem
            data[0] = new Data();
            data[1] = new Data();

            data[0].setMetric("cpu.used.percent");
            data[1].setMetric("mem.used.percent");
            //这里注意多线程情况下ArrayList的线程安全问题
            List<Values> cpu_vals = new ArrayList<>();  //cpu相关的指标
            List<Values> mem_vals = new ArrayList<>();  //mem相关指标
            data[0].setValues(cpu_vals);
            data[1].setValues(mem_vals);

            for (Performance p : performanceList){
                //cpu指标集合：两个指标，采集时间和具体的值
                if (p.getCpu_usage() != null){
                    Values v = new Values();
                    v.setTimestamp(p.getTimestamp());
                    v.setValue(p.getCpu_usage());
                    cpu_vals.add(v);  //添加到集合中
                }

                //mem指标集合
                if (p.getMem_usage() != null){
                    Values v = new Values();
                    v.setTimestamp(p.getTimestamp());
                    v.setValue(p.getMem_usage());
                    mem_vals.add(v);
                }
            }

            //封装resp
            resp.setCode(StatusEnum.QUERY_SUCCESS.getCode());
            resp.setContent(data);
            resp.setMessage("ok");
        } catch (Exception e){
            //请求失败
            String message = e.getMessage();
            resp.setCode(StatusEnum.QUERY_FAIL.getCode());
            resp.setMessage(message);
            resp.setContent(null);
        }
        return resp;
    }

    /**
     * 查询全部的数据
     * */
    @GetMapping("/findAll")
    public CommonResp findAll(){
        CommonResp resp = new CommonResp();
        try {
            //查询成功
            List<Performance> performanceList = performanceService.findAll();
            resp.setCode(StatusEnum.QUERY_SUCCESS.getCode());
            resp.setContent(performanceList);
            resp.setMessage("ok");
        } catch (Exception e){
            //查询失败
            String message = e.getMessage();
            resp.setCode(StatusEnum.QUERY_FAIL.getCode());
            resp.setMessage(message);
            resp.setContent(null);
        }
        return resp;
    }
}
