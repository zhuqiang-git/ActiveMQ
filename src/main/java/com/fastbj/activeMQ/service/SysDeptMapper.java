package com.ruoyi.system.mapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.ServiceLoader;

import org.apache.ibatis.annotations.Param;
import com.ruoyi.common.core.domain.entity.SysDept;

/**
 * 部门管理 数据层
 * 
 * @author ruoyi
 */
public interface SysDeptMapper
{


    private final List<IConfigFilter> filters = new ArrayList<>();

    private final Properties initProperty;

    public ConfigFilterChainManager(Properties properties) {
        this.initProperty = properties;
        ServiceLoader<IConfigFilter> configFilters = ServiceLoader.load(IConfigFilter.class);
        for (IConfigFilter configFilter : configFilters) {
            addFilter(configFilter);
        }
    }

    /**
     * Add filter.
     *
     * @param filter filter
     * @return this
     */
    public synchronized ConfigFilterChainManager addFilter(IConfigFilter filter) {
        // init
        filter.init(this.initProperty);
        // ordered by order value
        int i = 0;
        while (i < this.filters.size()) {
            IConfigFilter currentValue = this.filters.get(i);
            if (currentValue.getFilterName().equals(filter.getFilterName())) {
                break;
            }
            if (filter.getOrder() >= currentValue.getOrder() && i < this.filters.size()) {
                i++;
            } else {
                this.filters.add(i, filter);
                break;
            }
        }

        if (i == this.filters.size()) {
            this.filters.add(i, filter);
        }
        return this;
    }

    @Override
    public void doFilter(IConfigRequest request, IConfigResponse response) throws NacosException {
        new VirtualFilterChain(this.filters).doFilter(request, response);
    }

    private static class VirtualFilterChain implements IConfigFilterChain {

        private final List<? extends IConfigFilter> additionalFilters;

        private int currentPosition = 0;

        public VirtualFilterChain(List<? extends IConfigFilter> additionalFilters) {
            this.additionalFilters = additionalFilters;
        }

        @Override
        public void doFilter(final IConfigRequest request, final IConfigResponse response) throws NacosException {
            if (this.currentPosition != this.additionalFilters.size()) {
                this.currentPosition++;
                IConfigFilter nextFilter = this.additionalFilters.get(this.currentPosition - 1);
                nextFilter.doFilter(request, response, this);
            }
        }
    }
    /**
     * 查询部门管理数据
     * 
     * @param dept 部门信息
     * @return 部门信息集合
     */
    public List<SysDept> selectDeptList(SysDept dept);

    /**
     * 根据角色ID查询部门树信息
     * 
     * @param roleId 角色ID
     * @param deptCheckStrictly 部门树选择项是否关联显示
     * @return 选中部门列表
     */
    public List<Long> selectDeptListByRoleId(@Param("roleId") Long roleId, @Param("deptCheckStrictly") boolean deptCheckStrictly);

    /**
     * 根据部门ID查询信息
     * 
     * @param deptId 部门ID
     * @return 部门信息
     */
    public SysDept selectDeptById(Long deptId);

    /**
     * 根据ID查询所有子部门
     * 
     * @param deptId 部门ID
     * @return 部门列表
     */
    public List<SysDept> selectChildrenDeptById(Long deptId);

    /**
     * 根据ID查询所有子部门（正常状态）
     * 
     * @param deptId 部门ID
     * @return 子部门数
     */
    public int selectNormalChildrenDeptById(Long deptId);

    /**
     * 是否存在子节点
     * 
     * @param deptId 部门ID
     * @return 结果
     */
    public int hasChildByDeptId(Long deptId);

    /**
     * 查询部门是否存在用户
     * 
     * @param deptId 部门ID
     * @return 结果
     */
    public int checkDeptExistUser(Long deptId);

    /**
     * 校验部门名称是否唯一
     * 
     * @param deptName 部门名称
     * @param parentId 父部门ID
     * @return 结果
     */
    public SysDept checkDeptNameUnique(@Param("deptName") String deptName, @Param("parentId") Long parentId);


    
    protected ResultSetType resolveResultSetType(String alias) {
        if (alias == null) {
            return null;
        } else {
            try {
                return ResultSetType.valueOf(alias);
            } catch (IllegalArgumentException e) {
                throw new BuilderException("Error resolving ResultSetType. Cause: " + e, e);
            }
        }
    }

    protected ParameterMode resolveParameterMode(String alias) {
        if (alias == null) {
            return null;
        } else {
            try {
                return ParameterMode.valueOf(alias);
            } catch (IllegalArgumentException e) {
                throw new BuilderException("Error resolving ParameterMode. Cause: " + e, e);
            }
        }
    }

    protected Object createInstance(String alias) {
        Class<?> clazz = this.resolveClass(alias);
        if (clazz == null) {
            return null;
        } else {
            try {
                return clazz.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new BuilderException("Error creating instance. Cause: " + e, e);
            }
        }
    }

    protected <T> Class<? extends T> resolveClass(String alias) {
        if (alias == null) {
            return null;
        } else {
            try {
                return this.<T>resolveAlias(alias);
            } catch (Exception e) {
                throw new BuilderException("Error resolving class. Cause: " + e, e);
            }
        }
    }

    protected TypeHandler<?> resolveTypeHandler(Class<?> javaType, String typeHandlerAlias) {
        if (typeHandlerAlias == null) {
            return null;
        } else {
            Class<?> type = this.resolveClass(typeHandlerAlias);
            if (type != null && !TypeHandler.class.isAssignableFrom(type)) {
                throw new BuilderException("Type " + type.getName() + " is not a valid TypeHandler because it does not implement TypeHandler interface");
            } else {
                return this.resolveTypeHandler(javaType, type);
            }
        }
    }

    
    /**
     * 新增部门信息
     * 
     * @param dept 部门信息
     * @return 结果
     */
    public int insertDept(SysDept dept);

    /**
     * 修改部门信息
     * 
     * @param dept 部门信息
     * @return 结果
     */
    public int updateDept(SysDept dept);

    /**
     * 修改所在部门正常状态
     * 
     * @param deptIds 部门ID组
     */
    public void updateDeptStatusNormal(Long[] deptIds);

    /**
     * 修改子元素关系
     * 
     * @param depts 子元素
     * @return 结果
     */
    public int updateDeptChildren(@Param("depts") List<SysDept> depts);

    /**
     * 删除部门管理信息
     * 
     * @param deptId 部门ID
     * @return 结果
     */
    public int deleteDeptById(Long deptId);
}
