package com.sannsyn.dca.vaadin.widgets.operations.controller.component.pipelines.pipeline;

import com.sannsyn.dca.vaadin.view.DCALayoutContainer;
import com.vaadin.event.LayoutEvents;
import com.vaadin.server.Page;
import com.vaadin.ui.CssLayout;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by mashiur on 5/24/16.
 */
public class DCAPipeGenerator {
    private static final Logger logger = LoggerFactory.getLogger(DCAPipeGenerator.class);

    private Map<String, List<String>> pipeDropDownItems;
    private Map<String, List<String>> sourceDropDownItems;
    private DCALayoutContainer layoutContainer;

    public DCAPipeGenerator(Map<String, List<String>> pipesDropDownItems,
                            Map<String, List<String>> sourceDropDownItems, DCALayoutContainer  layoutContainer) {
        this.pipeDropDownItems = pipesDropDownItems;
        this.sourceDropDownItems = sourceDropDownItems;
        this.layoutContainer = layoutContainer;
    }

    public void addPipes(Object pipeItem, CssLayout itemContainer) {
        if (pipeItem instanceof String) {
            String removeIconId = UUID.randomUUID().toString();
            DCASelectionComponent dcaSelectionComponent = new DCASelectionComponent(String.valueOf(pipeItem), pipeDropDownItems, this.layoutContainer);
            dcaSelectionComponent.getRemoveIconComponent().setId(removeIconId);

            DCADropDownComponent rightDropDown = new DCADropDownComponent(pipeDropDownItems, sourceDropDownItems,
                    itemContainer, this.layoutContainer);

            itemContainer.addComponent(dcaSelectionComponent);
            itemContainer.addComponent(rightDropDown);

            dcaSelectionComponent.getSelectedComponent().addLayoutClickListener(new LayoutEvents.LayoutClickListener() {
                @Override
                public void layoutClick(LayoutEvents.LayoutClickEvent event) {
                    if (event.getChildComponent() != null && removeIconId.equals(event.getChildComponent().getId())) {
                        itemContainer.removeComponent(dcaSelectionComponent);
                        itemContainer.removeComponent(rightDropDown);
                        Page.getCurrent().getJavaScript().execute("calculatePipeLineWidth()");
                    }
                }
            });
        } else if (pipeItem instanceof List) {
            DCAPipeSourceComponent pipeSourceComponent;

            if (itemContainer.getParent() instanceof DCARegularPipeComponent) {
                pipeSourceComponent = (DCARegularPipeComponent) itemContainer.getParent();
            } else  if (itemContainer.getParent() instanceof  DCASubRegularPipeLineComponent) {
                pipeSourceComponent = (DCASubRegularPipeLineComponent) itemContainer.getParent();
                if (!StringUtils.isEmpty(itemContainer.getId())) {
                    pipeSourceComponent.setId(itemContainer.getId());
                    itemContainer.setId("");
                }
            } else {
                pipeSourceComponent = new DCARegularPipeComponent(pipeDropDownItems, sourceDropDownItems, layoutContainer);
                itemContainer.addComponent(pipeSourceComponent);
                if (!StringUtils.isEmpty(itemContainer.getId())) {
                    pipeSourceComponent.setId(itemContainer.getId());
                    itemContainer.setId("");
                }
            }


            List originalObject = (List) pipeItem;

            if (originalObject.isEmpty()) {
                return;
            }

            Object lastObjectList = originalObject.get(originalObject.size() - 1);
            if (lastObjectList instanceof String) {
                String sourceValue = String.valueOf(lastObjectList);
                pipeSourceComponent.getSourceDropDownComponent().setValue(sourceValue);
                pipeSourceComponent.getSourceWrapperComponent().addComponent(pipeSourceComponent.getRemoveBranchComponent());
                pipeSourceComponent.addComponent(pipeSourceComponent.getSourceWrapperComponent());

                for (int counter = 0; counter < originalObject.size() - 1; counter++) {
                    addPipes(originalObject.get(counter), pipeSourceComponent.getItemContainer());
                }
            } else {
                pipeSourceComponent.removeComponent(pipeSourceComponent.getSourceWrapperComponent());
                for (int counter = 0; counter < originalObject.size(); counter++) {
                    addPipes(originalObject.get(counter), pipeSourceComponent.getItemContainer());
                }
            }

        } else if (pipeItem instanceof Map) {
            Map<String, Object> itemMap = (Map<String, Object>) pipeItem;

            for (Map.Entry<String, Object> entry : itemMap.entrySet()) {


                CssLayout itemParent = (CssLayout) itemContainer.getParent();
                DCAJoinContainerWrapperComponent joinContainerWrapperComponent = new DCAJoinContainerWrapperComponent(entry.getKey(),
                        null, pipeDropDownItems, itemParent, layoutContainer);
                itemParent.addComponent(joinContainerWrapperComponent);

                Object valueObject = entry.getValue();
                List pipeList = ((List) valueObject);

                for (Object object : pipeList) {
                    if (object instanceof List) {
                        DCASubRegularPipeLineComponent subRegularPipeLineComponent = new DCASubRegularPipeLineComponent(pipeDropDownItems,
                                sourceDropDownItems, layoutContainer);
                        joinContainerWrapperComponent.addPipeLineComponent(subRegularPipeLineComponent);
                        addPipes(object, subRegularPipeLineComponent.getItemContainer());
                    } else if (object instanceof String) {
                        DCASubRegularPipeLineComponent subRegularPipeLineComponent = new DCASubRegularPipeLineComponent(pipeDropDownItems,
                                sourceDropDownItems, layoutContainer);
                        subRegularPipeLineComponent.getSourceDropDownComponent().setValue(object);
                        subRegularPipeLineComponent.getSourceWrapperComponent().
                                addComponent(subRegularPipeLineComponent.getRemoveBranchComponent());
                        joinContainerWrapperComponent.addPipeLineComponent(subRegularPipeLineComponent);
                    } else {
                        DCASubRegularPipeLineComponent subRegularPipeLineComponentForJoinObject = new DCASubRegularPipeLineComponent(pipeDropDownItems,
                                sourceDropDownItems, layoutContainer);
                        subRegularPipeLineComponentForJoinObject.removeComponent(subRegularPipeLineComponentForJoinObject.getSourceWrapperComponent());
                        joinContainerWrapperComponent.addPipeLineComponent(subRegularPipeLineComponentForJoinObject);
                        addPipes(object, subRegularPipeLineComponentForJoinObject.getItemContainer());
                    }
                }

                DCASubRegularPipeLineComponent subRegularPipeLineComponent = new DCASubRegularPipeLineComponent(pipeDropDownItems,
                        sourceDropDownItems, layoutContainer);
                joinContainerWrapperComponent.addPipeLineComponent(subRegularPipeLineComponent);
            }
        }
    }
}
