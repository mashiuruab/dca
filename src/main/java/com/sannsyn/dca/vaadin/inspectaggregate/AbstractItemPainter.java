package com.sannsyn.dca.vaadin.inspectaggregate;

import com.sannsyn.dca.metadata.DCAItem;
import com.sannsyn.dca.vaadin.component.custom.container.AlternatingRowClass;
import com.sannsyn.dca.vaadin.component.custom.container.ItemPainter;
import com.sannsyn.dca.vaadin.ui.DCAUiHelper;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import org.apache.commons.lang3.StringUtils;

import static com.vaadin.server.Sizeable.Unit.PERCENTAGE;

abstract class AbstractItemPainter implements ItemPainter<DCAItem> {
    protected CssLayout prepareRow(String property, String value, String rowType, int labelWidth, int valueWidth) {
        CssLayout idWrapper = new CssLayout();
        idWrapper.setStyleName(rowType, true);
        idWrapper.setWidth(100, PERCENTAGE);
        CssLayout idField = new CssLayout();
        idField.setWidth(labelWidth, PERCENTAGE);
        Label propertyName = new Label(property);
        propertyName.setStyleName("item-property-name");
        idField.addComponent(propertyName);
        idWrapper.addComponent(idField);

        CssLayout idValue = new CssLayout();
        idValue.setStyleName("item-property-value");
        idValue.setWidth(valueWidth, PERCENTAGE);
        Label id = new Label(value, ContentMode.HTML);
        Layout labelForSelection = DCAUiHelper.wrapLabelForSelection(id);
        idValue.addComponent(labelForSelection);
        idWrapper.addComponent(idValue);
        return idWrapper;
    }

    @Override
    public void redraw(ComponentContainer comp, DCAItem item) {
        comp.removeAllComponents();
        drawItems(comp, item);
    }

    protected void showProperties(DCAItem item, CssLayout layout) {
        AlternatingRowClass rowClass = new AlternatingRowClass("even", "odd");

        CssLayout idWrapper = prepareRow("Id:", item.getId(), rowClass.alt());
        layout.addComponent(idWrapper);

        if(StringUtils.isNotBlank(item.getTitle())) {
            CssLayout titleWrapper = prepareRow("Title:", item.getTitle(), rowClass.alt());
            layout.addComponent(titleWrapper);
        }

        if (StringUtils.isNotBlank(item.getAuthor())) {
            CssLayout authorWrapper = prepareRow("Author:", item.getAuthor(), rowClass.alt());
            layout.addComponent(authorWrapper);
        }

        if (item.getCount() > 0) {
            String count = "" + item.getCount();
            CssLayout countWrapper = prepareRow("Count:", count, rowClass.alt());
            layout.addComponent(countWrapper);
        }

        if (item.getSize() > 0) {
            String size = "" + item.getSize();
            CssLayout sizeWrapper = prepareRow("Size:", size, rowClass.alt());
            layout.addComponent(sizeWrapper);
        }

        if (item.getPopularity() != null && item.getPopularity() > 0) {
            CssLayout popularityWrapper = prepareRow("Popularity:", String.format("%.2f", item.getPopularity()), rowClass.alt());
            layout.addComponent(popularityWrapper);
        }

        if (item.getBoughtTogether() > 0) {
            String boughtAndTotal = item.getBoughtTogether() + "/" + item.getCount();
            CssLayout boughtTogether = prepareRow("Bought together/total:", boughtAndTotal, rowClass.alt());
            layout.addComponent(boughtTogether);
        }

        if (item.getScore() > 0) {
            String score = "" + item.getScore();
            CssLayout scoreWrapper = prepareRow("Score:", score, rowClass.alt());
            layout.addComponent(scoreWrapper);
        }

    }

    protected void addTitle(ComponentContainer comp) {
        CssLayout titleLayout = new CssLayout();
        titleLayout.setStyleName("item-title");
        String title = "Item Detail";
        Label c = new Label(title, ContentMode.HTML);
        titleLayout.addComponent(c);
        comp.addComponent(titleLayout);
    }

    protected abstract CssLayout prepareRow(String s, String id, String alt);

    protected ComponentContainer draw(DCAItem item, String styleName) {
        CssLayout comp = new CssLayout();
        comp.setStyleName(styleName);
        comp.setWidth(getWidthInPercentage(), PERCENTAGE);
        drawItems(comp, item);
        return comp;
    }

    protected float getWidthInPercentage(){
        return 98;
    }

    protected abstract void drawItems(ComponentContainer comp, DCAItem item);
}
