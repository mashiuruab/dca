package com.sannsyn.dca.vaadin.inspectassembly;

import com.google.gson.JsonObject;
import com.sannsyn.dca.service.inspacetassembly.DCAInspectAssemblyService;
import com.sannsyn.dca.vaadin.component.custom.field.DCAComboBox;
import com.sannsyn.dca.vaadin.component.custom.field.DCALabel;
import com.sannsyn.dca.vaadin.component.custom.field.DCATextFieldWithSeparateLabel;
import com.vaadin.ui.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.vaadin.server.Sizeable.Unit.PERCENTAGE;

/**
 * The input component for Inspect Assembly Widget
 * Created by jobaer on 1/4/17.
 */
class DCAInspectAssemblySearchInput extends CustomComponent {
    private final UI ui;
    private DCAInspectAssemblyService assemblyService = new DCAInspectAssemblyService();
    private static final Logger logger = LoggerFactory.getLogger(DCAInspectAssemblySearchInput.class);

    private final BiConsumer<String, String> searchCallback;
    private final Consumer<JsonObject> selectionCallback;

    DCAInspectAssemblySearchInput(UI ui, Consumer<JsonObject> selectionCallback, BiConsumer<String, String> searchCallback) {
        this.ui = ui;
        this.searchCallback = searchCallback;
        this.selectionCallback = selectionCallback;
        Component componentRoot = buildRootComponent();
        setCompositionRoot(componentRoot);
    }

    private Component buildRootComponent() {
        CssLayout layout = new CssLayout();
        layout.addStyleName("inspect-assembly-input");
        layout.setWidth(100, PERCENTAGE);

        DCALabel dcaLabel = new DCALabel("Assembly : ", "assembly-label");
        dcaLabel.setWidth(15, PERCENTAGE);
        layout.addComponent(dcaLabel);

        DCAComboBox assemblyList = new DCAComboBox();
        assemblyList.setWidth(35, PERCENTAGE);
        layout.addComponent(assemblyList);
        initializeAssemblyList(assemblyList);

        Label separator = new Label();
        separator.setWidth(3, PERCENTAGE);
        layout.addComponent(separator);

        DCATextFieldWithSeparateLabel inputId = new DCATextFieldWithSeparateLabel("Input Id : ");
        layout.addComponent(inputId);
        inputId.setWidth(27, PERCENTAGE);
        layout.addComponent(inputId);

        Button search = new Button("SEARCH");
        search.addStyleName("btn-primary-style");
        search.setWidth(20, PERCENTAGE);

        search.addClickListener(event -> {
            Object value = assemblyList.getValue();
            if(value == null) return;
            String assemblyName = value.toString();
            String inputValue = inputId.getValue();
            searchCallback.accept(assemblyName, inputValue);
        });

        layout.addComponent(search);

        return layout;
    }

    private void initializeAssemblyList(DCAComboBox assemblyList) {
        assemblyService.getAssemblyObjects().subscribe(assemblies -> {
            List<String> names = assemblies.entrySet().stream().map(Map.Entry::getKey).collect(Collectors.toList());
            updateNames(assemblyList, names);

            assemblyList.addValueChangeListener(event -> {
                if (event.getProperty().getValue() == null) return;
                String assemblyName = event.getProperty().getValue().toString();
                if (assemblies.has(assemblyName)) {
                    requestForUpdateAssemblyInfo(assemblies.getAsJsonObject(assemblyName));
                }
            });

        }, e -> logger.warn("Unable to fetch assembly names.", e));
    }

    private void updateNames(DCAComboBox assemblyList, List<String> names) {
        ui.access(() -> names.forEach(assemblyList::addItem));
    }

    private void requestForUpdateAssemblyInfo(JsonObject assemblyObject) {
        selectionCallback.accept(assemblyObject);
    }
}