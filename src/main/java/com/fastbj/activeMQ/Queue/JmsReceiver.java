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


    
 
    /**
     * 异步删除指定 ZSet 中的指定 memberName 元素
     */
    public void removeZSetMemberAsync(String key, String memberName) {
        RScoredSortedSet<Object> scoredSortedSet = client.getScoredSortedSet(key);
        if (!scoredSortedSet.isExists()) {
            return;
        }
        scoredSortedSet.removeAsync(memberName);
    }
 
 
    /**
     * 异步批量删除指定 ZSet 中的指定 member 元素列表
     */
    public void removeZSetMemberAsync(String key, List<String> memberList) {
        RScoredSortedSet<Object> scoredSortedSet = client.getScoredSortedSet(key);
        if (!scoredSortedSet.isExists()) {
            return;
        }
        RBatch batch = client.createBatch();
        memberList.forEach(member -> batch.getScoredSortedSet(key).removeAsync(member));
        batch.execute();
    }




    

    
}
