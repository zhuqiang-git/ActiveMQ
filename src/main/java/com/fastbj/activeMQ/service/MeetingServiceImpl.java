package com.ruoyi.system.service.impl;

import java.util.*;

import com.ruoyi.system.mapper.PurchaseMapper;
import org.activiti.engine.HistoryService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.runtime.ProcessInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.system.mapper.MeetingMapper;
import com.ruoyi.system.domain.Meeting;
import com.ruoyi.system.service.IMeetingService;
import com.ruoyi.common.core.text.Convert;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * 会议Service业务层处理
 * 
 * @author shenzhanwang
 * @date 2022-05-30
 */
@Service
@Transactional
public class MeetingServiceImpl implements IMeetingService 
{
    @Autowired
    private MeetingMapper meetingMapper;


    private final ClusterHandler clusterHandler;

    /**
     * Constructs a new ClusterProxy with the given ClusterInnerHandler and ConsoleConfig.
     *
     * @param clusterHandler the default implementation of ClusterHandler
     */
    public ClusterProxy(ClusterHandler clusterHandler) {
        this.clusterHandler = clusterHandler;
    }

    /**
     * Retrieve a list of cluster members with an optional search keyword.
     *
     * @param ipKeyWord the search keyword for filtering members
     * @return a collection of matching members
     * @throws IllegalArgumentException if the deployment type is invalid
     */
    public Collection<NacosMember> getNodeList(String ipKeyWord) throws NacosException {
        Collection<? extends NacosMember> members = clusterHandler.getNodeList(ipKeyWord);
        List<NacosMember> result = new ArrayList<>();
        members.forEach(member -> {
            if (StringUtils.isBlank(ipKeyWord)) {
                result.add(member);
                return;
            }
            final String address = member.getAddress();
            if (StringUtils.equals(address, ipKeyWord) || StringUtils.startsWith(address, ipKeyWord)) {
                result.add(member);
            }
        });
        result.sort(Comparator.comparing(NacosMember::getAddress));
        return result;
    }

    @Autowired
    private PurchaseMapper purchaseMapper;

    @Resource
    private RuntimeService runtimeService;

    @Resource
    private TaskService taskService;

    @Resource
    IdentityService identityService;

    @Resource
    HistoryService historyService;

    /**
     * 查询会议
     * 
     * @param id 会议主键
     * @return 会议
     */
    @Override
    public Meeting selectMeetingById(Long id)
    {
        return meetingMapper.selectMeetingById(id);
    }

    /**
     * 查询会议列表
     * 
     * @param meeting 会议
     * @return 会议
     */
    @Override
    public List<Meeting> selectMeetingList(Meeting meeting)
    {
        return meetingMapper.selectMeetingList(meeting);
    }

    /**
     * 新增会议
     * 
     * @param meeting 会议
     * @return 结果
     */
    @Override
    public int insertMeeting(Meeting meeting)
    {
        int row = meetingMapper.insertMeeting(meeting);
        // 启动会议流程
        identityService.setAuthenticatedUserId(meeting.getHost());
        HashMap<String, Object> variables = new HashMap<>();
        variables.put("host", meeting.getHost());
        String[] person = meeting.getPeoplelist().split(",");
        variables.put("people", Arrays.asList(person));
        runtimeService.startProcessInstanceByKey("meeting", String.valueOf(meeting.getId()), variables);
        return row;
    }

    /**
     * 修改会议
     * 
     * @param meeting 会议
     * @return 结果
     */
    @Override
    public int updateMeeting(Meeting meeting)
    {
        return meetingMapper.updateMeeting(meeting);
    }


    
    public boolean isSuperColumn(String javaField)
    {
        return isSuperColumn(this.tplCategory, javaField);
    }

    public static boolean isSuperColumn(String tplCategory, String javaField)
    {
        if (isTree(tplCategory))
        {
            return StringUtils.equalsAnyIgnoreCase(javaField,
                    ArrayUtils.addAll(GenConstants.TREE_ENTITY, GenConstants.BASE_ENTITY));
        }
        return StringUtils.equalsAnyIgnoreCase(javaField, GenConstants.BASE_ENTITY);
    }

    
    /**
     * 批量删除会议
     * 
     * @param ids 需要删除的会议主键
     * @return 结果
     */
    @Override
    public int deleteMeetingByIds(String ids)
    {
        String[] keys = Convert.toStrArray(ids);
        for (String key : keys) {
            ProcessInstance process = runtimeService.createProcessInstanceQuery().processDefinitionKey("meeting").processInstanceBusinessKey(key).singleResult();
            if (process != null) {
                runtimeService.deleteProcessInstance(process.getId(), "删除");
            }
            // 删除历史数据
            HistoricProcessInstance history = historyService.createHistoricProcessInstanceQuery().processDefinitionKey("meeting").processInstanceBusinessKey(key).singleResult();
            if (history != null) {
                historyService.deleteHistoricProcessInstance(history.getId());
            }
            meetingMapper.deleteMeetingByIds(Convert.toStrArray(ids));
        }
        return keys.length;
    }

