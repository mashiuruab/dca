package com.sannsyn.dca.vaadin.widgets.operations.controller.component.recommenders;

import com.sannsyn.dca.service.DCAPipesService;
import com.sannsyn.dca.vaadin.component.custom.DCAWrapper;
import com.sannsyn.dca.vaadin.component.custom.field.DCAError;
import com.sannsyn.dca.vaadin.component.custom.field.DCALabel;
import com.sannsyn.dca.vaadin.component.custom.logout.DCAModalComponent;
import com.sannsyn.dca.vaadin.icons.SannsynIcons;
import com.sannsyn.dca.vaadin.view.DCALayoutContainer;
import com.sannsyn.dca.vaadin.widgets.controller.overview.model.aggregates.DCAPipe;
import com.sannsyn.dca.vaadin.widgets.operations.controller.component.DCAPopupErrorComponent;
import com.sannsyn.dca.vaadin.widgets.operations.controller.component.DCAWidgetContainerComponent;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.DragAndDropWrapper;
import com.vaadin.ui.UI;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Created by mashiur on 1/11/17.
 */
public class DCAManageFilterItemComponent extends DCAWidgetContainerComponent {
    private static final Logger logger = LoggerFactory.getLogger(DCAManageFilterItemComponent.class);

    private DCAPipe pipeItem;
    private CssLayout parentContainer;

    private boolean withRemoveFunctionality;
    private boolean isEditable;
    private DCAPipesService pipesService;

    public DCAManageFilterItemComponent(DCAPipe pipeItem, CssLayout parentContainer,
                                        DCALayoutContainer layoutContainer, boolean withRemoveFunctionality,
                                        boolean isEditable) {
        setLayoutContainer(layoutContainer);
        this.setStyleName("filter-item");
        if (!isEditable) {
            this.addStyleName("read-only");
        }

        this.pipeItem = pipeItem;
        this.parentContainer = parentContainer;
        this.withRemoveFunctionality = withRemoveFunctionality;
        this.isEditable = isEditable;
        setCurrentComponent(this);
        this.pipesService = new DCAPipesService(getLoggedInUser());

        try {
            init();
        } catch (Exception e) {
            logger.error("Error : ", e);
            addComponentAsLast(new DCAError(e.getMessage()), getCurrentComponent());
        }
    }

    public DCAPipe getPipeItem() {
        return pipeItem;
    }

    public boolean isEditable() {
        return isEditable;
    }

    private CssLayout getOperationFilterIcons() {
        CssLayout editComponent = new CssLayout();
        editComponent.setStyleName("edit-filter-icon-container");

        CssLayout editIcon = new CssLayout();
        editIcon.setIcon(SannsynIcons.EDIT);
        DCAWrapper editIconComponent = new DCAWrapper(Collections.singletonList(editIcon), "edit-icon");

        String editFilterId = UUID.randomUUID().toString();
        editIconComponent.setId(editFilterId);

        String htmlInfoMessage = String.format("<span class='heading'>%s</span>" +
                "<span class='body'>%s</span>", pipeItem.getName(),
                StringUtils.abbreviate(pipeItem.getComponentDescription(), 60));

        DCALabel infoDescription = new DCALabel(htmlInfoMessage, "float-description");
        DCALabel upArrowIcon = new DCALabel("", "arrow-up");
        DCAWrapper infoPopupWrapper = new DCAWrapper(Arrays.asList(upArrowIcon, infoDescription),
                "float-description-wrapper");

        CssLayout infoIcon = new CssLayout();
        infoIcon.setIcon(SannsynIcons.HELP);
        DCAWrapper infoIconComponent = new DCAWrapper(Arrays.asList(infoIcon, infoPopupWrapper), "info-icon");


        DCACloseIconComponent removeComponent = new DCACloseIconComponent();

        String removeIconId = UUID.randomUUID().toString();
        removeComponent.setId(removeIconId);

        editComponent.addLayoutClickListener(event -> {
            if (event.getChildComponent() == null) {
                return;
            }

            if (removeIconId.equals(event.getChildComponent().getId())) {
                if (parentContainer.getComponentIndex(getCurrentComponent()) != - 1) {
                    removeComponent(getCurrentComponent(), parentContainer);
                } else if (getCurrentComponent().getParent() instanceof DragAndDropWrapper) {
                    removeComponent(getCurrentComponent().getParent(), parentContainer);
                }
            } else if (editFilterId.equals(event.getChildComponent().getId())) {
                if (!editIconComponent.isEnabled()) {
                    return;
                }

                UI.getCurrent().access(() -> {
                   editIconComponent.setEnabled(false);
                });

                Observable<List<DCAPipe>> pipeListObservable = pipesService
                        .getRecommenderComponents(Collections.singletonList(pipeItem.getName()));

                pipeListObservable.subscribe(this::loadEditFilter, this::onError, ()-> {
                    UI.getCurrent().access(() -> {
                        editIconComponent.setEnabled(true);
                    });
                });
            }
        });

        if (isEditable) {
            editComponent.addComponent(editIconComponent);
        }

        editComponent.addComponent(infoIconComponent);

        if (withRemoveFunctionality) {
            editComponent.addComponent(removeComponent);
        }

        return editComponent;
    }

    private void loadEditFilter(List<DCAPipe> pipeList) {
        if (pipeList.isEmpty()) {
            return;
        }

        this.pipeItem = pipeList.get(0);

        DCAModalComponent editFilterModalComponent = null;

        try {

            DCAEditFilterPopUpContainer editFilterContainer = new DCAEditFilterPopUpContainer(getPipeItem().getName(),
                    getLayoutContainer(), false);
            editFilterModalComponent = new DCAModalComponent(editFilterContainer);
            addComponentAsLast(editFilterModalComponent, getLayoutContainer().getWidgetContainer());
        } catch (Exception e) {
            logger.error("Error: ", e);
            removeComponent(editFilterModalComponent, getLayoutContainer().getWidgetContainer());
            DCAPopupErrorComponent errorComponent = new DCAPopupErrorComponent("ERROR:", e.getMessage(),
                    getCurrentComponent());
            addComponentAsLast(errorComponent, getLayoutContainer().getWidgetContainer());
        }
    }

    private void onError(Throwable throwable) {
        logger.error("Error : ", throwable);
        DCAPopupErrorComponent errorComponent = new DCAPopupErrorComponent("ERROR:",
                throwable.getMessage(), getCurrentComponent());
        addComponentAsLast(errorComponent, getCurrentComponent());
    }

    private void init() {
        DCAVerticalDotComponent verticalDots = new DCAVerticalDotComponent();
        this.addComponent(verticalDots);

        DCALabel itemNameComponent = new DCALabel(this.pipeItem.getName(), "item-name");
        this.addComponent(itemNameComponent);

        CssLayout operationUnitComponent = getOperationFilterIcons();

        DCALabel itemTypeComponent = new DCALabel(this.pipeItem.getTypeOfFilter(), "item-type");

        DCAWrapper itemUnitWrapper = new DCAWrapper(Arrays.asList(itemTypeComponent, operationUnitComponent),
                "item-unit-component");
        this.addComponent(itemUnitWrapper);
    }
}
