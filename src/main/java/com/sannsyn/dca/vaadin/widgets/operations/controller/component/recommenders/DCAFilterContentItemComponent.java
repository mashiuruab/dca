package com.sannsyn.dca.vaadin.widgets.operations.controller.component.recommenders;

import com.sannsyn.dca.metadata.DCAItem;
import com.sannsyn.dca.vaadin.component.custom.DCAWrapper;
import com.sannsyn.dca.vaadin.component.custom.field.DCAError;
import com.sannsyn.dca.vaadin.component.custom.field.DCALabel;
import com.sannsyn.dca.vaadin.component.custom.navigation.DCAExternalImageComponent;
import com.sannsyn.dca.vaadin.view.DCALayoutContainer;
import com.sannsyn.dca.vaadin.widgets.operations.controller.component.DCAWidgetContainerComponent;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.DragAndDropWrapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by mashiur on 2/24/17.
 */
public class DCAFilterContentItemComponent extends DCAWidgetContainerComponent {
    private static final Logger logger = LoggerFactory.getLogger(DCAFilterContentItemComponent.class);

    private DCAFilterContentItemComponent currentComponent;
    private CssLayout parentContainer;
    private CheckBox selectItem;
    private DCAEditFilterPopUpContainer dependentParentLayout;

    private DCAItem contentItem;

    public DCAFilterContentItemComponent(DCAItem contentItem, CssLayout parentContainer, DCALayoutContainer layoutContainer) {
        this.contentItem = contentItem;
        setLayoutContainer(layoutContainer);
        this.parentContainer = parentContainer;
        currentComponent = this;

        try {
            init();
        } catch (Exception e) {
            logger.error("Error : ", e);
            addComponentAsLast(new DCAError(e.getMessage()), this);
        }
    }

    public DCAFilterContentItemComponent(DCAItem contentItem, CssLayout parentContainer, DCALayoutContainer layoutContainer,
                                         DCAEditFilterPopUpContainer editFilterContainer) {
        this(contentItem, parentContainer, layoutContainer);
        this.dependentParentLayout = editFilterContainer;
    }

    public CheckBox getSelectItem() {
        return selectItem;
    }

    public DCAItem getContentItem() {
        return contentItem;
    }

    private void updateSelectedItemCount() {
        if (dependentParentLayout == null) {
            return;
        }

        dependentParentLayout.updateCheckedItemCount();
        dependentParentLayout.getAllCheckBox().setValue(false);
        dependentParentLayout.getNoneCheckBox().setValue(false);
    }

    private void init() {
        this.setStyleName("filter-content-item");

        DCAVerticalDotComponent verticalDotComponent = new DCAVerticalDotComponent();
        this.addComponent(verticalDotComponent);

        selectItem = new CheckBox("", false);
        selectItem.setStyleName("checkbox-value");
        selectItem.addValueChangeListener(event -> {
            updateSelectedItemCount();
        });

        this.addComponent(selectItem);

        String itemTitleText = StringUtils.abbreviate(contentItem.getTitle() == null ?
                contentItem.getId() : contentItem.getTitle(), 55);
        DCALabel itemTitle = new DCALabel(itemTitleText, "content-item-title");
        this.addComponent(itemTitle);

        List<Component> imageAndRemoveIconComponentList = new ArrayList<>();

        if (StringUtils.isNotEmpty(contentItem.getThumbnail())) {

            DCAExternalImageComponent contentImage = new DCAExternalImageComponent(StringUtils.stripToEmpty(contentItem.getThumbnail()));
            contentImage.setStyleName("content-image");

            imageAndRemoveIconComponentList.add(contentImage);
        }

        DCACloseIconComponent removeComponent = new DCACloseIconComponent();
        removeComponent.addStyleName("filter-content-remove");
        String removeId = UUID.randomUUID().toString();
        removeComponent.setId(removeId);

        imageAndRemoveIconComponentList.add(removeComponent);

        DCAWrapper imageAndRemoveIconWrapper = new DCAWrapper(imageAndRemoveIconComponentList, "image-remove-wrapper");
        this.addComponent(imageAndRemoveIconWrapper);

        imageAndRemoveIconWrapper.addLayoutClickListener(event -> {
            if (event.getChildComponent() == null) {
                return;
            }

            if (removeId.equals(event.getChildComponent().getId())) {
                if (parentContainer.getComponentIndex(currentComponent) != -1) {
                    removeComponent(currentComponent, parentContainer);
                } else if (currentComponent.getParent() instanceof DragAndDropWrapper) {
                    removeComponent(currentComponent.getParent(), parentContainer);
                }
            }
        });
    }
}
