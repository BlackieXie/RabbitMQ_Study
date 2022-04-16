package com.kobe.RabbitMQ.ConfirmMsg;

import com.kobe.RabbitMQ.Utils.RabbitUtils;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConfirmCallback;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * @description:
 * 发布确认模式
 *      1、单个确认
 *      2、批量确认
 *      3、异步批量确认
 * @author: Allen_Kobe
 * @create: 2022-04-14 16:17
 **/
public class ConfirmMessage {
    //批量发消息的个数
    public static final int MSG_COUNT = 1000;
    public static void main(String[] args) throws Exception{
        //1.单个确认 发布1000个单独确认消息，耗时380ms
//        ConfirmMessage.publishMessageIndividually();
        //2.批量确认
//        ConfirmMessage.publishMessageBatch();
        //3.异步批量确认
        ConfirmMessage.publishMessageAsync();
    }

    //单个确认
    public static void publishMessageIndividually() throws Exception {
        Channel channel = RabbitUtils.getChannel();
            //队列声明
            String queueName = UUID.randomUUID().toString();
            channel.queueDeclare(queueName, true, false, false, null);
            //开启发布确认
            channel.confirmSelect();
            long begin = System.currentTimeMillis();
            for (int i = 0; i < MSG_COUNT; i++) {
                String msg = i+"";
                channel.basicPublish("", queueName, null, msg.getBytes());
                //服务端返回 false 或超时时间内未返回，生产者可以消息重发
                boolean flag = channel.waitForConfirms();
                if(flag) {
                    System.out.println("消息发送成功");
                }
                long end = System.currentTimeMillis();
                System.out.println("发布"+MSG_COUNT+"个单独确认消息，耗时"+(end-begin)+"ms");
            }
        }
        //批量确认  发布1000个批量确认消息，耗时34ms
    public static void publishMessageBatch() throws Exception {
        Channel channel = RabbitUtils.getChannel();
        String queueName = UUID.randomUUID().toString();
        channel.queueDeclare(queueName, true, false, false, null);
        channel.confirmSelect();
        long begin = System.currentTimeMillis();
        //批量确认消息大小
        int batchSize = 100;
        for (int i = 0; i < MSG_COUNT; i++) {
            String msg = i+"";
            channel.basicPublish("", queueName, null, msg.getBytes());
            //判断达到100条消息的时候，批量确认一次
            if(i%batchSize==0) {
                channel.waitForConfirms();
            }
        }
        long end = System.currentTimeMillis();
        System.out.println("发布"+MSG_COUNT+"个批量确认消息，耗时"+(end-begin)+"ms");
    }
    //异步发布确认  发布1000个异步确认消息，耗时29ms
    public static void publishMessageAsync()throws Exception{
        Channel channel = RabbitUtils.getChannel();
        String queueName = UUID.randomUUID().toString();
        channel.queueDeclare(queueName, true, false, false, null);
        channel.confirmSelect();
        long begin = System.currentTimeMillis();
        /**
         * 线程安全有序的哈希表 适用于高并发的情况
         * 1.可以将序号与消息进行关联
         * 2.可以批量删除消息 只需要知道序号
         * 3.支持高并发（多线程)
         */
        ConcurrentSkipListMap<Long,String> outstandingConfirms =
                new ConcurrentSkipListMap<>();
        //消息确认成功  回调函数
        ConfirmCallback ackCallback = (deliveryTag,multiple)->{
            if(multiple){
                // 2*.删除掉已经确认的消息 剩下的就是未确认的消息
                ConcurrentNavigableMap<Long, String> confirmed = outstandingConfirms.headMap(deliveryTag);
                confirmed.clear();
            }else{
                outstandingConfirms.remove(deliveryTag);
            }
            System.out.println("确认消息："+deliveryTag);
        };
        //消息确认失败  回调函数   1.消息标记 2.是否为批量确认
        ConfirmCallback nackCallback = (deliveryTag,multiple)->{
            // 3*.打印一下未确认的消息都有哪些
            String msg = outstandingConfirms.get(deliveryTag);
            System.out.println("未确认的消息是："+msg+"::::未确认的消息tag:"+deliveryTag);
        };
        //准备消息的监听器 1 监听哪些消息成功了 2哪些消息失败了
        channel.addConfirmListener(ackCallback,nackCallback);
        for (int i = 0; i < MSG_COUNT; i++) {
            String msg = i+"";
            channel.basicPublish("", queueName, null, msg.getBytes());
            // 1*.此处记录下所有要发送的消息   消息的总和
            outstandingConfirms.put(channel.getNextPublishSeqNo(),msg);
        }
        long end = System.currentTimeMillis();
        System.out.println("发布"+MSG_COUNT+"个异步确认消息，耗时"+(end-begin)+"ms");
    }
}
