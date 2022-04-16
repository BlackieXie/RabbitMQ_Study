package com.kobe.RabbitMQ.helloworld;

import com.rabbitmq.client.*;


/**
 * @description: 消费者
 * @author: Allen_Kobe
 * @create: 2022-04-14 11:10
 **/
public class Consumer {
    //队列名称
    public static final String QUEUE_NAME = "hello";
    //接收
    public static void main(String[] args) throws Exception {
        //创建一个连接工厂
        ConnectionFactory factory = new ConnectionFactory();
        //工厂IP 连接RabbitMQ的队列
        factory.setHost("127.0.0.1");
        //用户名和密码
        factory.setUsername("guest");
        factory.setPassword("guest");
        //创建连接
        Connection connection = factory.newConnection();
        //获取信道
        Channel channel = connection.createChannel();
        System.out.println("等待接收消息");
        //推送的消息如何进行消费的接口回调
        DeliverCallback deliverCallback = (consumerTag,message)->{
            String msg = new String(message.getBody());
            System.out.println(msg);
        };

        //取消消费的一个回调接口 如在消费的时候队列被删除掉了
        CancelCallback cancelCallback= consumerTag->{
                System.out.println("消息消费被中断");
        };
        /**
         * 消费者消费消息
         * 1.消费哪个队列
         * 2.true 接收到传递过来的消息后acknowledged（应答服务器），false 接收到消息后不应答服务器
         * 3.deliverCallback： 当一个消息发送过来后的回调接口
         * 4.cancelCallback：当一个消费者取消订阅时的回调接口;取消消费者订阅队列时除了使用{@link Channel#basicCancel}之外的所有方式都会调用该回调方法
         */
        channel.basicConsume(QUEUE_NAME,true,deliverCallback,cancelCallback);
        System.out.println("消息消费完毕");
    }
}
