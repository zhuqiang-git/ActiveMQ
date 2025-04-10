package depend;

import java.util.Map;

import com.jd.platform.async.executor.Async;
import com.jd.platform.async.worker.WorkResult;
import com.jd.platform.async.wrapper.WorkerWrapper;

/**
 * @author sjsdfg
 * @since 2020/6/14
 */


public class LambdaTest {
    public static void main(String[] args) throws Exception {
        WorkerWrapper<WorkResult<User>, String> workerWrapper2 = new WorkerWrapper.Builder<WorkResult<User>, String>()
                .worker((WorkResult<User> result, Map<String, WorkerWrapper> allWrappers) -> {
                    System.out.println("par2的入参来自于par1： " + result.getResult());
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    return result.getResult().getName();
                })
                .callback((boolean success, WorkResult<User> param, WorkResult<String> workResult) ->
                        System.out.println(String.format("thread is %s, param is %s, result is %s", Thread.currentThread().getName(), param, workResult)))
                .id("third")
                .build();

        WorkerWrapper<WorkResult<User>, User> workerWrapper1 = new WorkerWrapper.Builder<WorkResult<User>, User>()
                .worker((WorkResult<User> result, Map<String, WorkerWrapper> allWrappers) -> {
                    System.out.println("par1的入参来自于par0： " + result.getResult());
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    return new User("user1");
                })
                .callback((boolean success, WorkResult<User> param, WorkResult<User> workResult) ->
                        System.out.println(String.format("thread is %s, param is %s, result is %s", Thread.currentThread().getName(), param, workResult)))
                .id("second")
                .next(workerWrapper2)
                .build();

        WorkerWrapper<String, User> workerWrapper = new WorkerWrapper.Builder<String, User>()
                .worker((String object, Map<String, WorkerWrapper> allWrappers) -> {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    return new User("user0");
                })
                .param("0")
                .id("first")
                .next(workerWrapper1, true)
                .callback((boolean success, String param, WorkResult<User> workResult) ->
                        System.out.println(String.format("thread is %s, param is %s, result is %s", Thread.currentThread().getName(), param, workResult)))
                .build();

        //虽然尚未执行，但是也可以先取得结果的引用，作为下一个任务的入参。V1.2前写法，需要手工给
        //V1.3后，不用给wrapper setParam了，直接在worker的action里自行根据id获取即可.参考dependnew包下代码
        WorkResult<User> result = workerWrapper.getWorkResult();
        WorkResult<User> result1 = workerWrapper1.getWorkResult();
        workerWrapper1.setParam(result);
        workerWrapper2.setParam(result1);

        Async.beginWork(3500, workerWrapper);

        System.out.println(workerWrapper2.getWorkResult());
        Async.shutDown();
    }



    
    /**
     * 判断参数是否相同
     */
    private boolean compareParams(Map<String, Object> nowMap, Map<String, Object> preMap)
    {
        String nowParams = (String) nowMap.get(REPEAT_PARAMS);
        String preParams = (String) preMap.get(REPEAT_PARAMS);
        return nowParams.equals(preParams);
    }


    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception
    {
        if (handler instanceof HandlerMethod)
        {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            Method method = handlerMethod.getMethod();
            RepeatSubmit annotation = method.getAnnotation(RepeatSubmit.class);
            if (annotation != null)
            {
                if (this.isRepeatSubmit(request, annotation))
                {
                    AjaxResult ajaxResult = AjaxResult.error(annotation.message());
                    ServletUtils.renderString(response, JSON.toJSONString(ajaxResult));
                    return false;
                }
            }
            return true;
        }
        else
        {
            return true;
        }
    }

    /**
     * 验证是否重复提交由子类实现具体的防重复提交的规则
     *
     * @param request 请求信息
     * @param annotation 防重复注解参数
     * @return 结果
     * @throws Exception
     */
    public abstract boolean isRepeatSubmit(HttpServletRequest request, RepeatSubmit annotation);
    /**
     * 判断两次间隔时间
     */
    private boolean compareTime(Map<String, Object> nowMap, Map<String, Object> preMap, int interval)
    {
        long time1 = (Long) nowMap.get(REPEAT_TIME);
        long time2 = (Long) preMap.get(REPEAT_TIME);
        if ((time1 - time2) < interval)
        {
            return true;
        }
        return false;
    }

    
    /** 编号 */
    private Long tableId;

    /** 表名称 */
    @NotBlank(message = "表名称不能为空")
    private String tableName;

    /** 表描述 */
    @NotBlank(message = "表描述不能为空")
    private String tableComment;

    /** 关联父表的表名 */
    private String subTableName;

    /** 本表关联父表的外键名 */
    private String subTableFkName;

