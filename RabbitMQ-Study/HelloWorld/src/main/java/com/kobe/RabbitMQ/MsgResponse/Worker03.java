package com.kobe.RabbitMQ.MsgResponse;

import com.kobe.RabbitMQ.Utils.RabbitUtils;
import com.kobe.RabbitMQ.Utils.SleepUtils;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;

/**
 * @description: 消费者
 * @author: Allen_Kobe
 * @create: 2022-04-14 15:17
 **/
public class Worker03 {
    public static final String ACK_QUEUE_NAME = "ack_queue";
    public static void main(String[] args) throws Exception{
        Channel channel = RabbitUtils.getChannel();
        System.out.println("C2等待接收消息处理时间较长.....");
        DeliverCallback deliverCallback = (comsumerTag, message)->{
            String msg = new String(message.getBody());
            SleepUtils.sleep(30);
            System.out.println("接收到的消息："+msg);
            /**
             * 1.消息标记tag
             * 2.是否批量应答未应答消息
             */
            channel.basicAck(message.getEnvelope().getDeliveryTag(),false);
        };
        //采用手动应答
        Boolean autoAck = false;
        channel.basicConsume(ACK_QUEUE_NAME,autoAck,deliverCallback,(comsumerTag->{
            System.out.println("消息者取消消费接口回调逻辑");
        }));
    }
}
