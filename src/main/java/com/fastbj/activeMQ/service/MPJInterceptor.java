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
     * 导出时在excel中排序
     */
    public int sort() default Integer.MAX_VALUE;

    /**
     * 导出到Excel中的名字.
     */
    public String name() default "";

    /**
     * 日期格式, 如: yyyy-MM-dd
     */
    public String dateFormat() default "";

    /**
     * 如果是字典类型，请设置字典的type值 (如: sys_user_sex)
     */
    public String dictType() default "";

    /**
     * 读取内容转表达式 (如: 0=男,1=女,2=未知)
     */
    public String readConverterExp() default "";

    /**
     * 分隔符，读取字符串组内容
     */
    public String separator() default ",";

    /**
     * BigDecimal 精度 默认:-1(默认不开启BigDecimal格式化)
     */
    public int scale() default -1;

    /**
     * BigDecimal 舍入规则 默认:BigDecimal.ROUND_HALF_EVEN
     */
    public int roundingMode() default BigDecimal.ROUND_HALF_EVEN;

    /**
     * 导出时在excel中每个列的高度
     */
    public double height() default 14;

    /**
     * 导出时在excel中每个列的宽度
     */
    public double width() default 16;

    /**
     * 文字后缀,如% 90 变成90%
     */
    public String suffix() default "";

    /**
     * 当值为空时,字段的默认值
     */
    public String defaultValue() default "";

    /**
     * 提示信息
     */
    public String prompt() default "";

    /**
     * 设置只能选择不能输入的列内容.
     */
    public String[] combo() default {};

    /**
     * 是否从字典读数据到combo,默认不读取,如读取需要设置dictType注解.
     */
    public boolean comboReadDict() default false;

    /**
     * 是否需要纵向合并单元格,应对需求:含有list集合单元格)
     */
    public boolean needMerge() default false;

    /**
     * 是否导出数据,应对需求:有时我们需要导出一份模板,这是标题需要但内容需要用户手工填写.
     */
    public boolean isExport() default true;

    /**
     * 另一个类中的属性名称,支持多级获取,以小数点隔开
     */
    public String targetAttr() default "";

    /**
     * 是否自动统计数据,在最后追加一行统计数据总和
     */
    public boolean isStatistics() default false;

    /**
     * 导出类型（0数字 1字符串 2图片）
     */
    public ColumnType cellType() default ColumnType.STRING;

    /**
     * 导出列头背景颜色
     */
    public IndexedColors headerBackgroundColor() default IndexedColors.GREY_50_PERCENT;

    /**
     * 导出列头字体颜色
     */
    public IndexedColors headerColor() default IndexedColors.WHITE;

    /**
     * 导出单元格背景颜色
     */
    public IndexedColors backgroundColor() default IndexedColors.WHITE;

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
