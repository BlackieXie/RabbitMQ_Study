package com.kobe.RabbitMQ.Topic;

import com.kobe.RabbitMQ.Utils.RabbitUtils;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;

/**
 * @description: 接收消息
 * @author: Allen_Kobe
 * @create: 2022-04-15 11:01
 **/
public class ReceiveTopic02 {
    public static final String EXCHANGE_NAME = "topic_logs";
    public static void main(String[] args) throws Exception{
        Channel channel = RabbitUtils.getChannel();
        channel.exchangeDeclare(EXCHANGE_NAME, "topic");
        //声明队列
        String quequeName = "Q2";
        channel.queueDeclare(quequeName,false,false,false,null);
        channel.queueBind(quequeName,EXCHANGE_NAME,"*.*.rabbit.*");
        channel.queueBind(quequeName,EXCHANGE_NAME,"lazy.#");
        System.out.println("等待接收消息。。。。");
        DeliverCallback deliverCallback = (comsumerTag, message)->{
            System.out.println("ReceiveLogsDirect01控制台打印接收到的消息："+new String(message.getBody(),"UTF-8"));
            System.out.println("接收队列:"+quequeName+" 绑定建: "+message.getEnvelope().getRoutingKey());
        };
        channel.basicConsume(quequeName,true,deliverCallback,comsumerTaf->{});
    }
}
