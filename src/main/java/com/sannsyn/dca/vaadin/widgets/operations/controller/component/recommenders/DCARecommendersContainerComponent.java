package com.sannsyn.dca.vaadin.widgets.operations.controller.component.recommenders;

import com.google.gson.JsonObject;
import com.sannsyn.dca.presenter.DCADashboardPresenter;
import com.sannsyn.dca.vaadin.component.custom.container.collapsible.DCACollapsibleItemContainer;
import com.sannsyn.dca.vaadin.component.custom.container.collapsible.DCACollapsibleItemContainerImpl;
import com.sannsyn.dca.vaadin.component.custom.container.collapsible.DCAColumnSpec;
import com.sannsyn.dca.vaadin.component.custom.field.DCAError;
import com.sannsyn.dca.vaadin.component.custom.field.DCALabel;
import com.sannsyn.dca.vaadin.component.custom.logout.DCAModalComponent;
import com.sannsyn.dca.vaadin.view.DCALayoutContainer;
import com.sannsyn.dca.vaadin.widgets.common.DCABreadCrumb;
import com.sannsyn.dca.vaadin.widgets.common.DCABreadCrumbImpl;
import com.sannsyn.dca.vaadin.widgets.operations.controller.component.DCAPopupErrorComponent;
import com.sannsyn.dca.vaadin.widgets.operations.controller.component.DCAWidgetContainerComponent;
import com.sannsyn.dca.vaadin.widgets.operations.controller.component.pipelines.pipeline.DCAPipelineViewComponent;
import com.sannsyn.dca.vaadin.widgets.operations.controller.model.aggregates.*;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.util.*;
import java.util.function.Consumer;

/**
 * The recommender container component
 * <p>
 * Created by mashiur on 12/26/16.
 */
public class DCARecommendersContainerComponent extends DCAWidgetContainerComponent {
    private static final Logger logger = LoggerFactory.getLogger(DCARecommendersContainerComponent.class);

    private static final String KEY_PROP_CHANNEL = "channel";
    private static final String KEY_PROP_PAGE_TYPE = "pageType";
    private static final String KEY_PROP_RECOMMENDER_NAME = "recommenderName";

    public static List<String> HEADER_NAME_LIST = Arrays.asList("Channel", "Page type", "Recommender");

    private DCABreadCrumb breadCrumb = new DCABreadCrumbImpl();
    private Consumer<String> navigationHelper;

    private Consumer<String> navigateToSelf;
    private DCAServiceConfig serviceConfig;
    private DCALabel widgetTitleComponent = new DCALabel("Recommender Overview", "dca-widget-title-container");

    public DCARecommendersContainerComponent(DCADashboardPresenter dashboardPresenter, DCALayoutContainer layoutContainer) {
        setDashboardPresenter(dashboardPresenter);
        setLayoutContainer(layoutContainer);
        this.setStyleName("recomndr-container-component");

        Observable<DCAServiceConfigWrapper> wrapperObservable = dashboardPresenter.getServiceConfig(getLoggedInUser());

        wrapperObservable.subscribe(this::onNext, this::onError);
    }

    private void onNext(DCAServiceConfigWrapper serviceConfigWrapper) {
        this.serviceConfig = serviceConfigWrapper.getService();
        try {
            init();
        } catch (Exception e) {
            logger.error("Exception", e);
            addComponentAsLast(new DCAError("Error happened while loading the component"), this);
        }
    }

    private void onError(Throwable throwable) {
        logger.error("Error : ", throwable);
        addComponentAsLast(new DCAError("Error Loading Recommender Widget Component"), this);
    }

    private void prepareBreadCrumb() {
        breadCrumb.addAction("Controller", navigationHelper);
        breadCrumb.addAction("Recommenders", navigateToSelf);
        this.addComponent(breadCrumb.getView());
        getLayoutContainer().setBreadCrumb(breadCrumb);
    }

    private void init() {
        prepareBreadCrumb();

        List<Component> componentList = new ArrayList<>();

        componentList.add(widgetTitleComponent);

        Component globalFilterItem = initAndGetGlobalFilterComponent();
        componentList.add(globalFilterItem);

        Component container = renderValues();
        componentList.add(container);

        addComponentAsLast(componentList, this);
    }


    private Component initAndGetGlobalFilterComponent() {
        return new DCAGlobalFilterItemComponent(getLayoutContainer(), getDashboardPresenter());
    }

