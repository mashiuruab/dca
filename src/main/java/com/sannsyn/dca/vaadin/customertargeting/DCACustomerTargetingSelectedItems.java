package com.sannsyn.dca.vaadin.customertargeting;

import com.sannsyn.dca.metadata.DCAItem;
import com.sannsyn.dca.model.config.DCAWidget;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import static com.sannsyn.dca.vaadin.ui.DCAUiHelper.wrapWithCssLayout;
import static com.vaadin.server.Sizeable.Unit.PERCENTAGE;

/**
 * A component for showing the selected items.
 * <p>
 * Created by jobaer on 8/22/16.
 */
//todo make it generic
class DCACustomerTargetingSelectedItems extends CustomComponent {
    private static final Logger logger = LoggerFactory.getLogger(DCACustomerTargetingSelectedItems.class);
    private final Consumer<DCAItem> removedAction;
    private List<DCAItem> selectedItems = new ArrayList<>();
    private ObjectProperty<List> itemsProperty = new ObjectProperty<List>(new ArrayList(), List.class);
    private DCACustomerTargetingPopup popupComponent;
    private Label label = new Label("Select items for targeting: ", ContentMode.HTML);

    private Label showNumber = new Label();

    DCACustomerTargetingSelectedItems(DCAWidget widgetConfig, Consumer<DCAItem> removedAction) {
        this.removedAction = removedAction;
        Layout layout = createSelectedItems(widgetConfig);
        setCompositionRoot(layout);
    }

    private Layout createSelectedItems(DCAWidget widgetConfig) {
        CssLayout cssLayout = new CssLayout();
        cssLayout.addStyleName("customer-targeting-selected-items");

        CssLayout labelWrapper = new CssLayout();
        labelWrapper.addStyleName("select-label-wrapper");
        label.addStyleName("select-label");
        labelWrapper.addComponent(label);
        label.setVisible(false);
        cssLayout.addComponent(labelWrapper);

        CssLayout buttonWrapper = new CssLayout();
        buttonWrapper.setWidth(93, PERCENTAGE);
        buttonWrapper.addStyleName("select-button-wrapper");

        showNumber.setWidth(10, PERCENTAGE);
        showNumber.addStyleName("selected-items-count");
        showNumber.setValue(" " + selectedItems.size());

        Label selectedItemsLabel = new Label("Selected Items");
        selectedItemsLabel.addStyleName("selected-items-label");
        selectedItemsLabel.setWidth(90, PERCENTAGE);
        buttonWrapper.addComponent(selectedItemsLabel);
        buttonWrapper.addComponent(showNumber);

        CssLayout popupLayout = new CssLayout();
        popupLayout.addStyleName("selected-popup");

        popupComponent = new DCACustomerTargetingPopup(widgetConfig, this::removeBook);
        popupLayout.addComponent(popupComponent);
        buttonWrapper.addComponent(popupLayout);

        buttonWrapper.addLayoutClickListener(click -> {
            String styleName = popupLayout.getStyleName();
            if (styleName.contains("visible")) {
                popupLayout.removeStyleName("visible");
            } else {
                popupLayout.addStyleName("visible");
            }
        });

        CssLayout layout = wrapWithCssLayout(buttonWrapper, "select-button-layout");
        layout.setWidth(30, PERCENTAGE);
        cssLayout.addComponent(layout);

        return cssLayout;
    }

    void add(DCAItem b) {
        if (selectedItems.contains(b)) return;

        selectedItems.add(b);
        itemsProperty.setValue(selectedItems);
        showNumber.setValue(" " + selectedItems.size());
        popupComponent.addItem(b);
    }

    private void removeBook(DCAItem b) {
        selectedItems.remove(b);
        itemsProperty.setValue(selectedItems);
        showNumber.setValue(" " + selectedItems.size());

        removedAction.accept(b);
    }

    void setLabelVisibility(boolean visible) {
        label.setVisible(visible);
    }

    boolean contains(DCAItem b) {
        return selectedItems.contains(b);
    }

    List<DCAItem> getItems() {
        return Collections.unmodifiableList(selectedItems);
    }
}
