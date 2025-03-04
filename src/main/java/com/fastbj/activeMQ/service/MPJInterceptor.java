package com.github.yulichang.interceptor;

import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.toolkit.*;
import com.github.yulichang.config.ConfigProperties;
import com.github.yulichang.method.MPJResultType;
import com.github.yulichang.query.MPJQueryWrapper;
import com.github.yulichang.toolkit.Constant;
import com.github.yulichang.toolkit.MPJReflectionKit;
import com.github.yulichang.toolkit.MPJTableMapperHelper;
import com.github.yulichang.toolkit.TableHelper;
import com.github.yulichang.toolkit.support.FieldCache;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import com.github.yulichang.wrapper.resultmap.Label;
import com.github.yulichang.wrapper.resultmap.Result;
import com.github.yulichang.wrapper.segments.Select;
import com.github.yulichang.wrapper.segments.SelectLabel;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ResultFlag;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.ResultMapping;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 连表拦截器
 * 用于实现动态resultType
 * 之前的实现方式是mybatis-plus的Interceptor,无法修改args,存在并发问题
 * 所以将这个拦截器独立出来

 */
@SuppressWarnings("unchecked")
@Intercepts(@Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}))
public class MPJInterceptor implements Interceptor {


    private static final List<ResultMapping> EMPTY_RESULT_MAPPING = new ArrayList<>(0);

    /**
     * 缓存MappedStatement,不需要每次都去重新构建MappedStatement
     */
    private static final Map<String, Map<Configuration, MappedStatement>> MS_CACHE = new ConcurrentHashMap<>();


    @Override
    @SuppressWarnings("Java8MapApi")
    public Object intercept(Invocation invocation) throws Throwable {
        Object[] args = invocation.getArgs();
        if (args[0] instanceof MappedStatement) {
            MappedStatement ms = (MappedStatement) args[0];
            if (args[1] instanceof Map) {
                Map<String, Object> map = (Map<String, Object>) args[1];
                Object ew = map.containsKey(Constants.WRAPPER) ? map.get(Constants.WRAPPER) : null;
                if (CollectionUtils.isNotEmpty(map) && map.containsKey(Constant.CLAZZ)) {
                    Class<?> clazz = (Class<?>) map.get(Constant.CLAZZ);
                    if (Objects.nonNull(clazz)) {
                        List<ResultMap> list = ms.getResultMaps();
                        if (CollectionUtils.isNotEmpty(list)) {
                            ResultMap resultMap = list.get(0);
                            if (resultMap.getType() == MPJResultType.class) {
                                args[0] = getMappedStatement(ms, clazz, ew);
                            }
                        }
                    }
                }
            }
        }
        return invocation.proceed();
    }


    /**
     * 获取MappedStatement
     */
    @SuppressWarnings("rawtypes")
    public MappedStatement getMappedStatement(MappedStatement ms, Class<?> resultType, Object ew) {
        String id = ms.getId() + StringPool.DASH + (resultType.getName().replaceAll("\\.", StringPool.DASH));
        if (ew instanceof MPJLambdaWrapper) {
            MPJLambdaWrapper wrapper = (MPJLambdaWrapper) ew;
            if (wrapper.getEntityClass() == null) {
                wrapper.setEntityClass(MPJTableMapperHelper.getEntity(getEntity(ms.getId())));
            }
            if (wrapper.getSelectColumns().isEmpty() && wrapper.getEntityClass() != null) {
                wrapper.selectAll(wrapper.getEntityClass());
            }
            //TODO 重构缓存 -> 根据sql缓存
            //不走缓存
        }
        if (ew instanceof MPJQueryWrapper) {
            MPJQueryWrapper wrapper = (MPJQueryWrapper) ew;
            if (ConfigProperties.msCache) {
                return getCache(ms, id + StringPool.UNDERSCORE + removeDot(wrapper.getSqlSelect()), resultType, ew);
            }
        }
        return buildMappedStatement(ms, resultType, ew, id);
    }


    /**
     * 走缓存
     */
    private MappedStatement getCache(MappedStatement ms, String id, Class<?> resultType, Object ew) {
        Map<Configuration, MappedStatement> statementMap = MS_CACHE.get(id);
        if (CollectionUtils.isNotEmpty(statementMap)) {
            MappedStatement statement = statementMap.get(ms.getConfiguration());
            if (Objects.nonNull(statement)) {
                return statement;
            }
        }
        MappedStatement mappedStatement = buildMappedStatement(ms, resultType, ew, id);
        if (statementMap == null) {
            statementMap = new ConcurrentHashMap<>();
            MS_CACHE.put(id, statementMap);
        }
        statementMap.put(ms.getConfiguration(), mappedStatement);
        return mappedStatement;
    }


    /**
     * 构建新的MappedStatement
     */
    private MappedStatement buildMappedStatement(MappedStatement ms, Class<?> resultType, Object ew, String id) {
        MappedStatement.Builder builder = new MappedStatement.Builder(ms.getConfiguration(), id, ms.getSqlSource(), ms.getSqlCommandType())
                .resource(ms.getResource())
                .fetchSize(ms.getFetchSize())
                .statementType(ms.getStatementType())
                .keyGenerator(ms.getKeyGenerator())
                .timeout(ms.getTimeout())
                .parameterMap(ms.getParameterMap())
                .resultSetType(ms.getResultSetType())
                .cache(ms.getCache())
                .flushCacheRequired(ms.isFlushCacheRequired())
                .useCache(ms.isUseCache());
        if (ms.getKeyProperties() != null && ms.getKeyProperties().length != 0) {
            builder.keyProperty(String.join(StringPool.COMMA, ms.getKeyProperties()));
        }
        List<ResultMap> resultMaps = buildResultMap(ms, resultType, ew);
        builder.resultMaps(resultMaps);
        return builder.build();
    }

}
