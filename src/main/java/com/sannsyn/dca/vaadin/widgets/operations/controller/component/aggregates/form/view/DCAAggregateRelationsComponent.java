package com.sannsyn.dca.vaadin.widgets.operations.controller.component.aggregates.form.view;

import com.sannsyn.dca.presenter.DCADashboardPresenter;
import com.sannsyn.dca.vaadin.component.custom.DCAWrapper;
import com.sannsyn.dca.vaadin.component.custom.field.DCAError;
import com.sannsyn.dca.vaadin.component.custom.field.DCALabel;
import com.sannsyn.dca.vaadin.widgets.operations.controller.model.aggregates.DCAAggregateItem;
import com.sannsyn.dca.vaadin.widgets.operations.controller.model.aggregates.info.DCAAggregateInfo;
import com.sannsyn.dca.vaadin.widgets.operations.controller.model.aggregates.info.DCAAggregateInfoEntity;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.NativeSelect;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

/**
 * The component showing details of an Aggregate
 * <p>
 * Created by mashiur on 4/28/16.
 */
public class DCAAggregateRelationsComponent extends DCAAggregateViewComponent {
    private static final Logger logger = LoggerFactory.getLogger(DCAAggregateRelationsComponent.class);

    private static final List<String> headerNames = Arrays.asList("External Id", "Count", "Popularity");
    private static final String INFO_MESSAGE_TEMPLATE = "No %s relations found for %s in %s";

    private CssLayout relationsFilterContainer = new CssLayout();
    private CssLayout entityListContainer = new CssLayout();
    private CssLayout clusterListContainer = new CssLayout();
    private NativeSelect type = new NativeSelect();
    private NativeSelect number = new NativeSelect();

    private DCAAggregateItem dcaAggregateItem;

    private String aggregateName;

    DCAAggregateRelationsComponent(String aggregateName, DCADashboardPresenter dcaDashboardPresenter,
                                   DCAAggregateItem dcaAggregateItem) {
        setDashboardPresenter(dcaDashboardPresenter);
        this.aggregateName = aggregateName;

        this.dcaAggregateItem = dcaAggregateItem;

        this.setStyleName("relations-content-container");

        initRelationsFilterContainer();

        this.relationsFilterContainer.setStyleName("relations-filter-container");
        this.entityListContainer.setStyleName("relations-entity-container");
        this.clusterListContainer.setStyleName("relations-cluster-container");

        DCALabel headerLabel = new DCALabel("<span>Aggregate Example Relations</span>", "header dca-widget-title");
        this.addComponent(headerLabel);
        this.addComponent(relationsFilterContainer);
        this.addComponent(entityListContainer);
        this.addComponent(clusterListContainer);
    }

    private void initRelationsFilterContainer() {
        DCALabel label = new DCALabel("Show: ", "aggregate-relations-label");

        Arrays.asList("LAST", "FIRST", "RANDOM", "HIGHEST_COUNT", "MOST POPULAR").forEach(this.type::addItem);
        Arrays.asList(10, 50, 100).forEach(this.number::addItem);

        this.type.setStyleName("relations-type");
        this.type.setValue("LAST");
        this.type.setNullSelectionAllowed(false);
        this.type.addValueChangeListener(event -> updateEntitiesOnValueChange());

        this.number.setStyleName("relations-number");
        this.number.setValue(10);
        this.number.setNullSelectionAllowed(false);
        this.number.addValueChangeListener(event -> updateEntitiesOnValueChange());

        DCALabel relationsText = new DCALabel("relations", "relations-text");
        DCAWrapper value = new DCAWrapper(Arrays.asList(this.type, this.number, relationsText), "aggregate-relations-value");

        addComponentAsLast(Arrays.asList(label, value), this.relationsFilterContainer);
    }

    private void initEntityContainer(DCAAggregateInfo dcaAggregateInfo) {
        initContainer(
            dcaAggregateInfo.getNumEntities(), "Entities", "entity", entityListContainer,
            () -> dcaAggregateItem.getEntityTaxon(),
            dcaAggregateInfo::getEntities,
            () -> entityListContainer.removeAllComponents());
    }