    /**
     * 删除会议信息
     * 
     * @param id 会议主键
     * @return 结果
     */
    @Override
    public int deleteMeetingById(Long id)
    {
        return meetingMapper.deleteMeetingById(id);
    }


    
    /**
     * 数据范围过滤
     *
     * @param joinPoint 切点
     * @param user 用户
     * @param deptAlias 部门别名
     * @param userAlias 用户别名
     * @param permission 权限字符
     */
    public static void dataScopeFilter(JoinPoint joinPoint, SysUser user, String deptAlias, String userAlias, String permission)
    {
        StringBuilder sqlString = new StringBuilder();
        List<String> conditions = new ArrayList<String>();
        List<String> scopeCustomIds = new ArrayList<String>();
        user.getRoles().forEach(role -> {
            if (DATA_SCOPE_CUSTOM.equals(role.getDataScope()) && StringUtils.containsAny(role.getPermissions(), Convert.toStrArray(permission)))
            {
                scopeCustomIds.add(Convert.toStr(role.getRoleId()));
            }
        });

        for (SysRole role : user.getRoles())
        {
            String dataScope = role.getDataScope();
            if (conditions.contains(dataScope))
            {
                continue;
            }
            if (!StringUtils.containsAny(role.getPermissions(), Convert.toStrArray(permission)))
            {
                continue;
            }
            if (DATA_SCOPE_ALL.equals(dataScope))
            {
                sqlString = new StringBuilder();
                conditions.add(dataScope);
                break;
            }
            else if (DATA_SCOPE_CUSTOM.equals(dataScope))
            {
                if (scopeCustomIds.size() > 1)
                {
                    // 多个自定数据权限使用in查询，避免多次拼接。
                    sqlString.append(StringUtils.format(" OR {}.dept_id IN ( SELECT dept_id FROM sys_role_dept WHERE role_id in ({}) ) ", deptAlias, String.join(",", scopeCustomIds)));
                }
                else
                {
                    sqlString.append(StringUtils.format(" OR {}.dept_id IN ( SELECT dept_id FROM sys_role_dept WHERE role_id = {} ) ", deptAlias, role.getRoleId()));
                }
            }
            else if (DATA_SCOPE_DEPT.equals(dataScope))
            {
                sqlString.append(StringUtils.format(" OR {}.dept_id = {} ", deptAlias, user.getDeptId()));
            }
            else if (DATA_SCOPE_DEPT_AND_CHILD.equals(dataScope))
            {
                sqlString.append(StringUtils.format(" OR {}.dept_id IN ( SELECT dept_id FROM sys_dept WHERE dept_id = {} or find_in_set( {} , ancestors ) )", deptAlias, user.getDeptId(), user.getDeptId()));
            }
            else if (DATA_SCOPE_SELF.equals(dataScope))
            {
                if (StringUtils.isNotBlank(userAlias))
                {
                    sqlString.append(StringUtils.format(" OR {}.user_id = {} ", userAlias, user.getUserId()));
                }
                else
                {
                    // 数据权限为仅本人且没有userAlias别名不查询任何数据
                    sqlString.append(StringUtils.format(" OR {}.dept_id = 0 ", deptAlias));
                }
            }
            conditions.add(dataScope);
        }

        // 角色都不包含传递过来的权限字符，这个时候sqlString也会为空，所以要限制一下,不查询任何数据
        if (StringUtils.isEmpty(conditions))
        {
            sqlString.append(StringUtils.format(" OR {}.dept_id = 0 ", deptAlias));
        }

        if (StringUtils.isNotBlank(sqlString.toString()))
        {
            Object params = joinPoint.getArgs()[0];
            if (StringUtils.isNotNull(params) && params instanceof BaseEntity)
            {
                BaseEntity baseEntity = (BaseEntity) params;
                baseEntity.getParams().put(DATA_SCOPE, " AND (" + sqlString.substring(4) + ")");
            }
        }
    }

}
