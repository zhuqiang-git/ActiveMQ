package com.fastbj.activeMQ.kafka;//package com.fastbj.common.kafka;


import com.fastbj.common.util.PropertiesUtil;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.*;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
public class KafkaProducerConfig {
    static String bootstrapServers;
    static Integer retries;
    static String keySerializer;
    static String valueSerializer;

    static {

        PropertiesUtil iknowCommonProperties = new PropertiesUtil("/iknowcommon.properties");
        String iknowCommonKafkaFileName = iknowCommonProperties.readProperty("iknowcommon.config.filename");

        PropertiesUtil p = new PropertiesUtil("/" + iknowCommonKafkaFileName + ".properties");
        bootstrapServers = p.readProperty("kafka.producer.bootstrap-servers");
        keySerializer = p.readProperty("kafka.producer.key-serializer");
        valueSerializer = p.readProperty("kafka.producer.value-serializer");
        retries = Integer.parseInt(p.readProperty("kafka.producer.retries"));




    }

    private ProducerFactory<String, String> producerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfigs());
    }

    @Bean
    public KafkaTemplate<String,String> iknowKafkaTemplate(){
        System.out.println("[Kafka Bean] loading iknowKafkaTemplate is complete.");
        return new KafkaTemplate<String,String>(producerFactory());
    }
    private Map<String, Object> producerConfigs() {
        Map<String, Object> props = new HashMap<>(4);
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.RETRIES_CONFIG, retries);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, keySerializer);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, valueSerializer);
        return props;
    }
}
