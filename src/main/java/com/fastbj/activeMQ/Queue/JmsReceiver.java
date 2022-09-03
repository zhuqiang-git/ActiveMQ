package com.fastbj.activeMQ.Queue;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;

public class JmsReceiver {
    private static final Logger logs = LoggerFactory.getLogger(JmsReceiver.class);
    public static void main(String[] args) throws JMSException {
// ConnectionFactory ：连接工厂，JMS 用它创建连接
        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(
                ActiveMQConnection.DEFAULT_USER,
                ActiveMQConnection.DEFAULT_PASSWORD,
                "tcp://118.25.154.234:61616");
        // JMS 客户端到JMS Provider 的连接
        Connection connection = connectionFactory.createConnection();
        connection.start();
        // Session： 一个发送或接收消息的线程
        Session session = connection.createSession(Boolean.TRUE, Session.AUTO_ACKNOWLEDGE);
        // Destination ：消息的目的地;消息发送给谁.
        // 获取session注意参数值xingbo.xu-queue是一个服务器的queue，须在在ActiveMq的console配置
        Destination destination = session.createQueue("foo.bar");
        // 消费者，消息接收者
        MessageConsumer consumer = session.createConsumer(destination);
        while (true) {
            TextMessage message = (TextMessage) consumer.receive();
            if (null != message) {
                System.out.println("收到消息：" + message.getText() + " 编号为：" + message.getJMSMessageID());
            } else {
                break;
            }

        }
        session.close();
        connection.close();

        logs.error("消息推送成功");


    }
}
