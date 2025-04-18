package com.fastbj.activeMQ.Topic;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.Logger;

import javax.jms.*;
import java.util.Collections;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Future;

public class TopReceiver {
    private static String BROKERURL = "tcp://118.25.154.234:61616";
    private static String TOPIC = "my-topic";

    private static final Logger LOGGER = LogUtils.logger(NacosConfigService.class);

    private static final String UP = "UP";

    private static final String DOWN = "DOWN";

    /**
     * long polling.
     */
    private final ClientWorker worker;

    private String namespace;

    private final ConfigFilterChainManager configFilterChainManager;

    public NacosConfigService(Properties properties) throws NacosException {
        PreInitUtils.asyncPreLoadCostComponent();
        final NacosClientProperties clientProperties = NacosClientProperties.PROTOTYPE.derive(properties);
        LOGGER.info(ClientBasicParamUtil.getInputParameters(clientProperties.asProperties()));
        ValidatorUtils.checkInitParam(clientProperties);

        initNamespace(clientProperties);
        this.configFilterChainManager = new ConfigFilterChainManager(clientProperties.asProperties());
        ConfigServerListManager serverListManager = new ConfigServerListManager(clientProperties);
        serverListManager.start();

        this.worker = new ClientWorker(this.configFilterChainManager, serverListManager, clientProperties);

    }

    private void initNamespace(NacosClientProperties properties) {
        namespace = ClientBasicParamUtil.parseNamespace(properties);
        properties.setProperty(PropertyKeyConst.NAMESPACE, namespace);
    }

    @Override
    public String getConfig(String dataId, String group, long timeoutMs) throws NacosException {
        return getConfigInner(namespace, dataId, group, timeoutMs);
    }

    @Override
    public String getConfigAndSignListener(String dataId, String group, long timeoutMs, Listener listener)
            throws NacosException {
        group = StringUtils.isBlank(group) ? Constants.DEFAULT_GROUP : group.trim();
        ConfigResponse configResponse = worker.getAgent()
                .queryConfig(dataId, group, worker.getAgent().getTenant(), timeoutMs, false);
        String content = configResponse.getContent();
        String encryptedDataKey = configResponse.getEncryptedDataKey();
        worker.addTenantListenersWithContent(dataId, group, content, encryptedDataKey,
                Collections.singletonList(listener));

        // get a decryptContent, fix https://github.com/alibaba/nacos/issues/7039
        ConfigResponse cr = new ConfigResponse();
        cr.setDataId(dataId);
        cr.setGroup(group);
        cr.setContent(content);
        cr.setEncryptedDataKey(encryptedDataKey);
        configFilterChainManager.doFilter(null, cr);
        return cr.getContent();
    }

    @Override
    public void addListener(String dataId, String group, Listener listener) throws NacosException {
        worker.addTenantListeners(dataId, group, Collections.singletonList(listener));
    }

    @Override
    public void fuzzyWatch(String groupNamePattern, FuzzyWatchEventWatcher watcher) throws NacosException {
        doAddFuzzyWatch(ANY_PATTERN, groupNamePattern, watcher);
    }

    @Override
    public void fuzzyWatch(String dataIdPattern, String groupNamePattern, FuzzyWatchEventWatcher watcher)
            throws NacosException {
        doAddFuzzyWatch(dataIdPattern, groupNamePattern, watcher);
    }

    @Override
    public Future<Set<String>> fuzzyWatchWithGroupKeys(String groupNamePattern, FuzzyWatchEventWatcher watcher)
            throws NacosException {
        return doAddFuzzyWatch(ANY_PATTERN, groupNamePattern, watcher);
    }

    @Override
    public Future<Set<String>> fuzzyWatchWithGroupKeys(String dataIdPattern, String groupNamePattern,
                                                       FuzzyWatchEventWatcher watcher) throws NacosException {
        return doAddFuzzyWatch(dataIdPattern, groupNamePattern, watcher);
    }

    private Future<Set<String>> doAddFuzzyWatch(String dataIdPattern, String groupNamePattern,
                                                FuzzyWatchEventWatcher watcher) throws NacosException {
        ConfigFuzzyWatchContext configFuzzyWatchContext = worker.addTenantFuzzyWatcher(dataIdPattern, groupNamePattern,
                watcher);
        return configFuzzyWatchContext.createNewFuture();
    }

    @Override
    public void cancelFuzzyWatch(String groupNamePattern, FuzzyWatchEventWatcher watcher) throws NacosException {
        cancelFuzzyWatch(ANY_PATTERN, groupNamePattern, watcher);
    }

    @Override
    public void cancelFuzzyWatch(String dataIdPattern, String groupNamePattern, FuzzyWatchEventWatcher watcher)
            throws NacosException {
        doCancelFuzzyWatch(dataIdPattern, groupNamePattern, watcher);
    }

