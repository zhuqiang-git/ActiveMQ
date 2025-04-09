import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 文件名称超长限制异常类
 * 
 * @author ruoyi
 */
public class FileNameLengthLimitExceededException extends FileException
{
    private static final long serialVersionUID = 1L;


    private final ConfigHandler configHandler;

    @Autowired
    public ConfigProxy(ConfigHandler configHandler) {
        this.configHandler = configHandler;
    }

    /**
     * Get configure information list.
     */
    public Page<ConfigBasicInfo> getConfigList(int pageNo, int pageSize, String dataId, String group, String namespaceId,
                                               Map<String, Object> configAdvanceInfo) throws IOException, ServletException, NacosException {
        return configHandler.getConfigList(pageNo, pageSize, dataId, group, namespaceId, configAdvanceInfo);
    }

    /**
     * Get the specific configuration information.
     */
    public ConfigDetailInfo getConfigDetail(String dataId, String group, String namespaceId) throws NacosException {
        return configHandler.getConfigDetail(dataId, group, namespaceId);
    }

    /**
     * Add or update configuration.
     */
    public Boolean publishConfig(ConfigForm configForm, ConfigRequestInfo configRequestInfo) throws NacosException {
        return configHandler.publishConfig(configForm, configRequestInfo);
    }

    /**
     * Delete configuration.
     */
    public Boolean deleteConfig(String dataId, String group, String namespaceId, String tag, String clientIp,
                                String srcUser) throws NacosException {
        return configHandler.deleteConfig(dataId, group, namespaceId, tag, clientIp, srcUser);
    }

    /**
     * Batch delete configurations.
     */
    public Boolean batchDeleteConfigs(List<Long> ids, String clientIp, String srcUser) throws NacosException {
        return configHandler.batchDeleteConfigs(ids, clientIp, srcUser);
    }

    /**
     * Search config list by config detail.
     */
    public Page<ConfigBasicInfo> getConfigListByContent(String search, int pageNo, int pageSize, String dataId, String group,
                                                        String namespaceId, Map<String, Object> configAdvanceInfo) throws NacosException {
        return configHandler.getConfigListByContent(search, pageNo, pageSize, dataId, group, namespaceId,
                configAdvanceInfo);
    }


    public FileNameLengthLimitExceededException(int defaultFileNameLength)
    {
        super("upload.filename.exceed.length", new Object[] { defaultFileNameLength });
    }
}
