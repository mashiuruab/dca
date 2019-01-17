package com.sannsyn.dca.vaadin.inspectaggregate;

import com.google.gwt.thirdparty.guava.common.collect.EvictingQueue;
import com.sannsyn.dca.model.user.DCAUser;
import com.sannsyn.dca.service.AggregateQuery;
import com.sannsyn.dca.service.DCAConfigService;
import com.sannsyn.dca.service.SortBy;
import com.sannsyn.dca.vaadin.component.custom.field.DCAComboBox;
import com.sannsyn.dca.vaadin.component.custom.field.DCASuggestionTextField;
import com.sannsyn.dca.vaadin.icons.SannsynIcons;
import com.sannsyn.dca.vaadin.validators.NonEmptyValidator;
import com.sannsyn.dca.vaadin.widgets.operations.controller.model.aggregates.DCAAggregateItem;
import com.sannsyn.dca.vaadin.widgets.operations.controller.model.aggregates.DCAServiceConfigWrapper;
import com.vaadin.data.Property;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.util.*;

import static com.vaadin.server.Sizeable.Unit.PIXELS;

/**
 * Component containing the search input ui.
 * <p>
 * Created by jobaer on 6/7/16.
 */
class DCAInspectAggregateSearchInput {
    private static final Logger logger = LoggerFactory.getLogger(DCAInspectAggregateSearchInput.class);
    private final UI mainUi;
    private DCAUser loggedInUser;

    private DCAConfigService configService = new DCAConfigService();
    private DCAComboBox aggregateComboBox = new DCAComboBox();
    private DCAComboBox taxonComboBox = new DCAComboBox();
    private Button search = new Button("");
    private DCASuggestionTextField id = new DCASuggestionTextField();
    private DCAInspectAggregateSearchInputHandler handler;

    DCAInspectAggregateSearchInput(UI ui, DCAUser loggedInUser) {
        this.mainUi = ui;
        this.loggedInUser = loggedInUser;
    }

    public Layout getComponent() {
        Layout wrapper = new CssLayout();
        Layout test = createInputComponents();
        wrapper.addComponent(test);
        return wrapper;
    }

    private Layout createInputComponents() {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setStyleName("search-aggregate-input");
        layout.setSpacing(true);

        prepareIdField(layout);

        taxonComboBox.setWidth(120, PIXELS);
        taxonComboBox.addItems("Loading...");
        taxonComboBox.setNullSelectionAllowed(true);
        taxonComboBox.setInputPrompt("Taxon");
        layout.addComponent(taxonComboBox);

        aggregateComboBox.setWidth(220, PIXELS);
        aggregateComboBox.addItems("Loading...");
        aggregateComboBox.setNullSelectionAllowed(false);
        aggregateComboBox.setInputPrompt("Aggregates");
        layout.addComponent(aggregateComboBox);
        initializeAggregates();

        DCAComboBox items = new DCAComboBox();
        items.setWidth(110, PIXELS);
        items.addItems("10", "25", "50", "100", "1000", "Unlimited");
        items.select("10");
        items.setNullSelectionAllowed(false);
        items.setInputPrompt("No. of items");

        Object storedNumOfItems = VaadinSession.getCurrent().getAttribute("ia-numOfItems");
        if (storedNumOfItems != null) {
            items.select(storedNumOfItems);
        }

        layout.addComponent(items);

        DCAComboBox sortBy = new DCAComboBox();
        sortBy.setWidth(150, PIXELS);
        Arrays.stream(SortBy.values()).forEach(sortBy::addItem);
        sortBy.select(SortBy.ID);
        sortBy.setNullSelectionAllowed(false);
        sortBy.setInputPrompt("Sort by");
        layout.addComponent(sortBy);

        Object storedSortBy = VaadinSession.getCurrent().getAttribute("ia-sortBy");
        if (storedNumOfItems != null) {
            sortBy.select(storedSortBy);
        }

        CssLayout group = new CssLayout();
        group.addStyleName("v-component-group");
        layout.addComponent(group);

        Button listButton = new Button("");
        listButton.setDescription("List view");
        listButton.setIcon(FontAwesome.NAVICON);
        listButton.addStyleName(ValoTheme.BUTTON_ICON_ONLY);
        listButton.addStyleName("flat-button");

        group.addComponent(listButton);
        Button imageButton = new Button("");
        imageButton.setDescription("Detail view");
        imageButton.setIcon(FontAwesome.IMAGE);
        imageButton.addStyleName(ValoTheme.BUTTON_ICON_ONLY);
        imageButton.addStyleName("flat-button");

        group.addComponent(imageButton);

        // By default the image style is active
        String activeStyleName = "btn-active-style";
        imageButton.addStyleName(activeStyleName);
        listButton.addClickListener(event -> {
            handler.switchToView("listView");
            imageButton.removeStyleName(activeStyleName);
            listButton.addStyleName(activeStyleName);
        });
        imageButton.addClickListener(event -> {
            handler.switchToView("default");
            listButton.removeStyleName(activeStyleName);
            imageButton.addStyleName(activeStyleName);
        });

        search.setIcon(SannsynIcons.SEARCH);
        search.addStyleName("btn-primary-style");
        search.addStyleName(ValoTheme.BUTTON_ICON_ONLY);
        search.addClickListener(event -> {
            if (handler == null) throw new IllegalStateException("Search input handler should not be null");
            search.setEnabled(false);
            Optional<AggregateQuery> aggregateQueryOption = prepareQuery(id, items, sortBy);
            aggregateQueryOption.ifPresent(query -> handler.search(query));
        });
        layout.addComponent(search);

        return layout;
    }

