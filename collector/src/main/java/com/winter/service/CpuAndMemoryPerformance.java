package com.winter.service;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.lang.management.ManagementFactory;
import com.sun.management.OperatingSystemMXBean;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


/**
 * 该类需要定时采集数据，实现quartz的Job类
 * */

@DisallowConcurrentExecution  //禁止任务并发执行
public class CpuAndMemoryPerformance implements Job {


    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        System.out.println("我是一个定时调度任务！！");

        //打印当前的时间
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedNow  = LocalDateTime.now().format(formatter);

        //cpu使用率和内存使用率
        OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
        double cpuLoad = osBean.getCpuLoad();
        System.out.println("cpu使用率：" + cpuLoad);


        System.out.println("\n========================================\n");
        System.out.println("采集时间：" + formattedNow);
        System.out.println("\n========================================\n");

    }
}
