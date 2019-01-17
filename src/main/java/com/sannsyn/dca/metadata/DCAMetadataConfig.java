package com.sannsyn.dca.metadata;

import com.sannsyn.dca.model.config.DCASettings;
import com.sannsyn.dca.vaadin.servlet.DCAUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Configuration for metadata client
 * <p>
 * Created by jobaer on 6/1/16.
 */
public class DCAMetadataConfig {
    private static final Logger logger = LoggerFactory.getLogger(DCAMetadataConfig.class);

    private static Map<String, List<String>> serviceIdFieldsMap = new HashMap<>();
    private static Map<String, List<String>> serviceIdPointersMap = new HashMap<>();
    private static Map<String, Map<String, String>> queryItemMap = new HashMap<>();

    private static final String CONFIG_FILENAME = "com/sannsyn/dca/metadata_fields.conf";

    public static void initMetaDataSettingsMap() {
        Map<String, Object> metaDataMap = DCAUtils.getConfigSettings().getDca();

        if (!metaDataMap.isEmpty()) {
            for (Map.Entry<String, Object> serviceBucketEntry : metaDataMap.entrySet()) {
                String serviceId = serviceBucketEntry.getKey();

                if (serviceIdFieldsMap.containsKey(serviceId) && serviceIdPointersMap.containsKey(serviceId)
                        && queryItemMap.containsKey(serviceId)) {
                    continue;
                }

                serviceIdFieldsMap.put(serviceId, new ArrayList<>());
                serviceIdPointersMap.put(serviceId, new ArrayList<>());
                queryItemMap.put(serviceId, new HashMap<>());

                Map<String, Object> selectedServiceConfig = (Map<String, Object>) serviceBucketEntry.getValue();
                List<String> fieldValueList = (List<String>) selectedServiceConfig.get("fields");

                for (String value : fieldValueList) {
                    serviceIdFieldsMap.get(serviceId).add(value);
                    serviceIdPointersMap.get(serviceId).add("/" + value);
                }

                Map<String, Object> queryItemMapConfig = (Map<String, Object>) selectedServiceConfig.get("mapping");

                for (Map.Entry<String, Object> entry : queryItemMapConfig.entrySet()) {
                    queryItemMap.get(serviceId).put(entry.getKey(), (String) entry.getValue());
                }
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug(String.format("Debug : serviceIdFieldsMap = %s,serviceIdPointersMap = %s, queryItemMap = %s",
                    serviceIdFieldsMap, serviceIdPointersMap, queryItemMap));
        }
    }

    public static List<String> getFields(String serviceIdentifier) {
        return serviceIdFieldsMap.containsKey(serviceIdentifier) ?
                serviceIdFieldsMap.get(serviceIdentifier) : Collections.emptyList();
    }

    public static List<String> getPointers(String serviceIdentifier) {
        return serviceIdPointersMap.containsKey(serviceIdentifier) ?
                serviceIdPointersMap.get(serviceIdentifier) : Collections.emptyList();
    }

    public static Map<String, String> getQueryItemMap(String serviceIdentifier) {
        return queryItemMap.containsKey(serviceIdentifier) ?
                queryItemMap.get(serviceIdentifier) : Collections.emptyMap();
    }
}
