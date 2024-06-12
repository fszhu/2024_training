package com.winter.mq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.winter.enums.MQTopicEnum;
import com.winter.resp.CommonResp;
import jakarta.annotation.Resource;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 定时查询server端日志数据是否存储成功
 * */
@Component
@RocketMQMessageListener(consumerGroup = "LOG_CONSUMPTION_RESULT", topic = "LOG_CONSUMPTION_RESULT")
public class LogStoreSuccessful implements RocketMQListener<MessageExt> {

    @Resource
    private RocketMQTemplate rocketMQTemplate;

    public static Map<String, String> store_success = new HashMap<>();

    @Override
    public void onMessage(MessageExt messageExt) {
        String msg = new String(messageExt.getBody());

        //反序列化MQ中的内容，并获取消息中书记的数据信息
        ObjectMapper mapper = new ObjectMapper();
        CommonResp resp = mapper.convertValue(msg, CommonResp.class);
        String respMsg = resp.getMessage();
        String content = (String)resp.getContent();
        if ("ok".equals(respMsg)){
            //消费成功,从集合中将此消息移除
            store_success.remove(content);
        } else {
            //消费失败，重新发送消息
            store_success.put(content, resp.getMessage());
            rocketMQTemplate.syncSend(MQTopicEnum.LOG_DATA.getCode(), content);
        }
    }
}
