package com.sannsyn.dca.vaadin.widgets.operations.controller.model.aggregates.info;

import com.google.gson.Gson;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mashiur on 4/27/16.
 */
public class DCAAggregateInfo {
    private String messageId;
    private String accountId;
    private String requestMessageId;
    private String name;
    private String aggregateType;
    private String updatesDone;
    private String updatesSubmitted;
    private List<String> tags;
    private String numEntities;
    private String numClusters;

    private List<DCAAggregateInfoEntity> entitySamplesJsonStr = new ArrayList<>();
    private List<DCAAggregateInfoEntity> clusterSamplesJsonStr = new ArrayList<>();

    private String memoryConsumed;

    private Gson gson = new Gson();

    public String getMessageId() {
        return messageId;
    }

    public String getAccountId() {
        return accountId;
    }

    public String getRequestMessageId() {
        return requestMessageId;
    }

    public String getName() {
        return name;
    }

    public String getAggregateType() {
        return aggregateType;
    }

    public String getUpdatesDone() {
        return StringUtils.stripToEmpty(updatesDone);
    }

    public String getUpdatesSubmitted() {
        return StringUtils.stripToEmpty(updatesSubmitted);
    }

    public List<String> getTags() {
        return tags;
    }

    public String getNumEntities() {
        return StringUtils.stripToEmpty(numEntities);
    }

    public String getNumClusters() {
        return StringUtils.stripToEmpty(numClusters);
    }

    public List<DCAAggregateInfoEntity> getEntities() {
        return entitySamplesJsonStr;
    }

    public List<DCAAggregateInfoEntity> getClusters() {
        return clusterSamplesJsonStr;
    }

    public String getMemoryConsumed() {
        return StringUtils.stripToEmpty(memoryConsumed);
    }

}
