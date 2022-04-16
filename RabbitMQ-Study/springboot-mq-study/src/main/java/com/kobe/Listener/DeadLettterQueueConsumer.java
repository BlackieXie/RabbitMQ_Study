package com.kobe.Listener;

import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Date;

/**
 * @description: 消息者监听器
 * @author: Allen_Kobe
 * @create: 2022-04-16 11:01
 **/
@Slf4j
@Component
public class DeadLettterQueueConsumer {
    @RabbitListener(queues = "QD")
    public void receiveD(Message message, Channel channel)throws IOException{
        String msg = new String(message.getBody());
        log.info("当前时间:{},收到死信队列信息{}",new Date().toString(),msg);
    }
}