    private void doCancelFuzzyWatch(String dataIdPattern, String groupNamePattern, FuzzyWatchEventWatcher watcher)
            throws NacosException {
        if (null == watcher) {
            return;
        }
        worker.removeFuzzyListenListener(dataIdPattern, groupNamePattern, watcher);
    }

    @Override
    public boolean publishConfig(String dataId, String group, String content) throws NacosException {
        return publishConfig(dataId, group, content, ConfigType.getDefaultType().getType());
    }

    @Override
    public boolean publishConfig(String dataId, String group, String content, String type) throws NacosException {
        return publishConfigInner(namespace, dataId, group, null, null, null, content, type, null);
    }

    @Override
    public boolean publishConfigCas(String dataId, String group, String content, String casMd5) throws NacosException {
        return publishConfigInner(namespace, dataId, group, null, null, null, content,
                ConfigType.getDefaultType().getType(), casMd5);
    }

    @Override
    public boolean publishConfigCas(String dataId, String group, String content, String casMd5, String type)
            throws NacosException {
        return publishConfigInner(namespace, dataId, group, null, null, null, content, type, casMd5);
    }

    @Override
    public boolean removeConfig(String dataId, String group) throws NacosException {
        return removeConfigInner(namespace, dataId, group, null);
    }

    @Override
    public void removeListener(String dataId, String group, Listener listener) {
        worker.removeTenantListener(dataId, group, listener);
    }

    private String getConfigInner(String tenant, String dataId, String group, long timeoutMs) throws NacosException {
        group = blank2defaultGroup(group);
        ParamUtils.checkKeyParam(dataId, group);
        ConfigResponse cr = new ConfigResponse();

        cr.setDataId(dataId);
        cr.setTenant(tenant);
        cr.setGroup(group);

        // We first try to use local failover content if exists.
        // A config content for failover is not created by client program automatically,
        // but is maintained by user.
        // This is designed for certain scenario like client emergency reboot,
        // changing config needed in the same time, while nacos server is down.
        String content = LocalConfigInfoProcessor.getFailover(worker.getAgentName(), dataId, group, tenant);
        if (content != null) {
            LOGGER.warn("[{}] [get-config] get failover ok, dataId={}, group={}, tenant={}", worker.getAgentName(),
                    dataId, group, tenant);
            cr.setContent(content);
            String encryptedDataKey = LocalEncryptedDataKeyProcessor.getEncryptDataKeyFailover(worker.getAgentName(),
                    dataId, group, tenant);
            cr.setEncryptedDataKey(encryptedDataKey);
            configFilterChainManager.doFilter(null, cr);
            content = cr.getContent();
            return content;
        }

        try {
            ConfigResponse response = worker.getServerConfig(dataId, group, tenant, timeoutMs, false);
            cr.setContent(response.getContent());
            cr.setEncryptedDataKey(response.getEncryptedDataKey());
            configFilterChainManager.doFilter(null, cr);
            content = cr.getContent();

            return content;
        } catch (NacosException ioe) {
            if (NacosException.NO_RIGHT == ioe.getErrCode()) {
                throw ioe;
            }
            LOGGER.warn("[{}] [get-config] get from server error, dataId={}, group={}, tenant={}, msg={}",
                    worker.getAgentName(), dataId, group, tenant, ioe.toString());
        }

        content = LocalConfigInfoProcessor.getSnapshot(worker.getAgentName(), dataId, group, tenant);
        if (content != null) {
            LOGGER.warn("[{}] [get-config] get snapshot ok, dataId={}, group={}, tenant={}", worker.getAgentName(),
                    dataId, group, tenant);
        }
        cr.setContent(content);
        String encryptedDataKey = LocalEncryptedDataKeyProcessor.getEncryptDataKeySnapshot(worker.getAgentName(),
                dataId, group, tenant);
        cr.setEncryptedDataKey(encryptedDataKey);
        configFilterChainManager.doFilter(null, cr);
        content = cr.getContent();
        return content;
    }
    public static void main(String[] args) throws JMSException {
        
        start();
    }

    private static void start() throws JMSException {
        System.out.println("消费点启动...。。。");


        

        // 创建ActiveMQConnectionFactory 会话工厂
        ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory(
                ActiveMQConnection.DEFAULT_USER, ActiveMQConnection.DEFAULT_PASSWORD, BROKERURL);
        Connection connection = activeMQConnectionFactory.createConnection();
        // 启动JMS 连接
        connection.start();
        // 不开消息启事物，消息主要发送消费者,则表示消息已经签收
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        // 创建一个队列
        Topic topic = session.createTopic(TOPIC);
        MessageConsumer consumer = session.createConsumer(topic);
        // consumer.setMessageListener(new MsgListener());
        while (true) {
            TextMessage textMessage = (TextMessage) consumer.receive();
            if (textMessage != null) {
                System.out.println("接受到消息:" + textMessage.getText());
                // textMessage.acknowledge();// 手动签收
                // session.commit();
            } else {
                break;
            }
        }
        connection.close();
        System.out.println("消费者结束.......");


    }


