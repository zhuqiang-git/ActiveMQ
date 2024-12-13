package com.fastbj.activeMQ.service;

public class KafkaService {

    public static void main(String[] args) throws ExecutionException, InterruptedException {

        Properties properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP\_SERVERS\_CONFIG, "服务器IP地址:9092,服务器IP地址:9093");
        properties.setProperty(ProducerConfig.KEY\_SERIALIZER\_CLASS\_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE\_SERIALIZER\_CLASS\_CONFIG, StringSerializer.class.getName());

        KafkaProducer<String, String> kafkaProducer = new KafkaProducer<>(properties);

        //String -> Object -> HashCode -> 这个字符串在内存中的地址值, 唯一值
        ProducerRecord<String, String> record = new ProducerRecord<>("my-replicated-topic",  "hello, kafka3");

        RecordMetadata recordMetadata = kafkaProducer.send(record).get();

        System.out.println("recordMetadata = " + recordMetadata);

    }
}
