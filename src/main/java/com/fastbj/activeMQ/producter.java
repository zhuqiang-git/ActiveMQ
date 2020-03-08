package src.main.java.com.fastbj.activeMQ;

//import org.apache.activemq;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;

public class producter {

    public static void main(String[] args) throws JMSException {
        System.out.println("Hello java");
//        Connectionfactory
        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(
                ActiveMQConnection.DEFAULT_USER,
                ActiveMQConnection.DEFAULT_PASSWORD,
                "tcp://118.25.154.234:61616");
        Connection connection = connectionFactory.createConnection();
        connection.start();
        Session session = connection.createSession(Boolean.FALSE, Session.AUTO_ACKNOWLEDGE);
        Destination destination = session.createQueue("my-queue");
        MessageProducer producer = session.createProducer(destination);
        producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
        for (int i = 1; i <= 5; i++) {
            sendMsg(session, producer, i);
        }
        connection.close();
    }

    /**
     * 向ActiveMQ发送消息
     *
     * @param session
     * @param producer
     * @param i
     */
    private static void sendMsg(Session session, MessageProducer producer, int i) throws JMSException {
        TextMessage message = session.createTextMessage("Hello ActiveMQ! " + i);
        producer.send(message);
    }

}
