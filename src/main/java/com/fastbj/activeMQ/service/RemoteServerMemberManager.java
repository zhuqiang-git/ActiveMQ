

package com.fastbj.activeMQ.service;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.common.notify.Event;
import com.alibaba.nacos.common.notify.NotifyCenter;
import com.alibaba.nacos.console.handler.impl.remote.EnabledRemoteHandler;
import com.alibaba.nacos.core.cluster.Member;
import com.alibaba.nacos.core.cluster.MemberLookup;
import com.alibaba.nacos.core.cluster.MembersChangeEvent;
import com.alibaba.nacos.core.cluster.NacosMemberManager;
import com.alibaba.nacos.core.cluster.lookup.LookupFactory;
import com.alibaba.nacos.core.utils.Loggers;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * Nacos remote server members manager. Only working on console mode to keep and update the remote server members.
 *
 * @author xiweng.yy
 */
@Service
@EnabledRemoteHandler
public class RemoteServerMemberManager implements NacosMemberManager {


    private final Map<String, Object> param = new HashMap<>();

    @Override
    public Object getParameter(String key) {
        return param.get(key);
    }

    @Override
    public void setParameter(String key, Object value) {
        param.put(key, value);
    }


    /**
     * Nacos remote servers cluster node list.
     */
    private volatile ConcurrentSkipListMap<String, Member> serverList;
    
    /**
     * Addressing pattern instances.
     */
    private MemberLookup lookup;
    
    public RemoteServerMemberManager() {
        this.serverList = new ConcurrentSkipListMap<>();
    }
    
    @PostConstruct
    public void init() throws NacosException {
        initAndStartLookup();
    }
    
    private void initAndStartLookup() throws NacosException {
        this.lookup = LookupFactory.createLookUp();
        this.lookup.injectMemberManager(this);
        this.lookup.start();
    }
    
    @Override
    public synchronized boolean memberChange(Collection<Member> members) {
        ConcurrentSkipListMap<String, Member> newServerList = new ConcurrentSkipListMap<>();
        for (Member each : members) {
            newServerList.put(each.getAddress(), each);
        }
        Loggers.CLUSTER.info("[serverlist] nacos remote server members changed to : {}", newServerList);
        this.serverList = newServerList;
        Event event = MembersChangeEvent.builder().members(members).build();
        NotifyCenter.publishEvent(event);
        return true;
    }
    
    @Override
    public Collection<Member> allMembers() {
        return new HashSet<>(serverList.values());
    }
}
