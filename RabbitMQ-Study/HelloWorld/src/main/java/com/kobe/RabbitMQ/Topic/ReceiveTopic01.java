package com.kobe.RabbitMQ.Topic;

import com.kobe.RabbitMQ.Utils.RabbitUtils;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;

/**
 * @description: 消息接收
 * @author: Allen_Kobe
 * @create: 2022-04-14 22:40
 **/
public class ReceiveTopic01 {
    public static final String EXCHANGE_NAME = "topic_logs";

    public static void main(String[] args) throws Exception{
        Channel channel = RabbitUtils.getChannel();
        channel.exchangeDeclare(EXCHANGE_NAME, "topic");
        //声明队列
        String quequeName = "Q1";
        channel.queueDeclare(quequeName,false,false,false,null);
        channel.queueBind(quequeName,EXCHANGE_NAME,"*.orange.*");
        System.out.println("等待接收消息。。。。");
        DeliverCallback deliverCallback = (comsumerTag, message)->{
            System.out.println("ReceiveLogsDirect01控制台打印接收到的消息："+new String(message.getBody(),"UTF-8"));
            System.out.println("接收队列:"+quequeName+" 绑定建: "+message.getEnvelope().getRoutingKey());
        };
        channel.basicConsume(quequeName,true,deliverCallback,comsumerTaf->{});
    }
}
