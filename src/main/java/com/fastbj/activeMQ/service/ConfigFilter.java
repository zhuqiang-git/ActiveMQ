

import com.alibaba.druid.filter.FilterAdapter;
import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidDataSourceFactory;
import com.alibaba.druid.proxy.jdbc.DataSourceProxy;
import com.alibaba.druid.support.logging.Log;
import com.alibaba.druid.support.logging.LogFactory;
import com.alibaba.druid.util.JdbcUtils;
import com.alibaba.druid.util.StringUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;
import java.security.PublicKey;
import java.sql.SQLException;
import java.util.Properties;

public class ConfigFilter extends FilterAdapter {
    private static Log LOG = LogFactory.getLog(ConfigFilter.class);
    public static final String CONFIG_FILE = "config.file";
    public static final String CONFIG_DECRYPT = "config.decrypt";
    public static final String CONFIG_KEY = "config.decrypt.key";
    public static final String SYS_PROP_CONFIG_FILE = "druid.config.file";
    public static final String SYS_PROP_CONFIG_DECRYPT = "druid.config.decrypt";
    public static final String SYS_PROP_CONFIG_KEY = "druid.config.decrypt.key";

    public ConfigFilter() {
    }

    public void init(DataSourceProxy dataSourceProxy) {
        if (!(dataSourceProxy instanceof DruidDataSource)) {
            LOG.error("ConfigLoader only support DruidDataSource");
        }

        DruidDataSource dataSource = (DruidDataSource)dataSourceProxy;
        Properties connectionProperties = dataSource.getConnectProperties();
        Properties configFileProperties = this.loadPropertyFromConfigFile(connectionProperties);
        boolean decrypt = this.isDecrypt(connectionProperties, configFileProperties);
        if (configFileProperties == null) {
            if (decrypt) {
                this.decrypt(dataSource, (Properties)null);
            }

        } else {
            if (decrypt) {
                this.decrypt(dataSource, configFileProperties);
            }

            try {
                DruidDataSourceFactory.config(dataSource, configFileProperties);
            } catch (SQLException e) {
                throw new IllegalArgumentException("Config DataSource error.", e);
            }
        }
    }

    public boolean isDecrypt(Properties connectionProperties, Properties configFileProperties) {
        String decrypterId = connectionProperties.getProperty("config.decrypt");
        if ((decrypterId == null || decrypterId.length() == 0) && configFileProperties != null) {
            decrypterId = configFileProperties.getProperty("config.decrypt");
        }

        if (decrypterId == null || decrypterId.length() == 0) {
            decrypterId = System.getProperty("druid.config.decrypt");
        }

        return Boolean.valueOf(decrypterId);
    }

    Properties loadPropertyFromConfigFile(Properties connectionProperties) {
        String configFile = connectionProperties.getProperty("config.file");
        if (configFile == null) {
            configFile = System.getProperty("druid.config.file");
        }

        if (configFile != null && configFile.length() > 0) {
            if (LOG.isInfoEnabled()) {
                LOG.info("DruidDataSource Config File load from : " + configFile);
            }

            Properties info = this.loadConfig(configFile);
            if (info == null) {
                throw new IllegalArgumentException("Cannot load remote config file from the [config.file=" + configFile + "].");
            } else {
                return info;
            }
        } else {
            return null;
        }
    }

    public void decrypt(DruidDataSource dataSource, Properties info) {
        try {
            String encryptedPassword = null;
            if (info != null) {
                encryptedPassword = info.getProperty("password");
            }

            if (encryptedPassword == null || encryptedPassword.length() == 0) {
                encryptedPassword = dataSource.getConnectProperties().getProperty("password");
            }

            if (encryptedPassword == null || encryptedPassword.length() == 0) {
                encryptedPassword = dataSource.getPassword();
            }

            PublicKey publicKey = this.getPublicKey(dataSource.getConnectProperties(), info);
            String passwordPlainText = ConfigTools.decrypt(publicKey, encryptedPassword);
            if (info != null) {
                info.setProperty("password", passwordPlainText);
            } else {
                dataSource.setPassword(passwordPlainText);
            }

        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to decrypt.", e);
        }
    }

    public PublicKey getPublicKey(Properties connectionProperties, Properties configFileProperties) {
        String key = null;
        if (configFileProperties != null) {
            key = configFileProperties.getProperty("config.decrypt.key");
        }

        if (StringUtils.isEmpty(key) && connectionProperties != null) {
            key = connectionProperties.getProperty("config.decrypt.key");
        }

        if (StringUtils.isEmpty(key)) {
            key = System.getProperty("druid.config.decrypt.key");
        }

        return ConfigTools.getPublicKey(key);
    }

    public Properties loadConfig(String filePath) {
        Properties properties = new Properties();
        InputStream inStream = null;

        Properties var14;
        try {
            boolean xml = false;
            if (filePath.startsWith("file://")) {
                filePath = filePath.substring("file://".length());
                inStream = this.getFileAsStream(filePath);
                xml = filePath.endsWith(".xml");
            } else if (!filePath.startsWith("http://") && !filePath.startsWith("https://")) {
                if (filePath.startsWith("classpath:")) {
                    String resourcePath = filePath.substring("classpath:".length());
                    inStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourcePath);
                    xml = resourcePath.endsWith(".xml");
                } else {
                    inStream = this.getFileAsStream(filePath);
                    xml = filePath.endsWith(".xml");
                }
            } else {
                URL url = new URL(filePath);
                inStream = url.openStream();
                xml = url.getPath().endsWith(".xml");
            }

            if (inStream != null) {
                if (xml) {
                    properties.loadFromXML(inStream);
                } else {
                    properties.load(inStream);
                }

                var14 = properties;
                return var14;
            }

            LOG.error("load config file error, file : " + filePath);
            var14 = null;
        } catch (Exception ex) {
            LOG.error("load config file error, file : " + filePath, ex);
            var14 = null;
            return var14;
        } finally {
            JdbcUtils.close(inStream);
        }

        return var14;
    }

    private InputStream getFileAsStream(String filePath) throws FileNotFoundException {
        InputStream inStream = null;
        File file = new File(filePath);
        if (file.exists()) {
            inStream = new FileInputStream(file);
        } else {
            inStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(filePath);
        }

        return inStream;
    }
}
