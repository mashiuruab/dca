package com.sannsyn.dca.metadata;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * A class representing the metadata of an item.
 */
public class DCAMetadataResponse {
    private String id;
    private String status;
    private Map<String, String> properties = new HashMap<>();
    Gson gson = new Gson();

    public DCAMetadataResponse() {
//        no-arg constructor
    }

    public DCAMetadataResponse(String id, String status) {
        this.id = id;
        this.status = status;
        addProperty("id", id);
    }

    public String getId() {
        return id;
    }

    public DCAMetadataResponse setId(String id) {
        this.id = id;
        addProperty("id", id);
        return this;
    }

    public String getStatus() {
        return status;
    }

    public DCAMetadataResponse setStatus(String status) {
        this.status = status;
        return this;
    }

    public String getAsJsonObject() {
        return gson.toJson(properties);
    }

    public void addProperty(String name, String value) {
        if(StringUtils.isBlank(name) || StringUtils.isBlank(value)) return;

        properties.put(name, value);
    }

    Optional<String> getProperty(String propertyName) {
        if(properties.containsKey(propertyName)) {
            String s = properties.get(propertyName);
            return Optional.of(s);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public String toString() {
        return "DCAMetadataResponse{" +
                "id='" + id + '\'' +
                ", status='" + status + '\'' +
                ", properties=" + properties +
                '}';
    }
}
