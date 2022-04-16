package com.kobe.RabbitMQ.Fanout;

import com.kobe.RabbitMQ.Utils.RabbitUtils;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;

import java.util.Scanner;

/**
 * @description: 消息发送 交换机
 * @author: Allen_Kobe
 * @create: 2022-04-14 21:03
 **/
public class EmitLog {
    //交换机名称
    public static final String EXCHANGE_NAME = "logs";
    public static void main(String[] args) throws Exception{
        Channel channel = RabbitUtils.getChannel();
        //声明一个交换机
//        channel.exchangeDeclare(EXCHANGE_NAME,"fanout");
        Scanner scanner = new Scanner(System.in);
        while(scanner.hasNext()){
            String msg = scanner.next();
            channel.basicPublish(EXCHANGE_NAME,"",null,msg.getBytes("UTF-8"));
            System.out.println("生产者发出消息："+msg);
        }
        //声明一个队列 临时队列
        /**
         * 生成一个临时队列，队列名称随机
         * 当消费者断开与队列的连接的时候 队列就自动删除
         */
        String queueName = channel.queueDeclare().getQueue();
        /**
         * 绑定交换机与队列
         */
        channel.queueBind(queueName,EXCHANGE_NAME,"");
        System.out.println("等待接收消息，把接收到的消息打印在屏幕上");
        DeliverCallback deliverCallback = (comsumerTag, message)->{
            System.out.println("ReceiveLogs01控制台打印接收到的消息："+new String(message.getBody(),"UTF-8"));
        };
        channel.basicConsume(queueName,true,deliverCallback,comsumerTaf->{});
    }
}
