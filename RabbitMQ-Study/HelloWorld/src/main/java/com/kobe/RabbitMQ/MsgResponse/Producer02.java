package com.kobe.RabbitMQ.MsgResponse;

import com.kobe.RabbitMQ.Utils.RabbitUtils;
import com.rabbitmq.client.Channel;

import java.util.Scanner;

/**
 * @description: 消息手动应答时不丢失，放回队列中重新消费
 * @author: Allen_Kobe
 * @create: 2022-04-14 12:31
 **/
public class Producer02 {
    public static final String ACK_QUEUE_NAME = "ack_queue";
    public static void main(String[] args) throws Exception {
        Channel channel = RabbitUtils.getChannel();
        channel.queueDeclare(ACK_QUEUE_NAME,false,false,false,null);
        //从控制台当中接收消息
        Scanner scanner = new Scanner(System.in);
        while(scanner.hasNext()){
            String msg = scanner.next();
            channel.basicPublish("",ACK_QUEUE_NAME,null,msg.getBytes("UTF-8"));
            System.out.println("生产者发出消息："+msg);
        }
    }
}
