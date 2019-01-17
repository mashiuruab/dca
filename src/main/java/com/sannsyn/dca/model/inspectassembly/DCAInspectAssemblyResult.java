package com.sannsyn.dca.model.inspectassembly;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * Model class for json mapping for InspectAssembly result
 *
 * Created by jobaer on 1/12/17.
 */
public class DCAInspectAssemblyResult {
    private String name;
    private List<String> sourceIds;
    private List<String> result;
    private List<DCAInspectAssemblyResult> children;
    // This will not come from json, but will be set manually
    private String path = "/ ";

    public String getPath() {
        return path;
    }

    public void addToPath(String parentPath, String path) {
        if(StringUtils.isNotBlank(parentPath)) {
            if(StringUtils.isNotBlank(path)) {
                this.path = parentPath + path + " / ";
            }
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getSourceIds() {
        return sourceIds;
    }

    public void setSourceIds(List<String> sourceIds) {
        this.sourceIds = sourceIds;
    }

    public List<String> getResult() {
        return result;
    }

    public void setResult(List<String> result) {
        this.result = result;
    }

    public List<DCAInspectAssemblyResult> getChildren() {
        return children;
    }

    public void setChildren(List<DCAInspectAssemblyResult> children) {
        this.children = children;
    }

    @Override
    public String toString() {
        return "DCAInspectAssemblyResult{" +
            "name='" + name + '\'' +
            ", sourceIds=" + sourceIds +
            ", result=" + result +
            ", children=" + children +
            ", path='" + path + '\'' +
            '}';
    }
}
