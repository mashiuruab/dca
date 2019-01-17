package com.sannsyn.dca.vaadin.widgets.operations.controller.model.aggregates;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by mashiur on 4/4/16.
 */
public class DCATaskObject {
    private List<String> finalFilters = new ArrayList<>();
    private List<Object> chain = new ArrayList<>();
    private String description = "";
    private String out = "";
    @SerializedName("taxa.out")
    private String outTaxon;

    private DCAEnsembles ensembles;

    public void setChain(Object chain) {
        List<Object> chainList = new ArrayList<>();

        if (chain instanceof List) {
            chainList = (List<Object>) chain;
        } else {
            chainList.add(chain);
        }
        this.chain = chainList;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setOut(String out) {
        this.out = out;
    }

    public void setEnsembles(DCAEnsembles ensembles) {
        this.ensembles = ensembles;
    }

    private List<Object> getChain(String pipeName) {
        if (this.ensembles.getTasks().containsKey(pipeName)) {
            this.ensembles.getTasks().get(pipeName).setEnsembles(ensembles);
            return this.ensembles.getTasks().get(pipeName).getChain();
        } else {
            throw new RuntimeException(String.format("%s PipeLine Not found", pipeName));
        }
    }

    public List<Object> getChain() {
        List<Object> chainList = new ArrayList<>();

        for (Object object : chain) {
            chainList.add(getObject(object));
        }
        return chainList;
    }

    private Object getObject(Object object) {
        if (object instanceof String) {
            return object;
        } else if (object instanceof List) {
            List originalList = (List) object;
            List<Object>  modifiedList = new ArrayList<>();
            for(Object item : originalList) {
                modifiedList.add(getObject(item));
            }
            return modifiedList;
        } else {
            Gson gson = new Gson();
            String  objectString = gson.toJson(object);
            Type joinPipeType = new TypeToken<Map<String, Object>>(){}.getType();
            Map<String, Object> joinPipeMap = gson.fromJson(objectString, joinPipeType);
            Map<String, Object> modifiedJoinMap = new LinkedHashMap<>();

            for (Map.Entry<String, Object> entry : joinPipeMap.entrySet()) {
                String key = entry.getKey();
                Object value = getObject(entry.getValue());
                modifiedJoinMap.put(key, value);
            }
            return modifiedJoinMap;
        }
    }

    public String getDescription() {
        return description;
    }

    public String getOut() {
        return out;
    }

    public String getOutTaxon() {
        return StringUtils.stripToEmpty(outTaxon);
    }

    public void setOutTaxon(String outTaxon) {
        this.outTaxon = outTaxon;
    }

    public List<String> getFinalFilters() {
        return finalFilters;
    }

    public void setFinalFilters(List<String> finalFilters) {
        this.finalFilters = finalFilters;
    }
}
