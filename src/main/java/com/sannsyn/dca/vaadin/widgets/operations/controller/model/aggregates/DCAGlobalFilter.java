package com.sannsyn.dca.vaadin.widgets.operations.controller.model.aggregates;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by mashiur on 3/9/17.
 */
public class DCAGlobalFilter {
    private Map<String, List<String>> taxa = new HashMap<>();
    private List<String> missingTaxon = new ArrayList<>();
    private List<String> all = new ArrayList<>();

    public Map<String, List<String>> getTaxa() {
        return taxa;
    }

    public List<String> getMissingTaxon() {
        return missingTaxon;
    }

    public List<String> getAll() {
        return all;
    }

    public void setTaxa(Map<String, List<String>> taxa) {
        this.taxa = taxa;
    }

    public void setMissingTaxon(List<String> missingTaxon) {
        this.missingTaxon = missingTaxon;
    }

    public void setAll(List<String> all) {
        this.all = all;
    }
}
