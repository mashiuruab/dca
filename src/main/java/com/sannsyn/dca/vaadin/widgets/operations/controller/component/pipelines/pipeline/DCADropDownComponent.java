package com.sannsyn.dca.vaadin.widgets.operations.controller.component.pipelines.pipeline;

import com.sannsyn.dca.vaadin.component.custom.field.DCALabel;
import com.sannsyn.dca.vaadin.component.custom.icon.DCAIcon;
import com.sannsyn.dca.vaadin.view.DCALayoutContainer;
import com.sannsyn.dca.vaadin.widgets.operations.controller.component.DCAWidgetContainerComponent;
import com.vaadin.event.LayoutEvents;
import com.vaadin.server.Page;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created by mashiur on 5/13/16.
 */
public class DCADropDownComponent extends DCAWidgetContainerComponent {
    private static final Logger logger = LoggerFactory.getLogger(DCADropDownComponent.class);

    private CssLayout inputContainer;
    private CssLayout dropdownContainer;
    private CssLayout selectedInput = new CssLayout();
    private DCADropDownComponent currentComponent;
    private String componentId = UUID.randomUUID().toString();
    private String downArrowIconId = UUID.randomUUID().toString();

    public CssLayout getSelectedInput() {
        return selectedInput;
    }

    public CssLayout getDropdownContainer() {
        return dropdownContainer;
    }

    private void init(boolean addNewIcon, DCALayoutContainer layoutContainer) {
        this.setId(componentId);

        currentComponent = this;
        this.setStyleName("pipeline-dropdown-component");


        inputContainer = new CssLayout();
        inputContainer.setStyleName("input-container");

        if (addNewIcon) {
            inputContainer.addComponent(getAddNewIcon());
        }

        DCAIcon downArrowIcon = new DCAIcon("icon-down", "down-arrow-icon");
        downArrowIcon.setId(downArrowIconId);

        this.selectedInput.setStyleName("selected-input");
        inputContainer.addComponent(selectedInput);
        inputContainer.addComponent(downArrowIcon);

        dropdownContainer = new CssLayout();
        dropdownContainer.setStyleName("dropdown-container");

        this.addComponent(inputContainer);
        this.addComponent(dropdownContainer);


        inputContainer.addLayoutClickListener(new LayoutEvents.LayoutClickListener() {
            @Override
            public void layoutClick(LayoutEvents.LayoutClickEvent event) {
                if (event.getChildComponent() != null && downArrowIconId.equals(event.getChildComponent().getId())) {
                    if (currentComponent.getStyleName().contains("open-dropdown")) {
                        currentComponent.removeStyleName("open-dropdown");
                    } else {
                        currentComponent.addStyleName("open-dropdown");
                    }
                }
            }
        });

        handleAllOtherDropDown(layoutContainer);
    }

    /*Faucet dropdown and outTaxon dropdown*/
    public DCADropDownComponent(List<String> items, DCALayoutContainer layoutContainer) {
        init(false, layoutContainer);

        this.addStyleName("no-plus-icon");

        for (String item : items) {
            DCALabel itemComponent = new DCALabel(item, "item");
            dropdownContainer.addComponent(itemComponent);
        }


        dropdownContainer.addLayoutClickListener(new LayoutEvents.LayoutClickListener() {
            @Override
            public void layoutClick(LayoutEvents.LayoutClickEvent event) {
                if (event.getChildComponent() != null) {
                    DCALabel itemClicked = (DCALabel) event.getChildComponent();
                    DCALabel newComponent = new DCALabel(itemClicked.getValue(), "item");
                    selectedInput.removeAllComponents();
                    selectedInput.addComponent(newComponent);
                    currentComponent.removeStyleName("open-dropdown");
                }
            }
        });
    }

