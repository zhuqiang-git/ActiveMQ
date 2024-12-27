package com.sanri.tools.modules.kafka.service;

import java.io.IOException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.PreDestroy;
import javax.management.*;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import com.sanri.tools.modules.core.dtos.UpdateConnectEvent;
import com.sanri.tools.modules.core.dtos.param.RedisConnectParam;
import com.sanri.tools.modules.core.service.connect.ActiveConnectManage;
import com.sanri.tools.modules.core.service.connect.ConnectService;
import com.sanri.tools.modules.core.service.connect.dtos.ConnectInput;
import com.sanri.tools.modules.core.service.connect.dtos.ConnectOutput;
import com.sanri.tools.modules.core.service.connect.events.SecurityConnectEvent;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertySource;
import org.springframework.boot.context.properties.source.ConfigurationPropertySources;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Constants;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sanri.tools.modules.core.dtos.param.KafkaConnectParam;
import com.sanri.tools.modules.core.exception.ToolException;
import com.sanri.tools.modules.core.service.file.ConnectServiceOldFileBase;

import com.sanri.tools.modules.kafka.dtos.*;
import com.sanri.tools.modules.zookeeper.service.ZookeeperService;

import lombok.extern.slf4j.Slf4j;

/**
 * kafka 主题和消费组管理
 */
@Service
@Slf4j
public class MQService implements ApplicationListener<SecurityConnectEvent>, ActiveConnectManage {
    @Autowired
    private ConnectService connectService;
    @Autowired
    private ZookeeperService zookeeperService;

    private YamlPropertySourceLoader yamlPropertySourceLoader = new YamlPropertySourceLoader();

    public static final String MODULE = "kafka";

    private static final Map<String, AdminClient> adminClientMap = new ConcurrentHashMap<>();
  
    /**
     * 停止并移除一个连接, 避免一直在后台报错
     * @param clusterName
     */
    public void stopAndRemove(String clusterName){
        final AdminClient adminClient = adminClientMap.get(clusterName);
        if (adminClient != null){
            try {
                adminClient.close();
            }finally {
                // 移除当前连接
                adminClientMap.remove(clusterName);
            }
        }
    }

    /**
     * 读取 brokers 信息
     * @param clusterName
     * @return
     * @throws IOException
     */
    public List<BrokerInfo> brokers(String clusterName) throws IOException {
//        KafkaConnectParam kafkaConnectParam = (KafkaConnectParam) connectService.readConnParams(MODULE, clusterName);
        KafkaConnectParam kafkaConnectParam = convertToKafkaConnectParam(clusterName);
        String chroot = kafkaConnectParam.getChroot();
        List<BrokerInfo> brokerInfos = readZookeeperBrokers(clusterName, chroot);
        return brokerInfos;
    }

    /**
     * 加载连接信息, 并转换成 KafkaConnectParam
     * @param clusterName
     * @return
     * @throws IOException
     */
    KafkaConnectParam convertToKafkaConnectParam(String clusterName) throws IOException {
        final String loadContent = connectService.loadContent(MODULE, clusterName);
        ByteArrayResource byteArrayResource = new ByteArrayResource(loadContent.getBytes(StandardCharsets.UTF_8));
        final List<PropertySource<?>> load = yamlPropertySourceLoader.load("a", byteArrayResource);
        Iterable<ConfigurationPropertySource> from = ConfigurationPropertySources.from(load);
        Binder binder = new Binder(from);
        BindResult<KafkaConnectParam> bind = binder.bind("", KafkaConnectParam.class);
        KafkaConnectParam kafkaConnectParam = bind.get();
        return kafkaConnectParam;
    }

    /**
     * 创建主题
     * @param clusterName
     * @param topic
     * @param partitions
     * @param replication
     * @return
     */
    public void createTopic(String clusterName,String topic,int partitions,int replication) throws IOException, ExecutionException, InterruptedException {
        AdminClient adminClient = loadAdminClient(clusterName);
        NewTopic newTopic = new NewTopic(topic,partitions,(short)replication);
        CreateTopicsResult createTopicsResult = adminClient.createTopics(Collections.singletonList(newTopic));
        KafkaFuture<Void> voidKafkaFuture = createTopicsResult.values().get(topic);
        voidKafkaFuture.get();
    }

    /**
     * 删除主题
     * @param clusterName
     * @param topic
     * @return
     * @throws IOException
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public void deleteTopic(String clusterName,String topic) throws IOException, ExecutionException, InterruptedException {
        AdminClient adminClient = loadAdminClient(clusterName);
        DeleteTopicsResult deleteTopicsResult = adminClient.deleteTopics(Collections.singletonList(topic));
        deleteTopicsResult.all().get();
    }




    /**
     * 查询参数配置信息
     * 
     * @param config 参数配置信息
     * @return 参数配置信息
     */
    public SysConfig selectConfig(SysConfig config);

    /**
     * 通过ID查询配置
     * 
     * @param configId 参数ID
     * @return 参数配置信息
     */
    public SysConfig selectConfigById(Long configId);

    /**
     * 查询参数配置列表
     * 
     * @param config 参数配置信息
     * @return 参数配置集合
     */
    public List<SysConfig> selectConfigList(SysConfig config);

    /**
     * 根据键名查询参数配置信息
     * 
     * @param configKey 参数键名
     * @return 参数配置信息
     */
    public SysConfig checkConfigKeyUnique(String configKey);

    /**
     * 新增参数配置
     * 
     * @param config 参数配置信息
     * @return 结果
     */
    public int insertConfig(SysConfig config);

    /**
     * 修改参数配置
     * 
     * @param config 参数配置信息
     * @return 结果
     */
    public int updateConfig(SysConfig config);

    /**
     * 删除参数配置
     * 
     * @param configId 参数ID
     * @return 结果
     */
    public int deleteConfigById(Long configId);

    /**
     * 批量删除参数信息
     * 
     * @param configIds 需要删除的参数ID
     * @return 结果
     */
    public int deleteConfigByIds(Long[] configIds);

  

}  


