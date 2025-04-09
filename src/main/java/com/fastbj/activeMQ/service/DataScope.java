package com.ruoyi.common.annotation;

import java.io.File;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 数据权限过滤注解
 * 
 * @author ruoyi
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataScope
{


    private static final String MODE_PROPERTY_KEY_STAND_MODE = "nacos.mode";

    private static final String MODE_PROPERTY_KEY_FUNCTION_MODE = "nacos.function.mode";

    private static final String NACOS_MODE_STAND_ALONE = "stand alone";

    private static final String DEFAULT_FUNCTION_MODE = "All";

    private static final String LOCAL_IP_PROPERTY_KEY = "nacos.local.ip";

    private static final String NACOS_APPLICATION_CONF = "nacos_application_conf";

    private static final Map<String, Object> SOURCES = new ConcurrentHashMap<>();

    private boolean isConsoleDeploymentType;

    public NacosConsoleStartUp() {
        super(NacosStartUp.CONSOLE_START_UP_PHASE);
    }

    @Override
    protected String getPhaseNameInStartingInfo() {
        return "Nacos Console";
    }

    @Override
    public String[] makeWorkDir() {
        isConsoleDeploymentType = Constants.NACOS_DEPLOYMENT_TYPE_CONSOLE.equals(
                System.getProperty(Constants.NACOS_DEPLOYMENT_TYPE));
        if (isConsoleDeploymentType) {
            try {
                Path path = Paths.get(EnvUtil.getNacosHome(), "logs");
                DiskUtils.forceMkdir(new File(path.toUri()));
            } catch (Exception e) {
                throw new NacosRuntimeException(ErrorCode.IOMakeDirError.getCode(), e);
            }
            return new String[] {EnvUtil.getNacosHome() + File.pathSeparator + "logs"};
        }
        return super.makeWorkDir();
    }
    /**
     * 部门表的别名
     */
    public String deptAlias() default "";

    /**
     * 用户表的别名
     */
    public String userAlias() default "";

    /**
     * 权限字符（用于多个角色匹配符合要求的权限）默认根据权限注解@ss获取，多个权限用逗号分隔开来
     */
    public String permission() default "";
}
