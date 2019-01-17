package com.sannsyn.dca.vaadin.widgets.operations.controller.component;

import com.sannsyn.dca.vaadin.component.custom.field.DCALabel;
import com.vaadin.event.LayoutEvents;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.CssLayout;

import java.util.UUID;

/**
 * Created by mashiur on 5/31/16.
 */
public class DCAPopupMessageComponent extends CssLayout {
    private CssLayout currentComponent;

    public DCAPopupMessageComponent(String titleMessage, String subTitleMessage, ComponentContainer parentContainer) {
        this.setStyleName("popup-message-wrapper");
        currentComponent = this;

        String successMessage = String.format("<span class='title-msg'>%s</span><br/><p class='subtitle'>%s</p>", titleMessage, subTitleMessage);
        DCALabel successMessageLabel = new DCALabel(successMessage, "popup-message-label");

        DCALabel removeIcon = new DCALabel("", "popup-message-remove");
        removeIcon.setIcon(FontAwesome.REMOVE);
        String iconId = UUID.randomUUID().toString();
        removeIcon.setId(iconId);

        this.addLayoutClickListener(new LayoutEvents.LayoutClickListener() {
            @Override
            public void layoutClick(LayoutEvents.LayoutClickEvent event) {
                if (event.getClickedComponent() == null) {
                    return;
                }
                String clickedComponentId = event.getClickedComponent().getId();
                if (iconId.equals(clickedComponentId)) {
                    parentContainer.removeComponent(currentComponent);
                }
            }
        });

        this.addComponent(successMessageLabel);
        this.addComponent(removeIcon);
    }
}
