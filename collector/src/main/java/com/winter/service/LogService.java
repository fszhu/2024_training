package com.winter.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.winter.config.CFGConfig;
import com.winter.enums.StatusEnum;
import com.winter.mq.LogStoreSuccessful;
import com.winter.req.LogUploadReq;
import com.winter.enums.MQTopicEnum;
import com.winter.resp.CommonResp;
import com.winter.util.SystemUtil;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

/**
 * 日志文件相关业务
 *
 * 坑点：@Value注解是在构造方法之后执行的,直接@Value注解，然后在构造方法中写逻辑代码可能会出现一定的问题
 * */
@Service
public class LogService {
    private String HOSTNAME;  //计算机主机名

    private String cfgConfigPath;  //配置文件的路径

    @Resource
    private ThreadPoolTaskExecutor taskExecutor;

    private CFGConfig cfgConfig;  //日志文件配置对应的类

    private HashMap<String, Long> lastKnownPositionMap;  //所有需要监听的日志文件上一次的读取位置

    @Resource
    private RocketMQTemplate rocketMQTemplate;


    //无参构造方法，初始化属性的值
    public LogService(){
        //获取计算机主机名
        if (HOSTNAME == null){
            HOSTNAME = SystemUtil.getHostname();
        }

        try {
            //读取application.properties文件，得到cfg.json配置文件的路径
            //初始化一个Properties的类
            Properties properties = new Properties();
            //使用ClassLoader加载properties配置文件生成对应的输入流
            InputStream in = LogService.class.getClassLoader().getResourceAsStream("application.properties");
            //使用Properties对象加载输入流
            properties.load(in);
            //获取cfg.json的文件路径
            cfgConfigPath = properties.getProperty("cfgConfig.file.path");
            in.close();

            //解析cfg.json配置文件
            //根据路径名获取相应的file类
            org.springframework.core.io.Resource resource = new ClassPathResource(cfgConfigPath);
            InputStream resourceInputStream = resource.getInputStream();

            //json文件序列化，初始化CFGConfig类
            ObjectMapper objectMapper = new ObjectMapper();
            cfgConfig = objectMapper.readValue(resourceInputStream, CFGConfig.class);

        } catch (Exception e){
            e.printStackTrace();
        }

        //初始化各个文件指针的位置为0
        lastKnownPositionMap = new HashMap<>();
        for (String f : cfgConfig.getFiles()){
            lastKnownPositionMap.put(f, 0L);  //初始化所有文件指针为0
        }

    }

    /**
     * 执行完构造方法后立即执行该方法
     * */
    @PostConstruct
    public void startLogWatch(){
        for (String p : cfgConfig.getFiles()){
            Path path = Paths.get(p);
            taskExecutor.execute(()->watchLogFile(path));  //将任务加到线程池
        }
    }

    public void watchLogFile(Path path){
        try {
            //创建watchService
            WatchService watchService = FileSystems.getDefault().newWatchService();

            //将所有要监听的日志文件的父目录注册到watchService和要监听的事件的类型, 即path.getParent()
            path.getParent().register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);

            System.out.println("Watching for " + path.toString() + " changes...");

            //无线循环监听事件
            while (true) {
                WatchKey key;
                try {
                    //watch.poll来等待事件的发生
                    key = watchService.take();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }

                //key.pollEvents()返回所有的事件
                for (WatchEvent<?> event : key.pollEvents()) {
                    //获取事件的类型，每个WatchEvent都包含一个kind属性，该属性表示事件的类型
                    //常见的有 ENTRY_CREATE（文件创建）、ENTRY_DELETE（文件删除）和 ENTRY_MODIFY（文件修改）
                    WatchEvent.Kind<?> kind = event.kind();

                    //处理overflow事件，这意味着某些事件可能丢失或者无法处理
                    //通常情况下可以忽略此事件，并继续处理其它事件
                    if (kind == StandardWatchEventKinds.OVERFLOW) {
                        continue;
                    }

                    //获取文件名
                    //将event转换为WatchEvent<Path> 类型，并使用 ev.context() 获取与事件相关的文件名。
                    WatchEvent<Path> ev = (WatchEvent<Path>) event;
                    //context() 方法返回的是相对路径，因此需要与监视的目录路径结合使用
                    Path fileName = ev.context();
                    if (fileName.toString().equals(path.getFileName().toString())) {
                        System.out.println("File " + path.getFileName() + " has been modified.");
                        // 读取文件新增的内容，并将其上传到server
                        readNewContentAndUpload(path.resolve(path.toString()));
                    }
                }

                //重置watchKey, 以便接受更多的事件
                boolean valid = key.reset();
                if (!valid) {
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    /**
     * 读取文件的新增内容，并将其上传到sever
     * */
    public CommonResp readNewContentAndUpload(Path filePath){
        //返回结果
        CommonResp commonResp = new CommonResp();
        try {
            RandomAccessFile file = new RandomAccessFile(filePath.toFile(), "r");
            //获取对应的文件指针
            Long lastKnownPosition = lastKnownPositionMap.get(filePath.toString());
            file.seek(lastKnownPosition);  //从上次读取的位置开始
            String line;
            List<String> logs = new ArrayList<>();  //创建一个集合，存储日志新增的内容
            //读取新增的内容，将其添加到集合
            while((line = file.readLine()) != null){
                // 忽略空行（仅包含回车或换行符的行）
                if (!line.trim().isEmpty()) {
                    logs.add(line);
                }
            }
            lastKnownPosition = file.getFilePointer();  //更新文件指针的位置
            lastKnownPositionMap.put(filePath.toString(), lastKnownPosition); //更新后的值加入集合
            //如果新增的内容不为空，则将其封装为LogUploadReq，上传日志到server
            if (logs.size() > 0){
                LogUploadReq logUploadReq = new LogUploadReq();
                logUploadReq.setHostname(HOSTNAME);  //设置主机名
                logUploadReq.setFile(filePath.toString());  //被监听的日志文件的全路径
                logUploadReq.setLogs(logs);  //采集的日志文件的内容,这里logs的类型为List

                //调用server，上报日志
                System.out.println("上报日志数据到消息队列：" + logUploadReq);
                //将封装好的消息发动到消息队列
                SendResult sendResult = rocketMQTemplate.syncSend(MQTopicEnum.LOG_DATA.getCode(), logUploadReq);
                System.out.println("数据上报消息队列的状态" + sendResult.getSendStatus());

                //查询结果，日志数据是否到服务端保存成功，这里连续查询100次
                String s = null;
                for (int i = 0; i < 100; i++){
                    s = LogStoreSuccessful.store_success.get(logUploadReq.toString());
                    if (s != null) break;
                }
                if (s == null){
                    //日志到server端被成功存储
                    commonResp.setCode(StatusEnum.UPLOAD_SUCCESS.getCode());
                    commonResp.setMessage("ok");
                } else {
                    //日志到server端没有成功存储
                    commonResp.setCode(StatusEnum.UPLOAD_FAIL.getCode());
                    commonResp.setMessage(s);
                }
            }
        } catch (Exception e) {
            //这里的逻辑还有一定的问题，”发生异常似乎也可能日志存储成功“
            commonResp.setCode(StatusEnum.UPLOAD_FAIL.getCode());
            commonResp.setMessage(e.getMessage());
            return commonResp;
        }
        return commonResp;
    }

}
