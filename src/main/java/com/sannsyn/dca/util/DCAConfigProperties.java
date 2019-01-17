package com.sannsyn.dca.util;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Optional;
import java.util.Properties;

/**
 * Created by mashiur on 8/31/16.
 */
public class DCAConfigProperties {
    private static final Logger logger = LoggerFactory.getLogger(DCAConfigProperties.class);
    private static final String CONFIG_PROPERTY_FILE_NAME = "config.properties";
    private static final String DEFAULT_TMP_DIR = "/tmp/";

    private DCAConfigProperties(){}

    private static Properties getConfig() {
        InputStream inputStream = null;
        try {
            Properties prop = new Properties();
            inputStream = DCAConfigProperties.class.getClassLoader().getResourceAsStream(CONFIG_PROPERTY_FILE_NAME);

            if (inputStream != null) {
                prop.load(inputStream);
                return prop;
            } else {
                throw new FileNotFoundException(String.format("property file '%s' not found in the classpath", CONFIG_PROPERTY_FILE_NAME));
            }

        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Error : ", e);
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }

    public static String getAdminServerUrl() {
        return getConfig().getProperty("adminServerUrl");
    }

    public static String getArkSolrUser() {
        return getConfig().getProperty("arkSolrUser");
    }

    public static String getArkSolrPassword() {
        return getConfig().getProperty("arkSolrPassword");
    }

    public static String getWsUserName() {
        return getConfig().getProperty("wsUserName");
    }

    public static String getWsPassword() {
        return getConfig().getProperty("wsPassword");
    }

    public static String getMailSenderUserName() {
        return getConfig().getProperty("mailSenderUserName");
    }

    public static String getMailSenderPassword() {
        return getConfig().getProperty("mailSenderPassword");
    }

    public static Optional<String>  getMetaDataServerUrl() {
        return Optional.ofNullable(getConfig().getProperty("metaDataServerUrl"));
    }

    public static Optional<String>  getAnalyticsServerUrl() {
        return Optional.ofNullable(getConfig().getProperty("analyticsServerUrl"));
    }

    public static Optional<String> getTemporaryDirectory() {
        return Optional.of(getConfig().getProperty("tmpDirectory") == null ?
                DEFAULT_TMP_DIR : getConfig().getProperty("tmpDirectory"));
    }
}
