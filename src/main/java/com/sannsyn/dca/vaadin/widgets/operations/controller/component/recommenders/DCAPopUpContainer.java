package com.sannsyn.dca.vaadin.widgets.operations.controller.component.recommenders;

import com.sannsyn.dca.service.DCAPipesService;
import com.sannsyn.dca.vaadin.component.custom.DCAWrapper;
import com.sannsyn.dca.vaadin.component.custom.field.DCALabel;
import com.sannsyn.dca.vaadin.component.custom.logout.DCAModalComponent;
import com.sannsyn.dca.vaadin.pipes.DCAPipeEditForm;
import com.sannsyn.dca.vaadin.widgets.controller.overview.model.aggregates.DCAPipe;
import com.sannsyn.dca.vaadin.widgets.operations.controller.component.DCAPopupErrorComponent;
import com.sannsyn.dca.vaadin.widgets.operations.controller.component.DCAPopupMessageComponent;
import com.sannsyn.dca.vaadin.widgets.operations.controller.component.DCAWidgetContainerComponent;
import com.sannsyn.dca.vaadin.widgets.operations.controller.model.aggregates.DCAServiceConfig;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.DragAndDropWrapper;

import java.util.*;

/**
 * Created by mashiur on 4/11/17.
 */
public class DCAPopUpContainer extends DCAWidgetContainerComponent {
    public static final String LOCAL_TYPE_OF_FILTER = "Local";
    public static final String GLOBAL_TYPE_OF_FILTER = "Global";

    public static final String TARGET_FILTER_COMPONENT_CLS_NAME =
            "com.sannsyn.aggregator.computation.ensemble.impl.filters.FilterInputMatchingIds";

    public DCAServiceConfig serviceConfig;

    public DCAWrapper getHeadLineComponent(String titleText) {
        DCALabel headLineComponent = new DCALabel(titleText, "manage-filter-header");


        DCACloseIconComponent removeComponent = new DCACloseIconComponent();

        String randomId = UUID.randomUUID().toString();
        removeComponent.setId(randomId);

        DCAWrapper manageFilterHeaderComponent = new DCAWrapper(Arrays.asList(headLineComponent, removeComponent), "dca-widget-title-container");

        manageFilterHeaderComponent.addLayoutClickListener(event -> {
            if (event.getChildComponent() == null) {
                return;
            }

            if (randomId.equals(event.getChildComponent().getId())) {
                closePopUpWindow();
            }
        });

        return manageFilterHeaderComponent;
    }

    public void closePopUpWindow() {
        if(getCurrentComponent().getParent() instanceof DCAModalComponent) {
            DCAModalComponent modalComponent = (DCAModalComponent) getCurrentComponent().getParent();
            removeComponent(modalComponent, getLayoutContainer().getWidgetContainer());
        }
    }

    public boolean showInManageFilter(String filterPipeName) {
        if (this.serviceConfig.getExternal().isPresent()
                && this.serviceConfig.getExternal().get().getDca().isPresent()
                && this.serviceConfig.getExternal().get().getDca().get().containsKey(DCAPipesService.PIPES_KEY)
                && this.serviceConfig.getExternal().get().getDca().get().get(DCAPipesService.PIPES_KEY).containsKey(filterPipeName)) {

            Object mapObject = this.serviceConfig.getExternal().get().getDca().get().get(DCAPipesService.PIPES_KEY).get(filterPipeName);

            if (mapObject instanceof Map) {
                Map externalFilterMap = (Map) mapObject;
                return Boolean.valueOf(String.valueOf(externalFilterMap.get(DCAPipeEditForm.SHOW_IN_MANAGE_FILTER_KEY)));
            }
        }

        return false;
    }

    public DragAndDropWrapper getFilterItemDragWrapper(DCAManageFilterItemComponent manageFilterItemComponent) {
        DragAndDropWrapper filterItemWrapperComponent = new DragAndDropWrapper(manageFilterItemComponent);

        filterItemWrapperComponent.setStyleName("filter-item-wrapper");
        filterItemWrapperComponent.setDragStartMode(DragAndDropWrapper.DragStartMode.COMPONENT);
        filterItemWrapperComponent.setData(manageFilterItemComponent);

        return filterItemWrapperComponent;
    }

    public void setTypeOfFilter(DCAPipe pipe, String initialType) {
        if (!pipe.getClazz().equals(TARGET_FILTER_COMPONENT_CLS_NAME)) {
            pipe.setTypeOfFilter(String.format("%s (%s)", initialType, "System filter"));
        } else {
            pipe.setTypeOfFilter(initialType);
        }
    }

    public boolean isEditable(DCAPipe filterItem) {
        return filterItem.getClazz().equals(TARGET_FILTER_COMPONENT_CLS_NAME);
    }

    public List<String> getFilterInContainer(CssLayout container) {
        List<String> filterList = new ArrayList<>();

        for (int counter = 0; counter < container.getComponentCount(); counter++) {
            DragAndDropWrapper filterItemDragDropper = ((DragAndDropWrapper) container.getComponent(counter));
            DCAManageFilterItemComponent itemComponent = (DCAManageFilterItemComponent) filterItemDragDropper.getData();
            filterList.add(itemComponent.getPipeItem().getName());
        }

        return filterList;
    }

    /*This two part should be removed when the super class method would be merged*/
    public void showSuccessNotification(String message) {
        DCAPopupMessageComponent successMessageComponent = new DCAPopupMessageComponent("DONE: ",
                message, getCurrentComponent());
        addComponentAsLast(successMessageComponent, getCurrentComponent());
    }

    public void showErrorNotification(String message) {
        DCAPopupErrorComponent errorMessageComponent = new DCAPopupErrorComponent("ERROR: ",
                message, getCurrentComponent());
        addComponentAsLast(errorMessageComponent, getCurrentComponent());
    }
}
