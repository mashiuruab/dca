package com.sannsyn.dca.vaadin.inspectaggregate;

import com.sannsyn.dca.metadata.DCAItem;
import com.vaadin.server.ExternalResource;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Image;
import org.apache.commons.lang3.StringUtils;

import static com.sannsyn.dca.vaadin.ui.DCAUiHelper.addSeparator;
import static com.vaadin.server.Sizeable.Unit.PERCENTAGE;
import static com.vaadin.server.Sizeable.Unit.PIXELS;

public class ItemPainterDetailsView extends AbstractItemPainter {
    private final float infoWidth;
    private final float imageWidth;

    public ItemPainterDetailsView(float infoWidth, float imageWidth) {
        this.infoWidth = infoWidth;
        this.imageWidth = imageWidth;
    }

    @Override
    public ComponentContainer draw(DCAItem item) {
        return draw(item, "item-view-details");
    }

    protected void drawItems(ComponentContainer comp, DCAItem item) {
        addTitle(comp);
        addSeparator(comp);
        CssLayout horizontalLayout = new CssLayout();
        horizontalLayout.setStyleName("item-container-div-horiz");
        horizontalLayout.setWidth(100, PERCENTAGE);

        CssLayout cssLayout = showInfo(item);
        horizontalLayout.addComponent(cssLayout);

        CssLayout imageLayout = new CssLayout();
        imageLayout.setStyleName("item-container-image");
        imageLayout.setWidth(imageWidth, PERCENTAGE);
        if (StringUtils.isNotEmpty(item.getThumbnail())) {
            ExternalResource resource = new ExternalResource(item.getThumbnail());
            Image image = new Image();
            image.setSource(resource);
            image.setWidth(getThumbnailWidth(), PIXELS);
            imageLayout.addComponent(image);
        }
        horizontalLayout.addComponent(imageLayout);
        comp.addComponent(horizontalLayout);
    }

    protected float getThumbnailWidth() {
        return 111;
    }

    private CssLayout showInfo(DCAItem item) {
        CssLayout layout = new CssLayout();
        layout.setStyleName("item-container-info");
        layout.setWidth(infoWidth, PERCENTAGE);
        showProperties(item, layout);
        return layout;
    }

    protected CssLayout prepareRow(String property, String value, String rowType) {
        return prepareRow(property, value, rowType, 30, 70);
    }
}
