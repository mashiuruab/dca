package com.sannsyn.dca.vaadin.widgets.operations.controller.component.aggregates.tags;

import com.sannsyn.dca.vaadin.component.custom.field.DCALabel;
import com.sannsyn.dca.vaadin.view.DCALayoutContainer;
import com.sannsyn.dca.vaadin.widgets.operations.controller.component.DCAWidgetContainerComponent;
import com.vaadin.event.FieldEvents;
import com.vaadin.event.LayoutEvents;
import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ShortcutListener;
import com.vaadin.ui.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by mashiur on 4/21/16.
 */
public class DCATagsComponent extends DCAWidgetContainerComponent {
    private static final Logger logger = LoggerFactory.getLogger(DCATagsComponent.class);

    private DCALabel componentLabel = new DCALabel("", "");
    private  CssLayout tagsInputContainer = new CssLayout();
    private CssLayout dropDownItemWrapper;
    private TextField textInputField;

    private Set<String> items;
    private int dropDownItemStateCounter = -1;

    public DCATagsComponent(String labelName, Set<String> tagItems, DCALayoutContainer layoutContainer) {
        setLayoutContainer(layoutContainer);
        this.items = tagItems;

        this.componentLabel.setStyleName("tags-component-name");
        this.componentLabel.setValue(labelName);

        this.setStyleName("dca-tags-container");
        this.addComponent(componentLabel);

        init();
    }

    private void init() {
        tagsInputContainer.setStyleName("tags-item-container");

        CssLayout inputWrapper = new CssLayout();
        inputWrapper.setStyleName("tags-input-wrapper");

        CssLayout selectedItemContainer = new CssLayout();
        selectedItemContainer.setStyleName("tags-selected-item-container");


        textInputField = new TextField();
        textInputField.setStyleName("tags-text-field");

        tagsInputContainer.addComponent(textInputField);
        selectedItemContainer.addComponent(tagsInputContainer);

        CssLayout inputButton = new CssLayout();
        inputButton.setStyleName("tags-input-button");
        inputButton.setId("tags-button");

        inputWrapper.addComponent(selectedItemContainer);
        inputWrapper.addComponent(inputButton);

        this.dropDownItemWrapper = new CssLayout();
        dropDownItemWrapper.setStyleName("tags-dropdown-item-wrapper");
        dropDownItemWrapper.addLayoutClickListener(new LayoutEvents.LayoutClickListener() {
            @Override
            public void layoutClick(LayoutEvents.LayoutClickEvent event) {
                Component clickedComponent = event.getClickedComponent();
                Label clickedLabel = (Label) clickedComponent;

                addTagItem(clickedLabel.getValue());
                Component targetComponent = event.getComponent().getParent();
                targetComponent.removeStyleName("show-tags-dropdown-item");
                dropDownItemStateCounter = -1;
                handleDropDownItemKeyboardState();
            }
        });

        populateDropDownItems(dropDownItemWrapper, items);

        CssLayout valueContainer = new CssLayout();
        valueContainer.setId("tags-component-value-container-id");
        valueContainer.setStyleName("tags-component-value-container");
        valueContainer.setWidthUndefined();

        valueContainer.addComponent(inputWrapper);
        valueContainer.addComponent(dropDownItemWrapper);

        valueContainer.addShortcutListener(new ShortcutListener("Tags DropDownItem Arrow Down Key", ShortcutAction.KeyCode.ARROW_DOWN, null) {
            @Override
            public void handleAction(Object sender, Object target) {
                dropDownItemStateCounter++;
                handleDropDownItemKeyboardState();
            }
        });

        valueContainer.addShortcutListener(new ShortcutListener("Tags DropDownItem Arrow Up Key", ShortcutAction.KeyCode.ARROW_UP, null) {
            @Override
            public void handleAction(Object sender, Object target) {
                dropDownItemStateCounter--;
                handleDropDownItemKeyboardState();

            }
        });

        valueContainer.addShortcutListener(new ShortcutListener("Tags DropDownItem Enter Key", ShortcutAction.KeyCode.ENTER, null) {
            @Override
            public void handleAction(Object sender, Object target) {
                handleDropDownItemKeyboardState();

                if (dropDownItemStateCounter < 0 || dropDownItemStateCounter >= dropDownItemWrapper.getComponentCount()) {
                    if (StringUtils.isNotEmpty(textInputField.getValue())) {
                        addTagItem(textInputField.getValue());
                    }
                    return;
                }

                Label hoveredItem = (Label) dropDownItemWrapper.getComponent(dropDownItemStateCounter);
                addTagItem(hoveredItem.getValue());

                dropDownItemStateCounter = -1;

                valueContainer.removeStyleName("show-tags-dropdown-item");
            }
        });


        valueContainer.addLayoutClickListener(new LayoutEvents.LayoutClickListener() {
            @Override
            public void layoutClick(LayoutEvents.LayoutClickEvent layoutClickEvent) {
                Component clickedComponent = layoutClickEvent.getClickedComponent();
                String clickedComponentId = clickedComponent.getId();
                if ("tag-remove-icon-id".equals(clickedComponentId)) {
                    Component container = clickedComponent.getParent();
                    tagsInputContainer.removeComponent(container);
                }
            }
        });

        inputWrapper.addLayoutClickListener(new LayoutEvents.LayoutClickListener() {
            @Override
            public void layoutClick(LayoutEvents.LayoutClickEvent event) {
                textInputField.focus();
                populateDropDownItems(dropDownItemWrapper, items);

                if(event.getChildComponent() != null && "tags-button".equals(event.getChildComponent().getId())) {
                    String styleName = valueContainer.getStyleName();
                    if (styleName.contains("show-tags-dropdown-item")) {
                        valueContainer.removeStyleName("show-tags-dropdown-item");
                    } else {
                        valueContainer.addStyleName("show-tags-dropdown-item");
                    }
                }
            }
        });

        getLayoutContainer().getWidgetContainer().addLayoutClickListener(new LayoutEvents.LayoutClickListener() {
            @Override
            public void layoutClick(LayoutEvents.LayoutClickEvent event) {
                if (event.getClickedComponent() != null && !"tags-button".equals(event.getClickedComponent().getId())) {
                    valueContainer.removeStyleName("show-tags-dropdown-item");
                }
            }
        });

        getLayoutContainer().getLeftPanelContainer().addLayoutClickListener(new LayoutEvents.LayoutClickListener() {
            @Override
            public void layoutClick(LayoutEvents.LayoutClickEvent event) {
                valueContainer.removeStyleName("show-tags-dropdown-item");
            }
        });

        getLayoutContainer().getTopPanelContainer().addLayoutClickListener(new LayoutEvents.LayoutClickListener() {
            @Override
            public void layoutClick(LayoutEvents.LayoutClickEvent event) {
                valueContainer.removeStyleName("show-tags-dropdown-item");
            }
        });

        textInputField.addTextChangeListener(new FieldEvents.TextChangeListener() {
            @Override
            public void textChange(FieldEvents.TextChangeEvent event) {
                valueContainer.removeStyleName("show-tags-dropdown-item");
                String typedText = event.getText();


                if (!typedText.isEmpty()) {
                    Set<String> filteredList = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
                    items.forEach(item -> {
                        if (item.toLowerCase().startsWith(typedText)) {
                            filteredList.add(item);
                        }
                    });
                    if (filteredList.isEmpty()) {
                        filteredList.add("No matches found");
                    }
                    populateDropDownItems(dropDownItemWrapper, filteredList);
                } else {
                    populateDropDownItems(dropDownItemWrapper, items);
                }

                valueContainer.addStyleName("show-tags-dropdown-item");
            }
        });

        textInputField.setImmediate(true);
        textInputField.setTextChangeEventMode(AbstractTextField.TextChangeEventMode.EAGER);


        this.addComponent(valueContainer);
    }

