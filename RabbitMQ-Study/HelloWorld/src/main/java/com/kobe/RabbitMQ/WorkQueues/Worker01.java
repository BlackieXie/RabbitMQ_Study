package com.kobe.RabbitMQ.WorkQueues;

import com.kobe.RabbitMQ.Utils.RabbitUtils;
import com.rabbitmq.client.CancelCallback;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;

/**
 * @description: 工作线程1
 * @author: Allen_Kobe
 * @create: 2022-04-14 11:44
 **/
public class Worker01 {
    public static final String QUEUE_NAME = "hello";

    public static void main(String[] args) throws Exception{
        Channel channel = RabbitUtils.getChannel();
        DeliverCallback deliverCallback = (comsumerTag,message)->{
            System.out.println("接收到的消息："+new String(message.getBody()));
        };
        CancelCallback cancelCallback = (comsumerTag)->{
            System.out.println("消息者取消消费接口回调逻辑");
        };
        System.out.println("C2等待接收消息.....");
        channel.basicConsume(QUEUE_NAME,true,deliverCallback,cancelCallback);
    }
}
