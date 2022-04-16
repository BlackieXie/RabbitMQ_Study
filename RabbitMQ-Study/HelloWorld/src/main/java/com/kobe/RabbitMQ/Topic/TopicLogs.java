package com.kobe.RabbitMQ.Topic;

import com.kobe.RabbitMQ.Utils.RabbitUtils;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;

import java.util.HashMap;
import java.util.Map;

/**
 * @description: 消息生产
 * @author: Allen_Kobe
 * @create: 2022-04-15 11:02
 **/
public class TopicLogs {
    public static final String EXCHANGE_NAME = "topic_logs";
    public static void main(String[] args) throws Exception{
        Channel channel = RabbitUtils.getChannel();
        channel.exchangeDeclare(EXCHANGE_NAME, "topic");
        Map<String,String> bindingKeyMap = new HashMap<>();
        bindingKeyMap.put("quick.orange.rabbit","被队列 Q1Q2 接收到");
        bindingKeyMap.put("lazy.orange.elephant","被队列 Q1Q2 接收到");
        bindingKeyMap.put("quick.orange.fox","被队列 Q1 接收到");
        bindingKeyMap.put("lazy.brown.fox","被队列 Q2 接收到");
        bindingKeyMap.put("lazy.pink.rabbit","虽然满足两个绑定但只被队列 Q2 接收一次");
        bindingKeyMap.put("quick.brown.fox","不匹配任何绑定不会被任何队列接收到会被丢弃");
        bindingKeyMap.put("quick.orange.male.rabbit","是四个单词不匹配任何绑定会被丢弃");
        bindingKeyMap.put("lazy.orange.male.rabbit","是四个单词但匹配 Q2");
        for(Map.Entry<String,String> stringEntry:bindingKeyMap.entrySet()){
            String routingKey = stringEntry.getKey();
            String msg = stringEntry.getValue();
            channel.basicPublish(EXCHANGE_NAME,routingKey,null,msg.getBytes("UTF-8"));
            System.out.println("生产者发出消息:"+msg);
        }
    }
}
