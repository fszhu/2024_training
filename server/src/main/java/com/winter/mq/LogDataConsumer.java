package com.winter.mq;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.winter.domain.LogData;
import com.winter.enums.MQTopicEnum;
import com.winter.enums.StatusEnum;
import com.winter.factory.LogStore;
import com.winter.factory.LogStoreFactory;
import com.winter.req.LogUploadReq;
import com.winter.resp.CommonResp;
import com.winter.utils.SnowUtil;
import jakarta.annotation.Resource;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

/**
 * 监听消息队列中的日志数据
 * 消费的组为default，主题为LOG_DATA
 *
 * 具体的存储方式由存储工厂实现
 * */
@Service
@RocketMQMessageListener(consumerGroup = "data_consumer", topic = "LOG_DATA")
public class LogDataConsumer implements RocketMQListener<MessageExt> {

    @Resource
    private RocketMQTemplate rocketMQTemplate;

    @Override
    public void onMessage(MessageExt messageExt) {
        byte[] body = messageExt.getBody();
        String log_data = new String(body);
        System.out.println("消费消息队列中的数据：" + log_data);

        CommonResp resp = new CommonResp();
        //将json格式的字符串，转换为相应的对象LogUploadReq
        ObjectMapper objectMapper = new ObjectMapper();
        LogUploadReq data = null;
        try {
            data = objectMapper.readValue(log_data, LogUploadReq.class);
            System.out.println("反序列化消息队列中的数据" + data);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        //获取存储方式
        String store = LogStorageConsumer.getStorage();
        System.out.println("当前的存储方式：" + store);

        try {
            //根据存储方式返回具体的日志存储方案
            LogStore storageMethod = LogStoreFactory.getStorageMethod(store);

            //封装LogData,LogData的logs类型为String类型
            LogData logData = new LogData();
            logData.setId(SnowUtil.getSnowflakeNextIdStr());
            logData.setHostname(data.getHostname());
            logData.setFile(data.getFile());
            logData.setLogs(data.getLogs().toString());  //List转String

            //调用存LogStore的存储方案，存储数据
            storageMethod.storeData(logData);

        } catch (Exception e){
            //数据上传失败
            resp.setCode(StatusEnum.UPLOAD_FAIL.getCode());
            resp.setMessage(e.getMessage());
            resp.setContent(data);
        }

        //数据上传成功
        resp.setCode(StatusEnum.UPLOAD_SUCCESS.getCode());
        resp.setMessage("ok");

        //将结果发送到另外一个消息队列
        rocketMQTemplate.syncSend(MQTopicEnum.LOG_CONSUMPTION_RESULT.getCode(), resp);
    }
}
