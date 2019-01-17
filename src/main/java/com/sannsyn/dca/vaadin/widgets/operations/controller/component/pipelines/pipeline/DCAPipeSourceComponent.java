package com.sannsyn.dca.vaadin.widgets.operations.controller.component.pipelines.pipeline;

import com.google.gson.Gson;
import com.sannsyn.dca.vaadin.component.custom.DCAWrapper;
import com.sannsyn.dca.vaadin.widgets.operations.controller.component.DCAWidgetContainerComponent;
import com.sannsyn.dca.vaadin.widgets.operations.controller.model.aggregates.DCAEnsembles;
import com.sannsyn.dca.vaadin.widgets.operations.controller.model.aggregates.DCATaskObject;
import com.vaadin.server.Page;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.dialogs.ConfirmDialog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by mashiur on 5/25/16.
 */
public class DCAPipeSourceComponent extends DCAWidgetContainerComponent {
    private static Logger logger = LoggerFactory.getLogger(DCAPipeSourceComponent.class);

    private CssLayout sourceWrapperComponent;
    private DCADropDownComponent sourceDropDownComponent;
    private DCARemoveButtonComponent removeBranchComponent;

    private CssLayout itemContainer = new CssLayout(){
        @Override
        public void addComponent(Component c) {
            super.addComponent(c);
            if (c instanceof DCASelectionComponent) {
                addRemoveIconAfterSource();
            }
        }

        @Override
        public void addComponent(Component c, int index) {
            super.addComponent(c, index);
            if (c instanceof DCASelectionComponent) {
                addRemoveIconAfterSource();
            }
        }
    };

    public DCADropDownComponent getSourceDropDownComponent() {
        return sourceDropDownComponent;
    }

    public void setSourceDropDownComponent(DCADropDownComponent sourceDropDownComponent) {
        this.sourceDropDownComponent = sourceDropDownComponent;
        this.sourceDropDownComponent.setId(String.format("source-%s", this.sourceDropDownComponent.getId()));
    }

    public void setSourceWrapperComponent() {
        this.sourceWrapperComponent = new DCAWrapper(Collections.singletonList(sourceDropDownComponent), "source-wrapper");
    }

    public void setRemoveBranchComponent() {
        this.removeBranchComponent = new DCARemoveButtonComponent();
        this.removeBranchComponent.getButton().addClickListener(clickEvent -> {

            String dialogMessage = String.format("You are about to delete a branch in the pipeline. Do you want to proceed?");

            ConfirmDialog confirmDialog = ConfirmDialog.show(getUI(), dialogMessage, new ConfirmDialog.Listener() {
                public void onClose(ConfirmDialog dialog) {
                    if (dialog.isConfirmed()) {
                        if(sourceWrapperComponent.getParent() instanceof DCASubRegularPipeLineComponent) {
                            CssLayout tableWrapperLayout = (CssLayout) sourceWrapperComponent.getParent().getParent().getParent();
                            CssLayout tableLayoutToRemove = (CssLayout) sourceWrapperComponent.getParent().getParent();
                            tableWrapperLayout.removeComponent(tableLayoutToRemove);
                        } else if (sourceWrapperComponent.getParent() instanceof DCARegularPipeComponent){
                            CssLayout parentContainer = (CssLayout) sourceWrapperComponent.getParent().getParent();
                            DCARegularPipeComponent componentToRemove = (DCARegularPipeComponent) sourceWrapperComponent.getParent();
                            parentContainer.removeComponent(componentToRemove);
                        }

                        Page.getCurrent().getJavaScript().execute("calculatePipeLineWidth()");
                    }
                }
            });

            confirmDialog.getOkButton().setStyleName("btn-primary");
            confirmDialog.getCancelButton().setStyleName("btn-primary");
            confirmDialog.setCaption("");
        });
    }

    public DCARemoveButtonComponent getRemoveBranchComponent() {
        return removeBranchComponent;
    }

    public CssLayout getSourceWrapperComponent() {
        return sourceWrapperComponent;
    }

    public CssLayout getItemContainer() {
        return itemContainer;
    };

    public Object getItems(DCAEnsembles ensembles) {
        List<Object> pipeItemList = new ArrayList<>();

        for (int counter = 0; counter < this.getComponentCount(); counter++) {
            Component itemComponent = this.getComponent(counter);
            if (itemComponent instanceof DCAJoinContainerWrapperComponent) {
                DCAJoinContainerWrapperComponent containerWrapperComponent = (DCAJoinContainerWrapperComponent) itemComponent;
                pipeItemList.add(containerWrapperComponent.getItem(ensembles));
            } else {
                populateItems(ensembles, pipeItemList, itemComponent);
            }
        }

        if (getSourceDropDownComponent() != null && !getSourceDropDownComponent().getValue().isEmpty()) {
            pipeItemList.add(getSourceDropDownComponent().getValue());
        } else if (this.getComponentIndex(getSourceWrapperComponent()) != -1 &&
                getSourceDropDownComponent() != null && getSourceDropDownComponent().getValue().isEmpty()) {
            if (!pipeItemList.isEmpty()) {
                throw new RuntimeException("The pipeline is not valid due to an empty source.");
            }
        }

        if (pipeItemList.size() == 1) {
            return pipeItemList.get(0);
        }

        return pipeItemList;
    }

    private void populateItems(DCAEnsembles ensembles, List<Object> pipeItemList, Component itemComponent) {
        CssLayout layout = (CssLayout) itemComponent;
        for (int counter = 0; counter < layout.getComponentCount(); counter++) {
            Component component = layout.getComponent(counter);
            if (component instanceof DCASelectionComponent) {
                DCASelectionComponent selectionComponent = (DCASelectionComponent) component;
                pipeItemList.add(selectionComponent.getValue());
            } else if (component instanceof DCAPipeSourceComponent) {
                DCAPipeSourceComponent pipeSourceComponent = (DCAPipeSourceComponent) component;

                Object pipeLineTypeSource = handleBracketComponent(pipeSourceComponent, pipeSourceComponent.getItems(ensembles), ensembles);
                pipeItemList.add(pipeLineTypeSource);
            }
        }
    }

    public Object handleBracketComponent(DCAPipeSourceComponent pipeSourceComponent, Object pipeItemList, DCAEnsembles ensembles) {
        if (StringUtils.isNotEmpty(pipeSourceComponent.getId())) {
            DCATaskObject taskObject = ensembles.getTasks().get(pipeSourceComponent.getId());
            taskObject.setEnsembles(ensembles);

            if (!(pipeItemList instanceof List)) {
                pipeItemList = Collections.singletonList(pipeItemList);
            }

            if (equals(taskObject.getChain(), pipeItemList)) {
                return String.format("(%s)", pipeSourceComponent.getId());
            }
        }

        return pipeItemList;
    }

    private boolean equals(Object originalObjectList, Object modifiedUIObjectList) {
        Gson gson = new Gson();
        String originalObjectString = gson.toJson(originalObjectList);
        String modifiedString = gson.toJson(modifiedUIObjectList);
        return originalObjectString.equals(modifiedString);
    }

    public void addRemoveIconAfterSource() {
        if (getSourceWrapperComponent().getComponentCount() == 1) {
            addComponentAsLast(getRemoveBranchComponent(), getSourceWrapperComponent());
        }
    }
}
