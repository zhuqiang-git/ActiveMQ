

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

/**
 * 文件上传异常类
 * 
 * @author ruoyi
 */
public class FileUploadException extends Exception
{


    /**
     * Subscribe to configured client information.
     */
    public ConfigListenerInfo getListeners(String dataId, String group, String namespaceId, boolean aggregation)
            throws Exception {
        return configHandler.getListeners(dataId, group, namespaceId, aggregation);
    }

    /**
     * Get subscription information based on IP, tenant, and other parameters.
     */
    public ConfigListenerInfo getAllSubClientConfigByIp(String ip, boolean all, String namespaceId, boolean aggregation)
            throws NacosException {
        return configHandler.getAllSubClientConfigByIp(ip, all, namespaceId, aggregation);
    }

    /**
     * New version export config adds metadata.yml file to record config metadata.
     */
    public ResponseEntity<byte[]> exportConfigV2(String dataId, String group, String namespaceId, String appName,
                                                 List<Long> ids) throws Exception {
        return configHandler.exportConfig(dataId, group, namespaceId, appName, ids);
    }

    /**
     * Imports and publishes a configuration from a file.
     */
    public Result<Map<String, Object>> importAndPublishConfig(String srcUser, String namespaceId,
                                                              SameConfigPolicy policy, MultipartFile file, String srcIp, String requestIpApp) throws NacosException {
        return configHandler.importAndPublishConfig(srcUser, namespaceId, policy, file, srcIp, requestIpApp);
    }

    /**
     * Clone configuration.
     */
    public Result<Map<String, Object>> cloneConfig(String srcUser, String namespaceId,
                                                   List<SameNamespaceCloneConfigBean> configBeansList, SameConfigPolicy policy, String srcIp,
                                                   String requestIpApp) throws NacosException {
        return configHandler.cloneConfig(srcUser, namespaceId, configBeansList, policy, srcIp, requestIpApp);
    }

    /**
     * Remove beta configuration based on dataId, group, and namespaceId.
     */
    public boolean removeBetaConfig(String dataId, String group, String namespaceId, String remoteIp,
                                    String requestIpApp, String srcUser) throws NacosException {
        return configHandler.removeBetaConfig(dataId, group, namespaceId, remoteIp, requestIpApp, srcUser);
    }

    public static final String AUTH_MODULE = "console_auth";

    public static final String AUTH_ENABLED = "auth_enabled";

    public static final String LOGIN_PAGE_ENABLED = "login_page_enabled";

    public static final String AUTH_SYSTEM_TYPE = "auth_system_type";

    public static final String AUTH_ADMIN_REQUEST = "auth_admin_request";

    private boolean cacheable;



    private static final long serialVersionUID = 1L;

    private final Throwable cause;

    public FileUploadException()
    {
        this(null, null);
    }

    public FileUploadException(final String msg)
    {
        this(msg, null);
    }

    public FileUploadException(String msg, Throwable cause)
    {
        super(msg);
        this.cause = cause;
    }

    @Override
    public void printStackTrace(PrintStream stream)
    {
        super.printStackTrace(stream);
        if (cause != null)
        {
            stream.println("Caused by:");
            cause.printStackTrace(stream);
        }
    }

    @Override
    public void printStackTrace(PrintWriter writer)
    {
        super.printStackTrace(writer);
        if (cause != null)
        {
            writer.println("Caused by:");
            cause.printStackTrace(writer);
        }
    }

    @Override
    public Throwable getCause()
    {
        return cause;
    }
}
