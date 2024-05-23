package com.winter.config;

import com.winter.service.CpuAndMemoryPerformance;
import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 使用quartz定时调度框架完成定时调度的策略，spring自带的定时任务不适合分布式的应用，
 * 切不支持定时时间的修改
 *
 * 这是定时任务的配置类
 * */

@Configuration
public class QuartzConfig {

    /**
     * 声名一个任务
     * */
    @Bean
    public JobDetail jobDetail(){
        return JobBuilder.newJob(CpuAndMemoryPerformance.class)
                .withIdentity("CpuAndMemoryPerformance", "XiaoMi")
                .storeDurably()
                .build();
    }

    /**
     * 声名一个触发器，设置执行的时机
     * */
    @Bean
    public Trigger trigger(){
        return TriggerBuilder.newTrigger()
                .forJob(jobDetail())
                .withIdentity("trigger", "trigger")
                .startNow()
                .withSchedule(CronScheduleBuilder.cronSchedule("*/2 * * * * ?"))
                .build();
    }
}