    /** 实体类名称(首字母大写) */
    @NotBlank(message = "实体类名称不能为空")
    private String className;

    /** 使用的模板（crud单表操作 tree树表操作 sub主子表操作） */
    private String tplCategory;

    /** 前端类型（element-ui模版 element-plus模版） */
    private String tplWebType;

    /** 生成包路径 */
    @NotBlank(message = "生成包路径不能为空")
    private String packageName;

    /** 生成模块名 */
    @NotBlank(message = "生成模块名不能为空")
    private String moduleName;

    /** 生成业务名 */
    @NotBlank(message = "生成业务名不能为空")
    private String businessName;

    /** 生成功能名 */
    @NotBlank(message = "生成功能名不能为空")
    private String functionName;

    /** 生成作者 */
    @NotBlank(message = "作者不能为空")
    private String functionAuthor;

    /** 生成代码方式（0zip压缩包 1自定义路径） */
    private String genType;

    /** 生成路径（不填默认项目路径） */
    private String genPath;

    /** 主键信息 */
    private GenTableColumn pkColumn;

    /** 子表信息 */
    private GenTable subTable;


     public PropertyPreExcludeFilter()
    {
    }

    public PropertyPreExcludeFilter addExcludes(String... filters)
    {
        for (int i = 0; i < filters.length; i++)
        {
            this.getExcludes().add(filters[i]);
        }
        return this;
    }

    
    /** 表列信息 */
    @Valid
    private List<GenTableColumn> columns;

    /** 其它生成选项 */
    private String options;

    /** 树编码字段 */
    private String treeCode;

    /** 树父编码字段 */
    private String treeParentCode;

    /** 树名称字段 */
    private String treeName;


    private static final char PLUS = '+';

    private static final char PERCENT = '%';

    private static final char TWO = '2';

    private static final char B = 'B';

    private static final char FIVE = '5';

    public static String getKey(String dataId, String group) {
        return getKey(dataId, group, "");
    }

    public static String getKey(String dataId, String group, String datumStr) {
        return doGetKey(dataId, group, datumStr);
    }

    public static String getKeyTenant(String dataId, String group, String tenant) {
        return doGetKey(dataId, group, tenant);
    }

    private static String doGetKey(String dataId, String group, String datumStr) {
        if (StringUtils.isBlank(dataId)) {
            throw new IllegalArgumentException("invalid dataId");
        }
        if (StringUtils.isBlank(group)) {
            throw new IllegalArgumentException("invalid group");
        }
        StringBuilder sb = new StringBuilder();
        urlEncode(dataId, sb);
        sb.append(PLUS);
        urlEncode(group, sb);
        if (StringUtils.isNotEmpty(datumStr)) {
            sb.append(PLUS);
            urlEncode(datumStr, sb);
        }

        return sb.toString();
    }

    /**
     * Parse key.
     *
     * @param groupKey group key
     * @return parsed key
     */
    public static String[] parseKey(String groupKey) {
        StringBuilder sb = new StringBuilder();
        String dataId = null;
        String group = null;
        String tenant = null;

        for (int i = 0; i < groupKey.length(); ++i) {
            char c = groupKey.charAt(i);
            if (PLUS == c) {
                if (null == dataId) {
                    dataId = sb.toString();
                    sb.setLength(0);
                } else if (null == group) {
                    group = sb.toString();
                    sb.setLength(0);
                } else {
                    throw new IllegalArgumentException("invalid groupkey:" + groupKey);
                }
            } else if (PERCENT == c) {
                char next = groupKey.charAt(++i);
                char nextnext = groupKey.charAt(++i);
                if (TWO == next && B == nextnext) {
                    sb.append(PLUS);
                } else if (TWO == next && FIVE == nextnext) {
                    sb.append(PERCENT);
                } else {
                    throw new IllegalArgumentException("invalid groupkey:" + groupKey);
                }
            } else {
                sb.append(c);
            }
        }

        if (group == null) {
            group = sb.toString();
        } else {
            tenant = sb.toString();
        }

        if (StringUtils.isBlank(dataId)) {
            throw new IllegalArgumentException("invalid dataId");
        }
        if (StringUtils.isBlank(group)) {
            throw new IllegalArgumentException("invalid group");
        }
        return new String[] {dataId, group, tenant};
    }

    /**
     * + -> %2B % -> %25.
     */
    static void urlEncode(String str, StringBuilder sb) {
        for (int idx = 0; idx < str.length(); ++idx) {
            char c = str.charAt(idx);
            if (PLUS == c) {
                sb.append("%2B");
            } else if (PERCENT == c) {
                sb.append("%25");
            } else {
                sb.append(c);
            }
        }
    }

}