    private void initializeAggregates() {
        Observable<DCAServiceConfigWrapper> aggregateInfo = configService.getServiceConfig(this.loggedInUser);
        aggregateInfo.subscribe(config -> {
            logger.debug("Got the config");
            aggregateComboBox.removeAllItems();
            taxonComboBox.removeAllItems();
            Map<String, DCAAggregateItem> aggregates = config.getService().getAggregates();

            Object storedAggvalue = VaadinSession.getCurrent().getAttribute("ia-aggregateValue");
            aggregates.keySet().stream().forEach(aggregateComboBox::addItem);
            if (storedAggvalue != null) {
                logger.debug("Updating the select");
                mainUi.access(() -> aggregateComboBox.select(storedAggvalue));
            }

            Map<String, Set<String>> taxonMap = prepareTaxons(aggregates);
            taxonMap.keySet().stream().forEach(taxonComboBox::addItem);

            // todo this is a brute force way. Try to find some sort of filtering mechanism for combobox
            taxonComboBox.addValueChangeListener(event -> {
                Property property = event.getProperty();
                Object value = property.getValue();
                logger.debug("Valued changed to " + value);
                if (value == null) {
                    aggregateComboBox.removeAllItems();
                    aggregates.keySet().stream().forEach(aggregateComboBox::addItem);
                } else {
                    String taxonName = value.toString();
                    Set<String> strings = taxonMap.get(taxonName);
                    if (strings != null) {
                        aggregateComboBox.removeAllItems();
                        strings.stream().forEach(aggregateComboBox::addItem);
                    }
                }
            });

        }, e -> {
            logger.error("Error while initializing inspect aggregates", e);
        });
    }

    private Map<String, Set<String>> prepareTaxons(Map<String, DCAAggregateItem> aggregates) {
        Map<String, Set<String>> result = new HashMap<>();

        for (Map.Entry<String, DCAAggregateItem> aggregateEntry : aggregates.entrySet()) {
            String aggregateName = aggregateEntry.getKey();
            DCAAggregateItem aggregate = aggregateEntry.getValue();

            String entityTaxon = aggregate.getEntityTaxon();
            String clusterTaxon = aggregate.getClusterTaxon();

            prepareTaxonMap(result, aggregateName, entityTaxon, clusterTaxon);
        }
        return result;
    }

    private void prepareTaxonMap(Map<String, Set<String>> result, String aggregateName, String entityTaxon, String clusterTaxon) {
        addToTaxonMap(result, aggregateName, entityTaxon);
        addToTaxonMap(result, aggregateName, clusterTaxon);
    }

    private void addToTaxonMap(Map<String, Set<String>> result, String aggregateName, String taxon) {
        if (StringUtils.isBlank(taxon) || "null".equals(taxon)) return;

        Set<String> strings = result.get(taxon);
        if (strings == null) {
            strings = new TreeSet<>();
            strings.add(aggregateName);
            result.put(taxon, strings);
        } else {
            strings.add(aggregateName);
        }
    }

    private void prepareIdField(HorizontalLayout layout) {
        id.setWidth(150, PIXELS);
        id.setInputPrompt("Id: ");
        Object storedIds = VaadinSession.getCurrent().getAttribute("ia-pastItemIds");
        if (storedIds != null && storedIds instanceof EvictingQueue) {
            EvictingQueue<String> queue = (EvictingQueue) storedIds;
            for (String s : queue) {
                id.addItem(s);
            }
        }
        // Add non-empty validator
        id.addValidator(new NonEmptyValidator("* Id"));

        layout.addComponent(id);
    }

    private Optional<AggregateQuery> prepareQuery(DCASuggestionTextField id, DCAComboBox items, DCAComboBox sortBy) {
        id.validate();
        String itemId = id.getValue();
        boolean invalid = false;
        if (StringUtils.isBlank(itemId)) {
            search.setEnabled(true);
            invalid = true;
        }

        if (aggregateComboBox.getValue() == null) {
            aggregateComboBox.showErrorMessage("* Select an aggregate");
            invalid = true;
        }
        if(invalid) return Optional.empty();

        String aggregate = aggregateComboBox.getValue().toString();

        String numOfItems = items.getValue().toString();
        String sort = sortBy.getValue().toString();
        SortBy sortParam = SortBy.valueOf(sort);

        preserveState(itemId, aggregate, numOfItems, sortParam);

        AggregateQuery query = new AggregateQuery(aggregate, itemId, sortParam, numOfItems);
        return Optional.of(query);
    }

    private void preserveState(String itemId, String aggregate, String numOfItems, SortBy sortParam) {
        id.addItem(itemId);
        EvictingQueue<String> queue;
        Object storedIds = VaadinSession.getCurrent().getAttribute("ia-pastItemIds");
        if (storedIds != null && storedIds instanceof EvictingQueue) {
            queue = (EvictingQueue) storedIds;
        } else {
            queue = EvictingQueue.create(10);
        }
        queue.add(itemId);

        VaadinSession.getCurrent().setAttribute("ia-pastItemIds", queue);
        VaadinSession.getCurrent().setAttribute("ia-aggregateValue", aggregate);
        VaadinSession.getCurrent().setAttribute("ia-numOfItems", numOfItems);
        VaadinSession.getCurrent().setAttribute("ia-sortBy", sortParam);
    }

    public void setHandler(DCAInspectAggregateSearchInputHandler handler) {
        this.handler = handler;
    }

    void enableSearch() {
        search.setEnabled(true);
    }
}
