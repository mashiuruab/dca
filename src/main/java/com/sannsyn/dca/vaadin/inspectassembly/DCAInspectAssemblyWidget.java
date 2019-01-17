package com.sannsyn.dca.vaadin.inspectassembly;

import com.google.gson.JsonObject;
import com.sannsyn.dca.service.inspacetassembly.DCAInspectAssemblyService;
import com.sannsyn.dca.vaadin.widgets.common.DCABreadCrumb;
import com.sannsyn.dca.vaadin.widgets.common.DCABreadCrumbImpl;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.UI;

import java.util.function.Consumer;

/**
 * The Inspect Assembly Widget. This is the main widget. It will create different child components to build the whole
 * widget.
 * <p>
 * Created by jobaer on 1/4/17.
 */
public class DCAInspectAssemblyWidget extends CustomComponent {
    private final Consumer<String> navigateToParent;

    public DCAInspectAssemblyWidget(Consumer<String> navigateToParent) {
        this.navigateToParent = navigateToParent;
        Component rootComponent = buildRootComponent();
        setCompositionRoot(rootComponent);
    }

    private Component buildRootComponent() {
        CssLayout layout = new CssLayout();
        layout.setWidth(100, Unit.PERCENTAGE);

        Component breadcrumb = setupBreadcrumb();
        layout.addComponent(breadcrumb);

        DCAInspectAssemblyRecommendersList resultGui = new DCAInspectAssemblyRecommendersList();
        DCAInspectAssemblyInformation infoComponent = createInfoComponent();
        DCAInspectAssemblySearchInput searchInput = createSearchInput(infoComponent, resultGui);

        layout.addComponent(searchInput);
        layout.addComponent(infoComponent);
        layout.addComponent(resultGui);

        return layout;
    }

    private Component setupBreadcrumb() {
        DCABreadCrumb breadCrumb = new DCABreadCrumbImpl();
        breadCrumb.addAction("Controller", navigateToParent);
        breadCrumb.addAction("Inspect Assembly", s -> {
        });
        return breadCrumb.getView();
    }

    private DCAInspectAssemblyInformation createInfoComponent() {
        return new DCAInspectAssemblyInformation();
    }

    private DCAInspectAssemblySearchInput createSearchInput(DCAInspectAssemblyInformation infoComponent, DCAInspectAssemblyRecommendersList resultGui) {
        final UI currentUi = UI.getCurrent();
        Consumer<JsonObject> selectionCallback =
            jsonObject ->
                currentUi.access(() -> infoComponent.updateInformation(jsonObject));
        return new DCAInspectAssemblySearchInput(currentUi, selectionCallback, (s, s2) -> performSearch(s, s2, resultGui));
    }

    private void performSearch(String assemblyName, String inputValue, DCAInspectAssemblyRecommendersList resultGui) {
        DCAInspectAssemblyService dcaInspectAssemblyService = new DCAInspectAssemblyService();
        dcaInspectAssemblyService.search(inputValue, assemblyName).subscribe(resultGui::updateResult);
    }
}
