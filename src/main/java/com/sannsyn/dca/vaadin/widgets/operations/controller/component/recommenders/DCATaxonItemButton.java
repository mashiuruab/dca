package com.sannsyn.dca.vaadin.widgets.operations.controller.component.recommenders;

import com.vaadin.server.Page;
import com.vaadin.ui.Button;
import com.vaadin.ui.CssLayout;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by mashiur on 4/13/17.
 */
public class DCATaxonItemButton extends Button {
    private static final String DRAG_ABLE_ID = "DRAG_ABLE_ID";
    private static final String CLS_NAME_INIT = "initial";
    private static final String CLS_NAME_SELECTED = "selected";

    private boolean isSelected = false;

    public DCATaxonItemButton(String  caption, String identifier, DCAGlobalFilterContainer globalFilterContainer) {
        this.setStyleName("taxon-item-button");
        this.setCaption(caption);
        this.setId(identifier);

        String filterListContainerSelector =  String.format("%s-%s", CLS_NAME_SELECTED, identifier);

        globalFilterContainer.getFilterListContainer().addStyleName(CLS_NAME_INIT);


        addClickListener(event -> {
            isSelected = !isSelected;

            globalFilterContainer.getFilterListContainer().removeStyleName(CLS_NAME_INIT);

            if (isSelected) {
                this.addStyleName(CLS_NAME_SELECTED);
                globalFilterContainer.getFilterListContainer().addStyleName(filterListContainerSelector);
            } else {
                this.removeStyleName(CLS_NAME_SELECTED);
                globalFilterContainer.getFilterListContainer().removeStyleName(filterListContainerSelector);
            }


            if (StringUtils.isEmpty(globalFilterContainer.getSelectedTaxonType())) {
                globalFilterContainer.getFilterListContainer().setId("");
            } else {
                globalFilterContainer.getFilterListContainer().setId(DRAG_ABLE_ID);
            }

        });

        Page.Styles styles = Page.getCurrent().getStyles();
        styles.add(String.format(".global-filter-container .filter-list-container .active-filter-list-container .%s {display:none;}", identifier));
        styles.add(String.format(".global-filter-container .filter-list-container.%s .active-filter-list-container .%s {display:block;}", filterListContainerSelector, identifier));
        styles.add(String.format(".global-filter-container .filter-list-container.%s .active-filter-list-container .%s {display:block;}", CLS_NAME_INIT, identifier));
    }

    public boolean isSelected() {
        return isSelected;
    }
}