    private void handleAllOtherDropDown(DCALayoutContainer layoutContainer) {
        layoutContainer.getWidgetContainer().addLayoutClickListener(event -> {
            String clickedComponentId = event.getClickedComponent() != null ? StringUtils.stripToEmpty(event.getClickedComponent().getId()) : "";
            if (!clickedComponentId.equals(downArrowIconId)) {
                currentComponent.removeStyleName("open-dropdown");
            }
        });

        layoutContainer.getLeftPanelContainer().addLayoutClickListener(event -> {
            String clickedComponentId = event.getClickedComponent() != null ? StringUtils.stripToEmpty(event.getClickedComponent().getId()) : "";
            if (!clickedComponentId.equals(downArrowIconId)) {
                currentComponent.removeStyleName("open-dropdown");
            }
        });

        layoutContainer.getTopPanelContainer().addLayoutClickListener(event -> {
            String clickedComponentId = event.getClickedComponent() != null ? StringUtils.stripToEmpty(event.getClickedComponent().getId()) : "";
            if (!clickedComponentId.equals(downArrowIconId)) {
                currentComponent.removeStyleName("open-dropdown");
            }
        });
    }

    /*It would be used for filter, transform and join type pipe*/
    public DCADropDownComponent(Map<String, List<String>> pipesDropDownItems, Map<String, List<String>> sourceDropDownItems,
                                CssLayout pipesComponentParentContainer, DCALayoutContainer layoutContainer) {
        init(true, layoutContainer);

        for (Map.Entry<String, List<String>> entry : pipesDropDownItems.entrySet()) {
            String headerName = entry.getKey();
            DCALabel headerComponent = new DCALabel(headerName, "item-header");
            headerComponent.setId(String.format("%s-id", headerName));
            dropdownContainer.addComponent(headerComponent);

            for (String item : entry.getValue()) {
                DCALabel itemComponent = new DCALabel(item, "item");
                dropdownContainer.addComponent(itemComponent);
            }
        }


        dropdownContainer.addLayoutClickListener(new LayoutEvents.LayoutClickListener() {
            @Override
            public void layoutClick(LayoutEvents.LayoutClickEvent event) {
                if (event.getChildComponent() != null) {
                    DCALabel itemClicked = (DCALabel) event.getChildComponent();
                    if (itemClicked.getId() != null) {
                        return;
                    }

                    int currentComponentIndex = pipesComponentParentContainer.getComponentIndex(currentComponent);

                    if (pipesDropDownItems.get("Joins").contains(itemClicked.getValue())) {
                        DCASubRegularPipeLineComponent subRegularPipeLineComponent = new DCASubRegularPipeLineComponent(pipesDropDownItems,
                                sourceDropDownItems, layoutContainer);

                        CssLayout parentContainer = (CssLayout)pipesComponentParentContainer.getParent();

                        DCAJoinContainerWrapperComponent joinContainerWrapperComponent = new DCAJoinContainerWrapperComponent(itemClicked.getValue(),
                                subRegularPipeLineComponent, pipesDropDownItems, parentContainer, layoutContainer);

                        if (parentContainer.getComponentCount() == 2) {
                            if (parentContainer.getComponent(1) instanceof DCAJoinContainerWrapperComponent) {
                                DCAJoinContainerWrapperComponent sourceJoinContainerWrapper = (DCAJoinContainerWrapperComponent) parentContainer.getComponent(1);

                                subRegularPipeLineComponent.addComponent(sourceJoinContainerWrapper);
                                subRegularPipeLineComponent.removeComponent(subRegularPipeLineComponent.getSourceWrapperComponent());
                                parentContainer.removeComponent(sourceJoinContainerWrapper);

                            } else if (parentContainer.getComponent(1) instanceof DCADropDownComponent) {
                                parentContainer.removeComponent(parentContainer.getComponent(1));
                            }

                            List<Component> componentList = new ArrayList<Component>();
                            List<Component> componentsToRemove = new ArrayList<Component>();

                            for (int counter = currentComponentIndex + 1; counter < pipesComponentParentContainer.getComponentCount(); counter++) {
                                if (pipesComponentParentContainer.getComponent(counter) instanceof DCADropDownComponent) {
                                    DCADropDownComponent newDropDownComponent = new DCADropDownComponent(pipesDropDownItems,
                                            sourceDropDownItems, subRegularPipeLineComponent.getItemContainer(), layoutContainer);
                                    componentList.add(newDropDownComponent);
                                    componentsToRemove.add(pipesComponentParentContainer.getComponent(counter));
                                } else {
                                    componentList.add(pipesComponentParentContainer.getComponent(counter));
                                }
                            }

                            for (Component component : componentList) {
                                subRegularPipeLineComponent.getItemContainer().addComponent(component);
                            }

                            for (Component component : componentsToRemove) {
                                pipesComponentParentContainer.removeComponent(component);
                            }
                        }

                        if (pipesComponentParentContainer.getParent() instanceof DCAPipeSourceComponent) {
                            DCAPipeSourceComponent sourceComponent = (DCAPipeSourceComponent) pipesComponentParentContainer.getParent();
                            sourceComponent.getSourceDropDownComponent().getSelectedInput().removeAllComponents();
                            sourceComponent.removeComponent(sourceComponent.getSourceWrapperComponent());
                        }


                        parentContainer.addComponent(joinContainerWrapperComponent);

                    } else {
                        DCADropDownComponent dcaDropDownComponentRight = new DCADropDownComponent(pipesDropDownItems, sourceDropDownItems,
                                pipesComponentParentContainer, layoutContainer);
                        String  removeIconId = UUID.randomUUID().toString();

                        DCASelectionComponent dcaSelectionComponent = new DCASelectionComponent(itemClicked.getValue(),
                                pipesDropDownItems, layoutContainer);
                        dcaSelectionComponent.getRemoveIconComponent().setId(removeIconId);

                        pipesComponentParentContainer.addComponent(dcaSelectionComponent, currentComponentIndex + 1);
                        int selectionComponentIndex = pipesComponentParentContainer.getComponentIndex(dcaSelectionComponent);
                        pipesComponentParentContainer.addComponent(dcaDropDownComponentRight, selectionComponentIndex + 1);

                        dcaSelectionComponent.getSelectedComponent().addLayoutClickListener(new LayoutEvents.LayoutClickListener() {
                            @Override
                            public void layoutClick(LayoutEvents.LayoutClickEvent event) {
                                if (event.getChildComponent() != null && removeIconId.equals(event.getChildComponent().getId())) {
                                    pipesComponentParentContainer.removeComponent(dcaSelectionComponent);
                                    pipesComponentParentContainer.removeComponent(dcaDropDownComponentRight);
                                    Page.getCurrent().getJavaScript().execute("calculatePipeLineWidth()");
                                }
                            }
                        });


                        if (pipesComponentParentContainer.getParent().getParent().getParent().getParent() instanceof DCAJoinContainerWrapperComponent
                                && pipesComponentParentContainer.getParent() instanceof DCASubRegularPipeLineComponent) {
                            DCASubRegularPipeLineComponent currentBranch = (DCASubRegularPipeLineComponent) pipesComponentParentContainer.getParent();
                            DCAJoinContainerWrapperComponent joinContainerWrapperComponent = (DCAJoinContainerWrapperComponent)
                                    pipesComponentParentContainer.getParent().getParent().getParent().getParent();
                            joinContainerWrapperComponent.addBranch(currentBranch);
                        }
                    }

                    Page.getCurrent().getJavaScript().execute("calculatePipeLineWidth()");
                    currentComponent.removeStyleName("open-dropdown");
                }
            }
        });
    }

