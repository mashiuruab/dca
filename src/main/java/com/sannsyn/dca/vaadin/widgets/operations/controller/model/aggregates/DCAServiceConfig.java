package com.sannsyn.dca.vaadin.widgets.operations.controller.model.aggregates;

import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * Created by mashiur on 4/4/16.
 */
public class DCAServiceConfig {
    private DCAAggregateDefault aggregateDefaults;
    private Map<String, DCAAggregateItem> aggregates;
    private DCAEnsembles ensembles;
    private DCAExternal external;

    public DCAAggregateDefault getAggregateDefaults() {
        return aggregateDefaults;
    }

    public Map<String, DCAAggregateItem> getAggregates() {
        return aggregates;
    }

    public DCAEnsembles getEnsembles() {
        return ensembles;
    }

    public Optional<DCAExternal> getExternal() {
        return Optional.ofNullable(external);
    }

    public Map<String, Set<String>> getPrePopulatedAggregateInfo() {
        Map<String, Set<String>> valueMap = new LinkedHashMap<>();
        valueMap.put("type", new HashSet<String>());
        valueMap.put("entityTaxon", new HashSet<String>());
        valueMap.put("clusterTaxon", new HashSet<String>());
        valueMap.put("tags", new TreeSet<String>(String.CASE_INSENSITIVE_ORDER));

        for(Map.Entry<String, DCAAggregateItem> entry : getAggregates().entrySet()) {
            DCAAggregateItem dcaAggregateItem = entry.getValue();
            valueMap.get("type").add(dcaAggregateItem.getType());

            if(StringUtils.isNotEmpty(dcaAggregateItem.getEntityTaxon())) {
                valueMap.get("entityTaxon").add(dcaAggregateItem.getEntityTaxon());
            }

            if (StringUtils.isNotEmpty(dcaAggregateItem.getClusterTaxon())) {
                valueMap.get("clusterTaxon").add(dcaAggregateItem.getClusterTaxon());
            }

            for (String tag : dcaAggregateItem.getTags()) {
                valueMap.get("tags").add(tag);
            }
        }
        return valueMap;
    }
}
