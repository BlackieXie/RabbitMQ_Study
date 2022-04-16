package com.kobe.RabbitMQ.DeadQueue;

import com.kobe.RabbitMQ.Utils.RabbitUtils;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;

import java.util.HashMap;
import java.util.Map;

/**
 * @description: 消息接收
 * @author: Allen_Kobe
 * @create: 2022-04-15 12:31
 **/
public class Consumer02 {
    //死信队列名称
    public static final String DEAD_QUEUE = "dead_queue";

    public static void main(String[] args) throws Exception{
        Channel channel = RabbitUtils.getChannel();
        System.out.println("等待接收消息......");
        DeliverCallback deliverCallback = (comsumerTag, message)->{
            System.out.println("Consumer02控制台打印接收到的消息："+new String(message.getBody(),"UTF-8"));
        };
        channel.basicConsume(DEAD_QUEUE,true,deliverCallback,comsumerTag->{});
    }
}