    /*Source DropDown in pipeline*/
    public DCADropDownComponent(Map<String, List<String>> sourceItems, DCALayoutContainer layoutContainer) {
        init(false, layoutContainer);

        for (Map.Entry<String, List<String>> entry : sourceItems.entrySet()) {
            String headerName = entry.getKey();

            String itemSecondaryClassName = "";

            if (headerName.equals("Pipelines")) {
                itemSecondaryClassName = "pipeline-item";
            } else {
                itemSecondaryClassName = "producer-item";
            }

            DCALabel headerComponent = new DCALabel(headerName, "item-header");
            headerComponent.addStyleName(itemSecondaryClassName);
            headerComponent.setId(String.format("id-%s", headerName));
            dropdownContainer.addComponent(headerComponent);

            for (String item : entry.getValue()) {
                String itemValue = item;
                if(itemValue.startsWith("(")) {
                    itemValue = removeBrackets(itemValue);
                }
                DCALabel itemComponent = new DCALabel(itemValue, "item");
                itemComponent.setId(item);
                itemComponent.addStyleName(itemSecondaryClassName);
                dropdownContainer.addComponent(itemComponent);
            }
        }

        dropdownContainer.addLayoutClickListener(new LayoutEvents.LayoutClickListener() {
            @Override
            public void layoutClick(LayoutEvents.LayoutClickEvent event) {
                if (event.getChildComponent() != null && event.getChildComponent().getId() != null
                        && !event.getChildComponent().getId().startsWith("id-")) {
                    DCALabel itemClicked = (DCALabel) event.getChildComponent();
                    DCALabel newComponent = new DCALabel(itemClicked.getValue(), "item");
                    newComponent.setId(itemClicked.getId());

                    if (itemClicked.getId().startsWith("(")) {
                        newComponent.addStyleName("pipeline-item");
                    }

                    selectedInput.removeAllComponents();
                    selectedInput.addComponent(newComponent);
                    currentComponent.removeStyleName("open-dropdown");

                    if (currentComponent.getParent().getParent() instanceof DCASubRegularPipeLineComponent
                            && currentComponent.getParent().getParent().getParent().getParent().getParent() instanceof DCAJoinContainerWrapperComponent) {
                        DCASubRegularPipeLineComponent currentBranch = (DCASubRegularPipeLineComponent) currentComponent.getParent().getParent();

                        if (currentBranch.getSourceWrapperComponent().getComponentCount() == 1) {
                            addComponentAsLast(currentBranch.getRemoveBranchComponent(), currentBranch.getSourceWrapperComponent());
                        }

                        DCAJoinContainerWrapperComponent joinContainerWrapperComponent =
                                (DCAJoinContainerWrapperComponent) currentComponent.getParent().getParent().getParent().getParent().getParent();
                        joinContainerWrapperComponent.addBranch(currentBranch);
                    } else if (currentComponent.getParent().getParent() instanceof DCARegularPipeComponent) {
                        DCARegularPipeComponent currentBranch = (DCARegularPipeComponent) currentComponent.getParent().getParent();
                        if (currentBranch.getSourceWrapperComponent().getComponentCount() == 1) {
                            addComponentAsLast(currentBranch.getRemoveBranchComponent(), currentBranch.getSourceWrapperComponent());
                        }
                    }

                    Page.getCurrent().getJavaScript().execute("calculatePipeLineWidth()");
                }
            }
        });
    }