    private void handleDropDownItemKeyboardState() {
        for (int counter = 0; counter < this.dropDownItemWrapper.getComponentCount(); counter++) {
            this.dropDownItemWrapper.getComponent(counter).removeStyleName("blue-background");
        }

        if (dropDownItemStateCounter >= this.dropDownItemWrapper.getComponentCount() || dropDownItemStateCounter < 0) {
            dropDownItemStateCounter = -1;
            return;
        }

        dropDownItemWrapper.getComponent(dropDownItemStateCounter).addStyleName("blue-background");
    }

    private boolean alreadyAdded(CssLayout layout, String tagItemText) {
        for (int counter = 0; counter < layout.getComponentCount() - 1; counter++) {
            DCATagsLabel dcaTagsLabel = (DCATagsLabel)layout.getComponent(counter);
            if (tagItemText.equals(dcaTagsLabel.getLabel().getValue())) {
                return true;
            }
        }
        return false;
    }

    private void addTagItem(String value) {
        if ("No matches found".equals(value) ||alreadyAdded(tagsInputContainer, value)) {
            return;
        }
        DCATagsLabel label = new DCATagsLabel(value);
        tagsInputContainer.removeComponent(textInputField);
        tagsInputContainer.addComponent(label, tagsInputContainer.getComponentCount());
        tagsInputContainer.addComponent(textInputField, tagsInputContainer.getComponentCount());
        textInputField.setValue("");
        textInputField.focus();
    }

    private void populateDropDownItems(CssLayout itemWrapper, Set<String> filteredList) {
        itemWrapper.removeAllComponents();
        filteredList.forEach(filteredItem -> {
            DCALabel label = new DCALabel(filteredItem, "tag-item");
            itemWrapper.addComponent(label);
        });
    }

    public List<String> getTagItemList() {
        List<String> itemList = new ArrayList<>();

        for (int counter = 0; counter < tagsInputContainer.getComponentCount() - 1; counter++) {
            DCATagsLabel dcaTagsLabel = (DCATagsLabel) tagsInputContainer.getComponent(counter);
            itemList.add(dcaTagsLabel.getLabel().getValue());
        }

        return itemList;
    }
}
