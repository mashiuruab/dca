package com.sannsyn.dca.vaadin.widgets.operations.controller.component.pipelines.pipeline;

import com.sannsyn.dca.vaadin.component.custom.field.DCALabel;
import com.sannsyn.dca.vaadin.view.DCALayoutContainer;
import com.sannsyn.dca.vaadin.widgets.operations.controller.model.aggregates.DCAEnsembles;
import com.vaadin.event.LayoutEvents;
import com.vaadin.server.Page;
import com.vaadin.ui.CssLayout;

import java.util.*;

/**
 * Created by mashiur on 5/24/16.
 */
public class DCAJoinContainerWrapperComponent extends CssLayout {
    private DCALabel afterSelectionWrapper = new DCALabel("&nbsp", "after-selection-wrapper");
    private DCAJoinContainerWrapperComponent currentComponent;

    private DCASelectionComponent dcaSelectionComponent;
    private DCASubRegularPipeLineComponent subRegularPipeLineComponent;
    private DCASubRegularPipeLineComponent additionalComponent;
    private CssLayout tableCssWrapperLayout = new CssLayout();
    private DCALayoutContainer layoutContainer;

    public DCAJoinContainerWrapperComponent(String joinItem, DCASubRegularPipeLineComponent dcaSubRegularPipeLineComponent,
                                            Map<String, List<String>> pipesDropDownItems, CssLayout parentContainer,
                                            DCALayoutContainer layoutContainer) {
        this.currentComponent = this;
        this.layoutContainer = layoutContainer;

        this.setStyleName("join-container-wrapper");
        this.tableCssWrapperLayout.setStyleName("table-wrapper-layout");

        String  removeIconId = UUID.randomUUID().toString();
        dcaSelectionComponent = new DCASelectionComponent(joinItem, pipesDropDownItems, this.layoutContainer);
        dcaSelectionComponent.addComponent(afterSelectionWrapper);
        dcaSelectionComponent.getRemoveIconComponent().setId(removeIconId);

        dcaSelectionComponent.getSelectedComponent().addLayoutClickListener(new LayoutEvents.LayoutClickListener() {
            @Override
            public void layoutClick(LayoutEvents.LayoutClickEvent event) {
                if (event.getChildComponent() != null && removeIconId.equals(event.getChildComponent().getId())) {
                    parentContainer.removeComponent(currentComponent);
                    if (parentContainer instanceof DCAPipeSourceComponent) {
                        DCAPipeSourceComponent sourceComponent = (DCAPipeSourceComponent) parentContainer;
                        sourceComponent.addComponent(sourceComponent.getSourceWrapperComponent());
                    }
                    Page.getCurrent().getJavaScript().execute("calculatePipeLineWidth()");
                }
            }
        });

        this.addComponent(dcaSelectionComponent);
        this.addComponent(getTableCssWrapperLayout());
        if (dcaSubRegularPipeLineComponent != null) {
            addPipeLineComponent(dcaSubRegularPipeLineComponent);
        }
    }


    public void addPipeLineComponent(DCASubRegularPipeLineComponent subRegularPipeLineComponent) {
        this.subRegularPipeLineComponent = subRegularPipeLineComponent;
        DCATableLayout tableLayout = new DCATableLayout();
        tableLayout.addComponent(this.subRegularPipeLineComponent);
        getTableCssWrapperLayout().addComponent(tableLayout);
        this.additionalComponent = new DCASubRegularPipeLineComponent(this.subRegularPipeLineComponent.getPipesDropDownItems(),
                this.subRegularPipeLineComponent.getSourceDropDownItems(), this.layoutContainer);
    }

    private boolean isLastBranch(DCASubRegularPipeLineComponent currentBranch) {
        DCATableLayout currentTableLayout = (DCATableLayout) currentBranch.getParent();
        CssLayout currentTableWrapperLayout = (CssLayout) currentTableLayout.getParent();

        return currentTableWrapperLayout.getComponentIndex(currentTableLayout) == currentTableWrapperLayout.getComponentCount() - 1;
    }

    public void addBranch(DCASubRegularPipeLineComponent currentBranch) {
        if (isLastBranch(currentBranch)) {
            DCASubRegularPipeLineComponent branch = new DCASubRegularPipeLineComponent(currentBranch.getPipesDropDownItems(),
                    currentBranch.getSourceDropDownItems(), this.layoutContainer);
            DCATableLayout tableLayout = new DCATableLayout();
            tableLayout.addComponent(branch);
            getTableCssWrapperLayout().addComponent(tableLayout);
        }
    }

    public Map<Object, List<Object>> getItem(DCAEnsembles ensembles) {
        Map<Object, List<Object>> joinItem = new LinkedHashMap<>();

        joinItem.put(dcaSelectionComponent.getValue(), new ArrayList<>());

        for(int counter = 0; counter < this.getTableCssWrapperLayout().getComponentCount(); counter++) {
            if (!(this.getTableCssWrapperLayout().getComponent(counter) instanceof DCATableLayout)) {
                continue;
            }

            DCATableLayout dcaTableLayout = (DCATableLayout) this.getTableCssWrapperLayout().getComponent(counter);

            if (dcaTableLayout.getComponentCount() == 1 && dcaTableLayout.getComponent(0) instanceof DCASubRegularPipeLineComponent) {
                DCASubRegularPipeLineComponent subRegularPipeLineComponent = (DCASubRegularPipeLineComponent) dcaTableLayout.getComponent(0);
                Object itemObject = subRegularPipeLineComponent.getItems(ensembles);

                if (itemObject instanceof List && ((List) itemObject).isEmpty()) {
                    continue;
                }

                Object pipeLineTypeSource = subRegularPipeLineComponent.handleBracketComponent(subRegularPipeLineComponent, itemObject, ensembles);
                joinItem.get(dcaSelectionComponent.getValue()).add(pipeLineTypeSource);
            }

        }
        return joinItem;
    }

    public CssLayout getTableCssWrapperLayout() {
        return tableCssWrapperLayout;
    }
}