    /*It would be used in selection component (selection-wrapper) in the pipeline*/
    public DCADropDownComponent(DCALayoutContainer layoutContainer, Map<String, List<String>> itemsMap) {
        init(false, layoutContainer);

        for (Map.Entry<String, List<String>> entry : itemsMap.entrySet()) {
            String headerName = entry.getKey();
            DCALabel headerComponent = new DCALabel(headerName, "item-header");
            headerComponent.setId(String.format("id-%s", headerName));
            dropdownContainer.addComponent(headerComponent);

            for (String item : entry.getValue()) {
                String itemValue = item;
                if(itemValue.startsWith("(")) {
                    itemValue = removeBrackets(itemValue);
                }
                DCALabel itemComponent = new DCALabel(itemValue, "item");
                itemComponent.setId(item);
                dropdownContainer.addComponent(itemComponent);
            }
        }

        dropdownContainer.addLayoutClickListener(new LayoutEvents.LayoutClickListener() {
            @Override
            public void layoutClick(LayoutEvents.LayoutClickEvent event) {
                if (event.getChildComponent() != null && event.getChildComponent().getId() != null
                        && !event.getChildComponent().getId().startsWith("id-")) {
                    DCALabel itemClicked = (DCALabel) event.getChildComponent();
                    DCALabel newComponent = new DCALabel(itemClicked.getValue(), "item");
                    newComponent.setId(itemClicked.getId());
                    selectedInput.removeAllComponents();
                    selectedInput.addComponent(newComponent);
                    currentComponent.removeStyleName("open-dropdown");

                    if (currentComponent.getParent() instanceof DCASelectionComponent
                            && currentComponent.getParent().getParent().getParent() instanceof DCASubRegularPipeLineComponent
                            && currentComponent.getParent().getParent().getParent().getParent().getParent().getParent() instanceof DCAJoinContainerWrapperComponent) {
                        DCASubRegularPipeLineComponent currentBranch = (DCASubRegularPipeLineComponent) currentComponent.getParent().getParent().getParent();
                        DCAJoinContainerWrapperComponent joinContainerWrapperComponent =
                                (DCAJoinContainerWrapperComponent) currentComponent.getParent().getParent().getParent().getParent().getParent().getParent();
                        joinContainerWrapperComponent.addBranch(currentBranch);
                        Page.getCurrent().getJavaScript().execute("calculatePipeLineWidth()");
                    }
                }
            }
        });
    }