    private Component renderValues() {
        DCACollapsibleItemContainer container = new DCACollapsibleItemContainerImpl();
        List<DCAColumnSpec> columnSpecs = Arrays.asList(
            new DCAColumnSpec("Channel", 33, KEY_PROP_CHANNEL),
            new DCAColumnSpec("Page type", 33, KEY_PROP_PAGE_TYPE),
            new DCAColumnSpec("Recommender", 33, KEY_PROP_RECOMMENDER_NAME)
        );

        container.setColumnSpecs(columnSpecs);
        container.registerExpandHandler(this::createButton);

        List<JsonObject> items = prepareValues();
        container.addItems(items);

        return container;
    }


    private Component createButton(JsonObject jsonItem) {
        Button manageFilterButton = new Button("Manage Filters");
        manageFilterButton.setStyleName("btn-manage-filter");

        CssLayout buttonWrapper = new CssLayout();
        buttonWrapper.addStyleName("expand-container");
        buttonWrapper.addComponent(manageFilterButton);

        DCAEnsembles ensembles = this.serviceConfig.getEnsembles();

        List<String> itemValues = new ArrayList<>();
        itemValues.add(jsonItem.get(KEY_PROP_CHANNEL).getAsString());
        itemValues.add(jsonItem.get(KEY_PROP_PAGE_TYPE).getAsString());
        itemValues.add(jsonItem.get(KEY_PROP_RECOMMENDER_NAME).getAsString());

        if (ensembles != null
                && !ensembles.getTasks().containsKey(jsonItem.get(KEY_PROP_RECOMMENDER_NAME).getAsString())) {
            throw new RuntimeException("Not Enough data Available in the service configuration");
        }

        manageFilterButton.addClickListener(event -> {
            DCAModalComponent manageFilterModalComponent = null;
            try {
                DCAManageFilterPopUpContainer manageFilterContainer = new DCAManageFilterPopUpContainer(itemValues,
                        jsonItem.get(KEY_PROP_RECOMMENDER_NAME).getAsString(),getDashboardPresenter(),
                        getLayoutContainer());
                manageFilterModalComponent = new DCAModalComponent(manageFilterContainer);
                getLayoutContainer().getWidgetContainer().addComponent(manageFilterModalComponent);
            } catch (Exception e) {
                logger.error("Error : ", e);
                removeComponent(manageFilterModalComponent, getLayoutContainer().getWidgetContainer());
                DCAPopupErrorComponent errorComponent = new DCAPopupErrorComponent("ERROR: ",
                        e.getMessage(), getLayoutContainer().getWidgetContainer());
                addComponentAsLast(errorComponent, getLayoutContainer().getWidgetContainer());
            }
        });

        return buttonWrapper;
    }

    private List<JsonObject> prepareValues() {
        Map<String, DCATaskObject> recommenderMap = serviceConfig.getEnsembles().getTasks();
        List<JsonObject> result = new ArrayList<>();

        serviceConfig.getExternal().ifPresent(dcaExternal -> {
            Map allPipeLineMap = Collections.EMPTY_MAP;

            if (dcaExternal.getDca().isPresent() && dcaExternal.getDca().get().containsKey(DCAPipelineViewComponent.EXTERNAL_PIPELINE_OBJECT_KEY)) {
                allPipeLineMap = dcaExternal.getDca().get().get(DCAPipelineViewComponent.EXTERNAL_PIPELINE_OBJECT_KEY);
            }

            for (Map.Entry<String, DCATaskObject> entry : recommenderMap.entrySet()) {
                String recommenderName = entry.getKey();

                if (allPipeLineMap.get(recommenderName) == null && !(allPipeLineMap.get(recommenderName) instanceof Map)) {
                    continue;
                }

                Map externalValueMap = (Map) allPipeLineMap.get(recommenderName);
                String channelValue = StringUtils.stripToEmpty(String.valueOf(externalValueMap.get(DCAPipelineViewComponent.CHANNEL_KEY)));
                String pageTypeValue = StringUtils.stripToEmpty(String.valueOf(externalValueMap.get(DCAPipelineViewComponent.PAGE_TYPE_KEY)));
                boolean showInDCA = Boolean.valueOf(String.valueOf(externalValueMap.get(DCAPipelineViewComponent.SHOW_IN_DCA_KEY)));

                if (showInDCA) {
                    JsonObject item = new JsonObject();
                    item.addProperty(KEY_PROP_CHANNEL, channelValue);
                    item.addProperty(KEY_PROP_PAGE_TYPE, pageTypeValue);
                    item.addProperty(KEY_PROP_RECOMMENDER_NAME, recommenderName);
                    result.add(item);
                }
            }
        });

        return result;
    }

    public void setNavigationHelper(Consumer<String> navigationHelper) {
        this.navigationHelper = navigationHelper;
    }

    public void setNavigateToSelf(Consumer<String> navigateToSelf) {
        this.navigateToSelf = navigateToSelf;
    }
}
