package com.sannsyn.dca.vaadin.inspectassembly;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Label;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static com.vaadin.server.Sizeable.Unit.PERCENTAGE;
import static org.apache.commons.lang3.StringUtils.splitByCharacterTypeCamelCase;

/**
 * The component that will show info regarding the assembly for Inspect Assembly Widget
 * <p>
 * Created by jobaer on 1/4/17.
 */
class DCAInspectAssemblyInformation extends CustomComponent {
    private static final Logger logger = LoggerFactory.getLogger(DCAInspectAssemblyInformation.class);
    private CssLayout rootLayout = new CssLayout();

    DCAInspectAssemblyInformation() {
        Component root = createComponentRoot();
        setCompositionRoot(root);
    }

    void updateInformation(JsonObject assembly) {
        logger.debug("Got request for update assembly information.");
        rootLayout.removeAllComponents();
        updateDescription(assembly);
        updateProperties(assembly);
    }

    private void updateDescription(JsonObject assembly) {
        if (assembly.has("description")) {
            String description = assembly.get("description").getAsString();
            addRow("Description : ", description);
        }
    }

    private void updateProperties(JsonObject assembly) {
        for (Map.Entry<String, JsonElement> childElementEntry : assembly.entrySet()) {
            String key = childElementEntry.getKey();
            JsonElement value = childElementEntry.getValue();
            if ("description".equals(key) || !value.isJsonPrimitive()) {
                continue;
            }
            String label = formatLabel(key);
            addRow(label, value.getAsString());
        }
    }

    private String formatLabel(String label) {
        String removeCamelCase = StringUtils.join(splitByCharacterTypeCamelCase(label), ' ');
        String capitalize = StringUtils.capitalize(removeCamelCase);
        return capitalize + " : ";
    }

    private Component createComponentRoot() {
        rootLayout.addStyleName("inspect-assembly-info-wrapper");
        rootLayout.setWidth(100, PERCENTAGE);
        return rootLayout;
    }

    private void addRow(String label, String value) {
        CssLayout row1 = new CssLayout();
        row1.addStyleName("inspect-assembly-info-row");
        row1.setWidth(100, PERCENTAGE);
        Label c = new Label(label);
        c.addStyleName("gray-font");
        c.setWidth(15, PERCENTAGE);
        row1.addComponent(c);

        Label content = new Label(value);
        content.setWidth(85, PERCENTAGE);
        row1.addComponent(content);

        rootLayout.addComponent(row1);
    }
}
