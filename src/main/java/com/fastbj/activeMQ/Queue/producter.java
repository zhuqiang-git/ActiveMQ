package com.fastbj.activeMQ.Queue;

//import org.apache.activemq;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;

public class producter {

    public static void main(String[] args) throws JMSException {
        
        // ConnectionFactory ：连接工厂，JMS 用它创建连接
        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(
                ActiveMQConnection.DEFAULT_USER,
                ActiveMQConnection.DEFAULT_PASSWORD,
                "tcp://118.25.154.235:61616");
        // JMS 客户端到JMS Provider 的连接
        Connection connection = connectionFactory.createConnection();
        connection.start();
        // Session： 一个发送或接收消息的线程
        Session session = connection.createSession(Boolean.FALSE, Session.AUTO_ACKNOWLEDGE);
        // Destination ：消息的目的地;消息发送给谁.
        // 获取session注意参数值my-queue是Query的名字
        Destination destination = session.createQueue("my-queue");
        // MessageProducer：消息生产者
        MessageProducer producer = session.createProducer(destination);
        // 设置不持久化（就是设置为点对点通信，如果持久化就是发布订阅模式）
        producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
        // 发送一条消息
        for (int i = 1; i <= 500; i++) {
            sendMsg(session, producer, i);
        }
        connection.close();



    }


        /**
     * 读取指定 key 下所有 member, 按照 score 升序(默认)
     */
    public Collection<Object> getZSetMembers(String key, int startIndex, int endIndex) {
        RScoredSortedSet<Object> scoredSortedSet = client.getScoredSortedSet(key);
        return scoredSortedSet.valueRange(startIndex, endIndex);
    }
 
    /**
     * 取指定 key 下所有 member, 按照 score 降序
     */
    public Collection<Object> getZSetMembersReversed(String key, int startIndex, int endIndex) {
        RScoredSortedSet<Object> scoredSortedSet = client.getScoredSortedSet(key);
        return scoredSortedSet.valueRangeReversed(startIndex, endIndex);
    }

      /**
     * 读取 member和score, 按照 score 升序(默认)
     */
    public Collection<ScoredEntry<Object>> getZSetEntryRange(String key, int startIndex, int endIndex) {
        RScoredSortedSet<Object> scoredSortedSet = client.getScoredSortedSet(key);
        return scoredSortedSet.entryRange(startIndex, endIndex);
    }
 
 
    /**
     * 读取 member和score, 按照 score 降序
     */
    public Collection<ScoredEntry<Object>> getZSetEntryRangeReversed(String key, int startIndex, int endIndex) {
        RScoredSortedSet<Object> scoredSortedSet = client.getScoredSortedSet(key);
        return scoredSortedSet.entryRangeReversed(startIndex, endIndex);

    }

                            

    /**
     * 在指定的会话上，通过指定的消息生产者发出一条消息
     *
     * @param session 消息会话
     * @param producer  消息生产者
     * @param i
     */
    private static void sendMsg(Session session, MessageProducer producer, int i) throws JMSException {
        // 创建一条文本消息
        TextMessage message = session.createTextMessage("Hello ActiveMQ! " + i);
        // 通过消息生产者发出消息
        producer.send(message);
    }

}