    /*It would be used  for the Filter type pipe chain for the filter list generation*/
    public DCADropDownComponent(DCALayoutContainer layoutContainer, CssLayout filterChainParentContainer,
                                Map<String, List<String>> filterItemMap) {
        init(true, layoutContainer);

        for (Map.Entry<String, List<String>> entry : filterItemMap.entrySet()) {
            String headerName = entry.getKey();
            DCALabel headerComponent = new DCALabel(headerName, "item-header");
            headerComponent.setId(String.format("%s-id", headerName));
            dropdownContainer.addComponent(headerComponent);

            for (String item : entry.getValue()) {
                DCALabel itemComponent = new DCALabel(item, "item");
                dropdownContainer.addComponent(itemComponent);
            }
        }


        dropdownContainer.addLayoutClickListener(new LayoutEvents.LayoutClickListener() {
            @Override
            public void layoutClick(LayoutEvents.LayoutClickEvent event) {
                if (event.getChildComponent() != null) {
                    DCALabel itemClicked = (DCALabel) event.getChildComponent();
                    if (itemClicked.getId() != null) {
                        return;
                    }

                    int currentComponentIndex = filterChainParentContainer.getComponentIndex(currentComponent);

                    DCADropDownComponent dcaDropDownComponentRight = new DCADropDownComponent(
                            layoutContainer, filterChainParentContainer, filterItemMap);
                    String  removeIconId = UUID.randomUUID().toString();

                    DCASelectionComponent dcaSelectionComponent = new DCASelectionComponent(itemClicked.getValue(),
                            filterItemMap, layoutContainer);
                    dcaSelectionComponent.getRemoveIconComponent().setId(removeIconId);

                    filterChainParentContainer.addComponent(dcaSelectionComponent, currentComponentIndex + 1);
                    int selectionComponentIndex = filterChainParentContainer.getComponentIndex(dcaSelectionComponent);
                    filterChainParentContainer.addComponent(dcaDropDownComponentRight, selectionComponentIndex + 1);

                    dcaSelectionComponent.getSelectedComponent().addLayoutClickListener(new LayoutEvents.LayoutClickListener() {
                        @Override
                        public void layoutClick(LayoutEvents.LayoutClickEvent event) {
                            if (event.getChildComponent() != null && removeIconId.equals(event.getChildComponent().getId())) {
                                filterChainParentContainer.removeComponent(dcaSelectionComponent);
                                filterChainParentContainer.removeComponent(dcaDropDownComponentRight);
                            }
                        }
                    });


                    currentComponent.removeStyleName("open-dropdown");
                }
            }
        });
    }

    private String removeBrackets(String inputString) {
        String cleanString = inputString.replace("(", "");
        cleanString = cleanString.replace(")", "");
        return cleanString;
    }

    public void setValue(Object value) {
        String valueStr = String.valueOf(value);
        String valueId = String.valueOf(value);

        if(StringUtils.isEmpty(valueStr)) {
            return;
        }

        String pipeLineAdditionalStyle = "";

        if (valueStr.startsWith("(")) {
            valueStr = removeBrackets(valueStr);
            pipeLineAdditionalStyle = "pipeline-item";
        }

        DCALabel newComponent = new DCALabel(valueStr, "item");
        newComponent.setId(valueId);
        newComponent.addStyleName(pipeLineAdditionalStyle);
        selectedInput.removeAllComponents();
        selectedInput.addComponent(newComponent);
    }

    public String getValue() {
        if (selectedInput.getComponentCount() > 0) {
            DCALabel valueComponent = (DCALabel) selectedInput.getComponent(0);
            return StringUtils.isNotEmpty(valueComponent.getId()) ? valueComponent.getId() : valueComponent.getValue();
        }
        return "";
    }

    private DCAIcon getAddNewIcon() {
        return new DCAIcon("icon-add-new", "rectangle-green-plus-icon");
    }
}
