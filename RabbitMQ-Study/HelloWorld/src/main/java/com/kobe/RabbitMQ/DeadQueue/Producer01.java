package com.kobe.RabbitMQ.DeadQueue;

import com.kobe.RabbitMQ.Utils.RabbitUtils;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;

/**
 * @description: 消息发出
 * @author: Allen_Kobe
 * @create: 2022-04-15 11:53
 **/
public class Producer01 {
    //普通交换机的名称
    public static final String NORMAL_EXCHANGE = "normal_exchange";

    public static void main(String[] args) throws Exception{
        Channel channel = RabbitUtils.getChannel();
        channel.exchangeDeclare(NORMAL_EXCHANGE, BuiltinExchangeType.DIRECT);
        //死信消息 设置TTL时间 单位是ms  10000ms=10s
        AMQP.BasicProperties properties = new AMQP.BasicProperties().builder().expiration("10000").build();
        for (int i=1;i<11;i++){
            String msg = "info" + i;
            channel.basicPublish(NORMAL_EXCHANGE,"zhangsan",properties,msg.getBytes());
            System.out.println("生产者发送消息"+msg);
        }
    }
}
