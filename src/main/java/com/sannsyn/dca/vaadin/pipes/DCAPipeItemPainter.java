package com.sannsyn.dca.vaadin.pipes;

import com.sannsyn.dca.vaadin.component.custom.container.AlternatingRowClass;
import com.sannsyn.dca.vaadin.component.custom.container.ItemPainter;
import com.sannsyn.dca.vaadin.component.custom.icon.DCAAddNewIcon;
import com.sannsyn.dca.vaadin.widgets.controller.overview.model.aggregates.DCAPipe;
import com.vaadin.event.LayoutEvents;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import org.apache.commons.lang3.StringUtils;

import static com.sannsyn.dca.vaadin.widgets.controller.overview.model.aggregates.DCAPipeInstanceType.SPECIFICATION;
import static com.vaadin.server.Sizeable.Unit.PERCENTAGE;

/**
 * Paints a Pipe item for the create/edit pipe ui.
 * <p>
 * Created by jobaer on 6/17/16.
 */
class DCAPipeItemPainter implements ItemPainter<DCAPipe> {
    private final DCAPipeItemClickHandler handler;
    private AlternatingRowClass rowClass = new AlternatingRowClass("alternating-gray-color", "");

    DCAPipeItemPainter(DCAPipeItemClickHandler handler) {
        this.handler = handler;
    }

    @Override
    public ComponentContainer draw(DCAPipe item) {
        CssLayout layout = new CssLayout();
        layout.setStyleName("recommender-component-search-result-row");
        layout.addStyleName(rowClass.alt());
        layout.setWidth(100, PERCENTAGE);

        redraw(layout, item);
        return layout;
    }

    @Override
    public void redraw(ComponentContainer comp, DCAPipe item) {
        comp.removeAllComponents();

        Component plusIcon = createPlusIcon(item);
        comp.addComponent(plusIcon);
        CssLayout details = createItemDetails(item);
        comp.addComponent(details);
    }

    private Component createPlusIcon(DCAPipe item) {
        Layout iconWrapper = new CssLayout();
        iconWrapper.setWidth(6, PERCENTAGE);
        if (SPECIFICATION.equals(item.getComponentType())) {
            DCAAddNewIcon addIcon = new DCAAddNewIcon("plus-icon-without-margin");
            addIcon.addLayoutClickListener((LayoutEvents.LayoutClickListener) event -> handler.onClick(item));
            iconWrapper.addComponent(addIcon);
        } else {
            Label space = new Label("");
            iconWrapper.addComponent(space);
        }
        return iconWrapper;
    }

    private CssLayout createItemDetails(DCAPipe item) {
        CssLayout layout = new CssLayout();
        layout.setStyleName("recommender-component-search-result-item-details");
        layout.setWidth(94, PERCENTAGE);

        addName(item, layout);
        addType(item, layout);
        addDescription(item, layout);

        layout.addLayoutClickListener(event -> handler.onClick(item));
        return layout;
    }

    private void addDescription(DCAPipe item, CssLayout layout) {
        String description = item.getComponentDescription();
        description = StringUtils.abbreviate(description, 80);
        Label descriptionLabel = new Label(description, ContentMode.HTML);
        descriptionLabel.setWidth(60, PERCENTAGE);
        descriptionLabel.addStyleName("recommender-component-search-result-col");
        layout.addComponent(descriptionLabel);
    }

    private void addType(DCAPipe item, CssLayout layout) {
        String type = item.getType();
        if("pipe".equalsIgnoreCase(type)) {
            type = "join";
        }
        Label typeLabel = new Label(type);
        typeLabel.setWidth(13, PERCENTAGE);
        typeLabel.addStyleName("recommender-component-search-result-col");
        layout.addComponent(typeLabel);
    }

    private void addName(DCAPipe item, CssLayout layout) {
        Label nameLabel = new Label(item.getName());
        nameLabel.setWidth(27, PERCENTAGE);
        nameLabel.addStyleName("recommender-component-search-result-col");
        layout.addComponent(nameLabel);
    }
}

interface DCAPipeItemClickHandler {
    void onClick(DCAPipe item);
}
