package com.sannsyn.dca.vaadin.inspectaggregate;

import com.sannsyn.dca.metadata.DCAItem;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.CssLayout;

import static com.vaadin.server.Sizeable.Unit.PERCENTAGE;

public class ItemPainterListView extends AbstractItemPainter {

    @Override
    public ComponentContainer draw(DCAItem item) {
        return draw(item, "item-view-list");
    }

    protected void drawItems(ComponentContainer comp, DCAItem item) {
        addTitle(comp);
        CssLayout layout = new CssLayout();
        layout.setStyleName("item-container-div-horiz");
        layout.setWidth(100, PERCENTAGE);
        showProperties(item, layout);
        comp.addComponent(layout);
    }

    protected CssLayout prepareRow(String property, String value, String rowType) {
        return prepareRow(property, value, rowType, 20, 80);
    }
}
