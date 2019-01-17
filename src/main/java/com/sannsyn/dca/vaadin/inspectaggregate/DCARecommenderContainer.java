package com.sannsyn.dca.vaadin.inspectaggregate;

import com.sannsyn.dca.model.user.DCAUser;
import com.sannsyn.dca.vaadin.icons.SannsynIcons;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static com.vaadin.server.Sizeable.Unit.PERCENTAGE;

/**
 * This is the recommender result container. It will contain multiple ui which represents result from a
 * specific recommender.
 *
 * Created by jobaer on 4/16/2016.
 */
class DCARecommenderContainer {
    private static final Logger logger = LoggerFactory.getLogger(DCARecommenderContainer.class);

    private List<DCARecommenderItems> rows = new ArrayList<>();
    private UI ui;
    private Layout recsLayout = new HorizontalLayout();
    private DCARemoveRecommenderInspector removeHandler = this::removeRow;
    private DCARecommenderItemsHandler handler;

    private DCAUser loggedInUser;

    DCARecommenderContainer(DCAUser loggedInUser) {
        this.loggedInUser = loggedInUser;

        Object totalRecsStored = VaadinSession.getCurrent().getAttribute("ir-totalRecs");
        if (totalRecsStored != null && totalRecsStored instanceof Integer) {
            logger.info("Got value from ir-totalRecs");
            int n = (Integer) totalRecsStored;
            for (int i = 0; i < n; i++) {
                DCARecommenderItems recItems = createNewRecommenderItems();
                rows.add(recItems);
            }
        } else {
            DCARecommenderItems first = createNewRecommenderItems();
            rows.add(first);
        }

        initializeValues();
    }

    void setHandler(DCARecommenderItemsHandler handler) {
        this.handler = handler;
        for (DCARecommenderItems row : rows) {
            row.setHandler(handler);
        }
    }

    private void initializeValues() {
        Object selectedValues = VaadinSession.getCurrent().getAttribute("ir-selectedRecs");
        if (selectedValues != null && selectedValues instanceof List) {
            List<String> storedValues = (List<String>) selectedValues;
            for (int i = 0; i < storedValues.size(); i++) {
                String v = storedValues.get(i);
                DCARecommenderItems recommenderItems = rows.get(i);
                if (recommenderItems != null) {
                    recommenderItems.setSelection(v);
                }
            }
        }
    }

    private void addRow() {
        DCARecommenderItems recommenderItems = createNewRecommenderItems();
        rows.add(recommenderItems);
        recsLayout.addComponent(recommenderItems);
    }

    private DCARecommenderItems createNewRecommenderItems() {
        DCARecommenderItems recommenderItems = new DCARecommenderItems(loggedInUser);
        recommenderItems.setHandler(handler);
        recommenderItems.setUi(ui);
        recommenderItems.setRemoveHandler(removeHandler);
        return recommenderItems;
    }

    private void removeRow(Component component) {
        logger.debug("Trying to remove a row from RecommenderContainer.");
        rows.remove(component);
        recsLayout.removeComponent(component);
    }

    public Component getComponent() {
        Layout cssLayout = new VerticalLayout();
        cssLayout.setStyleName("recommender-container-wrapper");

        Layout buttonWrapper = new CssLayout();
        buttonWrapper.setWidth(100, PERCENTAGE);
        buttonWrapper.setStyleName("recommender-selector");

        Button selectNewButton = new Button("Select new pipeline");
        selectNewButton.setIcon(SannsynIcons.ADD_NEW);
        selectNewButton.setStyleName(ValoTheme.BUTTON_TINY);
        selectNewButton.addStyleName("btn-white-green-style");
        buttonWrapper.addComponent(selectNewButton);

        Layout labelWrapper = new CssLayout();
        labelWrapper.setWidth(100, PERCENTAGE);
        labelWrapper.setStyleName("recommender-title-label");
        Label label = new Label("Pipelines");
        labelWrapper.addComponent(label);
        cssLayout.addComponent(labelWrapper);

        selectNewButton.addClickListener(event -> addRow());

        for (DCARecommenderItems row : rows) {
            recsLayout.addComponent(row);
        }

        Layout horLayout = new HorizontalLayout();
        horLayout.addComponent(recsLayout);
        horLayout.addComponent(buttonWrapper);
        cssLayout.addComponent(horLayout);

        return cssLayout;
    }

    void searchAll() {
        rows.forEach(DCARecommenderItems::search);
    }

    void switchAllViews(String viewName) {
        for (DCARecommenderItems row : rows) {
            row.switchView(viewName);
        }
    }

    public void setUi(UI ui) {
        this.ui = ui;
        for (DCARecommenderItems row : rows) {
            row.setUi(ui);
        }
    }

    void preserveState() {
        List<String> selectedRecs = new ArrayList<>();
        for (DCARecommenderItems row : rows) {
            String selectedValue = row.getSelectedValue();
            if (selectedValue != null) {
                selectedRecs.add(selectedValue);
            }
        }
        VaadinSession.getCurrent().setAttribute("ir-totalRecs", selectedRecs.size());
        VaadinSession.getCurrent().setAttribute("ir-selectedRecs", selectedRecs);
    }
}
