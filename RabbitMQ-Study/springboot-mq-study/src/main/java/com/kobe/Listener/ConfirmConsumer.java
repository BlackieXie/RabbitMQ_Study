package com.kobe.Listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * @description:
 * @author: Allen_Kobe
 * @create: 2022-04-16 12:19
 **/
@Component
@Slf4j
public class ConfirmConsumer {
    @RabbitListener(queues = {"confirm-queue"})
    public void receiveMsg(Message message){
        log.info("接收到的消息为: " + new String(message.getBody()));
    }
}
