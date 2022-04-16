package com.kobe.RabbitMQ.WorkQueues;

import com.kobe.RabbitMQ.Utils.RabbitUtils;
import com.rabbitmq.client.Channel;

import java.util.Scanner;

/**
 * @description: WorkQueues案例中的生产者
 * @author: Allen_Kobe
 * @create: 2022-04-14 11:54
 **/
public class Producer01 {
    public static final String QUEUE_NAME = "hello";
    public static void main(String[] args) throws Exception {
        Channel channel = RabbitUtils.getChannel();
        channel.queueDeclare(QUEUE_NAME,false,false,false,null);
        //从控制台当中接收消息
        Scanner scanner = new Scanner(System.in);
        while(scanner.hasNext()){
            String msg = scanner.next();
            channel.basicPublish("",QUEUE_NAME,null,msg.getBytes());
            System.out.println("发送消息完成："+msg);
        }
    }
}
