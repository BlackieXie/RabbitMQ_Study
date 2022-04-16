package com.kobe.RabbitMQ.Utils;

/**
 * @description: 睡眠工具类
 * @author: Allen_Kobe
 * @create: 2022-04-14 15:10
 **/
public class SleepUtils {
    public static void sleep(int second){
        try{
            Thread.sleep(1000*second);
        }catch (InterruptedException i){
            Thread.currentThread().interrupt();
        }
    }
}