    /**
     * worker将来要处理的param
     */
    private T param;
    private IWorker<T, V> worker;
    private ICallback<T, V> callback;
    /**
     * 在自己后面的wrapper，如果没有，自己就是末尾；如果有一个，就是串行；如果有多个，有几个就需要开几个线程</p>
     * -------2
     * 1
     * -------3
     * 如1后面有2、3
     */
    private List<WorkerWrapper<?, ?>> nextWrappers;
    /**
     * 依赖的wrappers，有2种情况，1:必须依赖的全部完成后，才能执行自己 2:依赖的任何一个、多个完成了，就可以执行自己
     * 通过must字段来控制是否依赖项必须完成
     * 1
     * -------3
     * 2
     * 1、2执行完毕后才能执行3
     */
    private List<DependWrapper> dependWrappers;
    /**
     * 标记该事件是否已经被处理过了，譬如已经超时返回false了，后续rpc又收到返回值了，则不再二次回调
     * 经试验,volatile并不能保证"同一毫秒"内,多线程对该值的修改和拉取
     * <p>
     * 1-finish, 2-error, 3-working
     */
    private AtomicInteger state = new AtomicInteger(0);
    /**
     * 该map存放所有wrapper的id和wrapper映射
     */
    private Map<String, WorkerWrapper> forParamUseWrappers;
    /**
     * 也是个钩子变量，用来存临时的结果
     */
    private volatile WorkResult<V> workResult = WorkResult.defaultResult();
    /**
     * 是否在执行自己前，去校验nextWrapper的执行结果<p>
     * 1   4
     * -------3
     * 2
     * 如这种在4执行前，可能3已经执行完毕了（被2执行完后触发的），那么4就没必要执行了。
     * 注意，该属性仅在nextWrapper数量<=1时有效，>1时的情况是不存在的
     */
    private volatile boolean needCheckNextWrapperResult = true;

    private static final int FINISH = 1;
    private static final int ERROR = 2;
    private static final int WORKING = 3;
    private static final int INIT = 0;

    private WorkerWrapper(String id, IWorker<T, V> worker, T param, ICallback<T, V> callback) {
        if (worker == null) {
            throw new NullPointerException("async.worker is null");
        }
        this.worker = worker;
        this.param = param;
        this.id = id;
        //允许不设置回调
        if (callback == null) {
            callback = new DefaultCallback<>();
        }
        this.callback = callback;
    }

    /**
     * 开始工作
     * fromWrapper代表这次work是由哪个上游wrapper发起的
     */
    private void work(ExecutorService executorService, WorkerWrapper fromWrapper, long remainTime, Map<String, WorkerWrapper> forParamUseWrappers) {
        this.forParamUseWrappers = forParamUseWrappers;
        //将自己放到所有wrapper的集合里去
        forParamUseWrappers.put(id, this);
        long now = SystemClock.now();
        //总的已经超时了，就快速失败，进行下一个
        if (remainTime <= 0) {
            fastFail(INIT, null);
            beginNext(executorService, now, remainTime);
            return;
        }
        //如果自己已经执行过了。
        //可能有多个依赖，其中的一个依赖已经执行完了，并且自己也已开始执行或执行完毕。当另一个依赖执行完毕，又进来该方法时，就不重复处理了
        if (getState() == FINISH || getState() == ERROR) {
            beginNext(executorService, now, remainTime);
            return;
        }

        //如果在执行前需要校验nextWrapper的状态
        if (needCheckNextWrapperResult) {
            //如果自己的next链上有已经出结果或已经开始执行的任务了，自己就不用继续了
            if (!checkNextWrapperResult()) {
                fastFail(INIT, new SkippedException());
                beginNext(executorService, now, remainTime);
                return;
            }
        }

        //如果没有任何依赖，说明自己就是第一批要执行的
        if (dependWrappers == null || dependWrappers.size() == 0) {
            fire();
            beginNext(executorService, now, remainTime);
            return;
        }

        /*如果有前方依赖，存在两种情况
         一种是前面只有一个wrapper。即 A  ->  B
        一种是前面有多个wrapper。A C D ->   B。需要A、C、D都完成了才能轮到B。但是无论是A执行完，还是C执行完，都会去唤醒B。
        所以需要B来做判断，必须A、C、D都完成，自己才能执行 */

        //只有一个依赖
        if (dependWrappers.size() == 1) {
            doDependsOneJob(fromWrapper);
            beginNext(executorService, now, remainTime);
        } else {
            //有多个依赖时
            doDependsJobs(executorService, dependWrappers, fromWrapper, now, remainTime);
        }

    }

}
