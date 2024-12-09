package com.fastbj.activeMQ.Queue;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;

public class JmsReceiver {
    private static final Logger logs = LoggerFactory.getLogger(JmsReceiver.class);

    
    public static void main(String[] args) throws JMSException {
        System.out.println("ok");
    }

     public static void main(String[] args) {
        try {
            // 获取本机的InetAddress实例
            InetAddress inetAddress = InetAddress.getLocalHost();
            
            // 获取IP地址字符串
            String ipAddress = inetAddress.getHostAddress();
            
            // 打印IP地址
            System.out.println("Local IP Address: " + ipAddress);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    
}
