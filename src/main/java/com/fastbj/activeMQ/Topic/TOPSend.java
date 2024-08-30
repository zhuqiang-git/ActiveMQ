package com.fastbj.activeMQ.Topic;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;

public class TOPSend {
    private static String BROKERURL = "tcp://118.25.154.235:61616";
    private static String TOPIC = "my-topic";

    public static void main(String[] args) throws JMSException {
        start();
    }

    private static void start() throws JMSException {
        
        System.out.println("生产者已经启动......");
        ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory(
                ActiveMQConnection.DEFAULT_USER, ActiveMQConnection.DEFAULT_PASSWORD, BROKERURL);
        Connection connection = activeMQConnectionFactory.createConnection();

        connection.start();
        Session session = connection.createSession(Boolean.FALSE, Session.AUTO_ACKNOWLEDGE);
        MessageProducer producer = session.createProducer(null);
        producer.setDeliveryMode(DeliveryMode.PERSISTENT);
        send(producer, session);
        System.out.println("发送成功");
        connection.close();
    }

    private static void send(MessageProducer producer, Session session) throws JMSException {
        for (int i = 1; i <= 5; i++) {
            System.out.println("我是消息 " + i);
            TextMessage textMessage = session.createTextMessage("我是消息 " + i);
            Destination destination = session.createTopic(TOPIC);
            producer.send(destination, textMessage);
        }
    }

}
