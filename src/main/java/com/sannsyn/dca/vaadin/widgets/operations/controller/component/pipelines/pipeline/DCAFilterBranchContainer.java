package com.sannsyn.dca.vaadin.widgets.operations.controller.component.pipelines.pipeline;

import com.sannsyn.dca.vaadin.view.DCALayoutContainer;
import com.sannsyn.dca.vaadin.widgets.operations.controller.component.DCAWidgetContainerComponent;
import com.vaadin.server.Page;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import org.vaadin.dialogs.ConfirmDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by mashiur on 3/7/17.
 */
public class DCAFilterBranchContainer extends DCAWidgetContainerComponent {

    private CssLayout currentComponent;
    private CssLayout chainContainer = new CssLayout() {
        @Override
        public void addComponent(Component c) {
            super.addComponent(c);
            Page.getCurrent().getJavaScript().execute("calculateFilterChainWidth()");
        }

        @Override
        public void removeComponent(Component c) {
            super.removeComponent(c);
            Page.getCurrent().getJavaScript().execute("calculateFilterChainWidth()");
        }
    };
    private DCARemoveButtonComponent removeButtonComponent = new DCARemoveButtonComponent();

    public DCAFilterBranchContainer(Map<String, List<String>> filterPipeItems, DCALayoutContainer layoutContainer) {
        this.setStyleName("filter-branch-container");
        this.chainContainer.setStyleName("filter-chain-container");
        currentComponent = this;

        removeButtonComponent.getButton().addClickListener(event -> {
            String dialogMessage = "You are about to delete the filter list in the pipeline. Do you want to proceed?";

            ConfirmDialog confirmDialog = ConfirmDialog.show(getUI(), dialogMessage, new ConfirmDialog.Listener() {
                public void onClose(ConfirmDialog dialog) {
                    if (dialog.isConfirmed()) {
                        chainContainer.removeAllComponents();
                        DCADropDownComponent filterDropDownComponent = new DCADropDownComponent(layoutContainer,
                                chainContainer, filterPipeItems);
                        chainContainer.addComponent(filterDropDownComponent);
                    }
                }
            });

            confirmDialog.getOkButton().setStyleName("btn-primary");
            confirmDialog.getCancelButton().setStyleName("btn-primary");
            confirmDialog.setCaption("");
        });

        this.addComponent(chainContainer);
        this.addComponent(removeButtonComponent);
    }

    public CssLayout getChainContainer() {
        return chainContainer;
    }

    public List<String> getFilters() {
        List<String> filterList = new ArrayList<>();

        for (int counter = 0; counter < chainContainer.getComponentCount(); counter++) {
            if (chainContainer.getComponent(counter) instanceof DCASelectionComponent) {
                DCASelectionComponent selectionComponent = (DCASelectionComponent) chainContainer.getComponent(counter);
                filterList.add(selectionComponent.getValue());
            }
        }

        return filterList;
    }
}