    private void initClusterContainer(DCAAggregateInfo dcaAggregateInfo) {
        initContainer(
            dcaAggregateInfo.getNumClusters(), "Clusters", "cluster", clusterListContainer,
            () -> dcaAggregateItem.getClusterTaxon(),
            dcaAggregateInfo::getClusters,
            () -> clusterListContainer.removeAllComponents());
    }

    private void initContainer(String totalTaxonNumber, String headerStr, String infoStr, CssLayout container,
                               Supplier<String> taxonNameSupplier,
                               Supplier<List<DCAAggregateInfoEntity>> listSupplier,
                               Runnable clearComponentAction) {
        clearComponentAction.run();
        DCALabel headerLabel = makeHeaderLabel(headerStr, totalTaxonNumber, taxonNameSupplier);
        addComponentAsLast(headerLabel, container);

        List<DCAAggregateInfoEntity> entities = listSupplier.get();
        if (entities == null || entities.isEmpty()) {
            DCALabel infoMessageComponent = createInfoLabel(infoStr);
            addComponentAsLast(infoMessageComponent, container);
            return;
        }

        List<Component> entityContainerList = populateContainerList(headerLabel, entities);
        addComponentAsLast(entityContainerList, container);
    }


    private DCALabel makeHeaderLabel(String defaultName, String number, Supplier<String> taxonNameSupplier) {
        String entityTaxonName = defaultName;
        String entityTaxon = taxonNameSupplier.get();
        if (StringUtils.isNotEmpty(entityTaxon)) {
            entityTaxonName = entityTaxon;
        }
        entityTaxonName = StringUtils.capitalize(entityTaxonName);

        if (StringUtils.isBlank(number)) {
            number = "0";
        }
        return new DCALabel(
            String.format("%s (Total number of %s = %s)", entityTaxonName, StringUtils.lowerCase(defaultName), number),
            "dca-widget-title-container");
    }

    private DCALabel createInfoLabel(String relationName) {
        String selectedOption = (String) type.getValue();
        String infoMsg = String.format(INFO_MESSAGE_TEMPLATE, relationName, selectedOption, aggregateName);
        return new DCALabel(infoMsg, "relations-empty-msg-label");
    }

    private List<Component> populateContainerList(DCALabel headerLabel, List<DCAAggregateInfoEntity> infoEntityList) {
        DCAAggregateEntityComponent headers = new DCAAggregateEntityComponent(headerNames);
        List<Component> entityContainerList = new ArrayList<>();
        entityContainerList.add(headerLabel);
        entityContainerList.add(headers);

        int counter = 0;
        for (DCAAggregateInfoEntity dcaAggregateInfoEntity : infoEntityList) {
            DCAAggregateEntityComponent dcaAggregateEntityComponent = new DCAAggregateEntityComponent(dcaAggregateInfoEntity);
            if (counter % 2 == 0) {
                dcaAggregateEntityComponent.addStyleName("alternating-gray-color");
            }
            entityContainerList.add(dcaAggregateEntityComponent);
            counter++;
        }
        return entityContainerList;
    }

    private void updateEntitiesOnValueChange() {
        Observable<DCAAggregateInfo> dcaAggregateInfoObservable = getDashboardPresenter().getServiceConfig(aggregateName,
            (String) type.getValue(), (Integer) number.getValue());
        dcaAggregateInfoObservable.subscribe(this::onNext, this::onError);
    }

    public void onNext(DCAAggregateInfo dcaAggregateInfo) {
        try {
            initEntityContainer(dcaAggregateInfo);
            initClusterContainer(dcaAggregateInfo);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void onError(Throwable throwable) {
        throwable.printStackTrace();
        logger.error("Error : ", throwable.getMessage());
        addComponentAsLast(new DCAError("Error Happened While fetching Entity/Cluster taxons"),
            this.entityListContainer);
    }
}
