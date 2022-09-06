package com.fastbj.activeMQ.kafka;//package com.fastbj.common.kafka;
//
//
//import com.fastbj.common.util.PropertiesUtil;
//import org.apache.kafka.clients.consumer.ConsumerConfig;
//import org.apache.kafka.common.serialization.StringDeserializer;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.kafka.annotation.EnableKafka;
//import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
//import org.springframework.kafka.config.KafkaListenerContainerFactory;
//import org.springframework.kafka.core.ConsumerFactory;
//import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
//import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
//
//import java.util.HashMap;
//import java.util.Map;
//
//@Configuration
//@EnableKafka
//public class KafkaConsumerConfig {
//
//    static String bootstrapServers;
//    static String groupID;
//    static String autoOffsetReset;
//    static String maxPoolRecords;
//    static Boolean autoCommit;
//    static Integer autoCommitInterval;
//    static Long poolTimeout;
//    static Boolean batchListener;
//    static Integer concurrency;
//    static Integer maxPollInterval;
//    static Integer maxPartitionFetchBytes;
//    static Integer sessiontimeout;
//
//    static{
//
//        PropertiesUtil iknowCommonProperties=new PropertiesUtil("/iknowcommon.properties");
//        String iknowCommonKafkaFileName=iknowCommonProperties.readProperty("iknowcommon.config.filename");
//
//        PropertiesUtil p=new PropertiesUtil("/"+iknowCommonKafkaFileName+".properties");
//        bootstrapServers=p.readProperty("kafka.consumer.bootstrap-servers");
//        groupID=p.readProperty("kafka.consumer.group-id");
//        autoOffsetReset=p.readProperty("kafka.consumer.auto-offset-reset");
//        maxPoolRecords=p.readProperty("kafka.consumer.max-poll-records");
//        autoCommit=Boolean.parseBoolean(p.readProperty("kafka.consumer.enable-auto-commit"));
//        autoCommitInterval=Integer.parseInt(p.readProperty("kafka.consumer.auto-commit-interval"));
//        poolTimeout=Long.parseLong(p.readProperty("kafka.consumer.listener.pool-timeout"));
//        batchListener=Boolean.parseBoolean(p.readProperty("kafka.consumer.listener.batch-listener"));
//        concurrency=Integer.parseInt(p.readProperty("kafka.consumer.listener.concurrencys"));
//        maxPollInterval=Integer.parseInt(p.readProperty("kafka.consumer.max-poll-interval"));
//        maxPartitionFetchBytes=Integer.parseInt(p.readProperty("kafka.consumer.max-partition-fetch-bytes"));
//        sessiontimeout=Integer.parseInt(p.readProperty("kafka.consumer.sessiontimeout"));
//    }
//
//    @Bean
//    @ConditionalOnMissingBean(name="iknowKafkaBatchListener")
//    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String,String>> iknowKafkaBatchListener(){
//        ConcurrentKafkaListenerContainerFactory<String,String> factory=kafkaListenerContainerFactory();
//        factory.setConcurrency(concurrency);
//        System.out.println("[kafka Bean]loading iknowKafkaBatchListener is complete.");
//        return factory;
//    }
//
//    private ConcurrentKafkaListenerContainerFactory<String,String> kafkaListenerContainerFactory(){
//        ConcurrentKafkaListenerContainerFactory<String,String> factory=new ConcurrentKafkaListenerContainerFactory<>();
//        factory.setConsumerFactory(consumerFactory());
//        factory.setBatchListener(batchListener);
//        factory.getContainerProperties().setPollTimeout(poolTimeout);
//        return factory;
//
//    }
//    private ConsumerFactory<String,String> consumerFactory(){
//        return new DefaultKafkaConsumerFactory<>(consumerConfigs());
//    }
//    private Map<String,Object> consumerConfigs(){
//        Map<String,Object> props=new HashMap<>(11);
//        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG,autoCommitInterval);
//        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,bootstrapServers);
//        props.put(ConsumerConfig.GROUP_ID_CONFIG,groupID);
//        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,autoCommit);
//        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG,maxPoolRecords);
//        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,autoOffsetReset);
//        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG,maxPollInterval);
//        props.put(ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG,maxPartitionFetchBytes);
//        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
//        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,StringDeserializer.class);
//        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG,sessiontimeout);
//        return props;
//    }
//}
