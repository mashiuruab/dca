package com.sannsyn.dca.vaadin.widgets.operations.controller.component.aggregates.form.create;

import com.sannsyn.dca.presenter.DCADashboardPresenter;
import com.sannsyn.dca.vaadin.component.custom.DCAWrapper;
import com.sannsyn.dca.vaadin.component.custom.field.DCAComboBox;
import com.sannsyn.dca.vaadin.component.custom.field.DCALabel;
import com.sannsyn.dca.vaadin.component.custom.field.DCATextField;
import com.sannsyn.dca.vaadin.view.DCALayoutContainer;
import com.sannsyn.dca.vaadin.widgets.common.DCABreadCrumb;
import com.sannsyn.dca.vaadin.widgets.operations.controller.DCAControllerOverviewComponent;
import com.sannsyn.dca.vaadin.widgets.operations.controller.component.DCAPopupErrorComponent;
import com.sannsyn.dca.vaadin.widgets.operations.controller.component.DCAPopupMessageComponent;
import com.sannsyn.dca.vaadin.widgets.operations.controller.component.DCAWidgetContainerComponent;
import com.sannsyn.dca.vaadin.widgets.operations.controller.component.aggregates.DCAPopularityDecayComponent;
import com.sannsyn.dca.vaadin.widgets.operations.controller.component.aggregates.form.view.DCAAggregateViewWrapperComponent;
import com.sannsyn.dca.vaadin.widgets.operations.controller.component.aggregates.tags.DCATagsComponent;
import com.sannsyn.dca.vaadin.widgets.operations.controller.model.aggregates.DCAAggregateItem;
import com.sannsyn.dca.vaadin.widgets.operations.controller.model.aggregates.DCAAggregatePopularity;
import com.sannsyn.dca.vaadin.widgets.operations.controller.model.aggregates.DCAAggregatePopularityTerm;
import com.sannsyn.dca.vaadin.widgets.operations.controller.model.aggregates.DCAServiceConfig;
import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.data.util.PropertysetItem;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static com.sannsyn.dca.vaadin.widgets.operations.controller.DCAAggregatesWidget.AGGREGATES_LABEL;
import static com.sannsyn.dca.vaadin.widgets.operations.controller.DCAAggregatesWidget.CREATE_NEW_LABEL;

/**
 * Created by mashiur on 4/8/16.
 */
public class DCAAggregateCreateUpdateComponent extends DCAWidgetContainerComponent {
    private static final Logger logger = LoggerFactory.getLogger(DCAAggregateCreateUpdateComponent.class);

    private DCATextField name = new DCATextField("Name: ", "secondary", true);
    private DCATextField description = new DCATextField("Description: ", "secondary", true);
    private DCAComboBox type;
    private DCAComboBox entityTaxon;
    private DCAComboBox clusterTaxon;
    private DCATagsComponent tagsComponent;
    /*It should be not false initially as the default value is false (checked) and vaadin checkbox component need to
    * setValue(true) to mark it as checked*/
    private CheckBox isStatic = new CheckBox("Static", true);
    private DCAPopularityDecayComponent popularityDecayComponent;
    private Button cancelButton = new Button();
    private Button saveButton = new Button();

    private DCAServiceConfig mDCAServiceConfig;

    private DCAAggregateItem newAggregateItem;
    private DCAAggregatePopularity newAggregatePopularity;

    private CssLayout currentComponent;

    public DCAAggregateCreateUpdateComponent(DCAServiceConfig dcaServiceConfig, DCADashboardPresenter dcaDashboardPresenter, DCALayoutContainer layoutContainer)  {
        currentComponent = this;
        setLayoutContainer(layoutContainer);
        setDashboardPresenter(dcaDashboardPresenter);
        this.setStyleName("create-aggregate-container");

        this.mDCAServiceConfig = dcaServiceConfig;

        try {
            init();
        }catch (Exception e) {
            logger.error("Error : ", e);
            this.addComponent(new DCALabel("Error Happened In Aggregate Create Component", ""));
        }
    }

    private boolean isValidForm() {
        if (!name.getTextField().isValid()) {
            name.getTextField().setRequiredError("Name Required");
            return false;
        }

        if (!description.getTextField().isValid()) {
            description.getTextField().setRequiredError("Description Required");
            return false;
        }

        if (!type.getComboBox().isValid()) {
            type.getComboBox().setRequiredError("Type Required");
            return false;
        }

        return true;
    }

    private void init() {
        Map<String, Set<String>> aggregateMap = mDCAServiceConfig.getPrePopulatedAggregateInfo();

        this.type = new DCAComboBox("Type: ", "dca-combo-box", aggregateMap.get("type"));
        this.type.getComboBox().setRequired(true);

        this.isStatic.setStyleName("static-checkbox");
        DCAWrapper staticComponent = new DCAWrapper(Collections.singletonList(this.isStatic),"half-width");

        this.entityTaxon = new DCAComboBox("Entity Taxon: ", "dca-combo-box", aggregateMap.get("entityTaxon"));
        this.entityTaxon.getComboBox().setNewItemsAllowed(true);
        this.entityTaxon.getComboBox().setImmediate(true);

        this.clusterTaxon = new DCAComboBox("Cluster Taxon: ", "dca-combo-box", aggregateMap.get("clusterTaxon"));
        this.clusterTaxon.getComboBox().setNewItemsAllowed(true);
        this.clusterTaxon.getComboBox().setImmediate(true);

        this.tagsComponent = new DCATagsComponent("Tags: ", aggregateMap.get("tags"), getLayoutContainer());

        this.popularityDecayComponent = new DCAPopularityDecayComponent(this.mDCAServiceConfig.getAggregateDefaults().getPopularity(),
                "item-row", false, false);

        PropertysetItem item = new PropertysetItem();
        item.addItemProperty("name", new ObjectProperty<String>(name.getTextField().getValue()));
        item.addItemProperty("description", new ObjectProperty<String>(name.getTextField().getValue()));

        FieldGroup binder = new FieldGroup(item);
        binder.bindMemberFields(this);
        binder.bind(name.getTextField(),"name");
        binder.bind(description.getTextField(), "description");

        this.popularityDecayComponent.updatePopularityDecayComponentState(true);

        this.cancelButton.setCaption("CANCEL");
        this.cancelButton.setStyleName("btn-primary");
        this.cancelButton.addClickListener(event -> {
            // todo .. this feels a bit shaky. Need to improve.
            DCABreadCrumb breadCrumb = getLayoutContainer().getBreadCrumb();
            breadCrumb.navigateTo(AGGREGATES_LABEL);
        });

        this.saveButton.setCaption("SAVE");
        this.saveButton.setStyleName("btn-primary");
        this.saveButton.addClickListener(event -> {
            if (!isValidForm()) {
                return;
            }
            createAggregate();
        });

        CssLayout aggregateForm = new CssLayout();
        aggregateForm.setStyleName("create-aggregate-form");

        DCALabel formHeader = new DCALabel("Aggregate Detail", "form-header");

        aggregateForm.addComponent(formHeader);

        aggregateForm.addComponent(wrapInput(name, 100));
        aggregateForm.addComponent(wrapInput(description, 100));

        aggregateForm.addComponent(wrapInput(type, 50));
        aggregateForm.addComponent(staticComponent);
        aggregateForm.addComponent(wrapInput(entityTaxon, 50));
        aggregateForm.addComponent(wrapInput(clusterTaxon, 50));
        aggregateForm.addComponent(this.tagsComponent);
        aggregateForm.addComponent(this.popularityDecayComponent);
        aggregateForm.addComponent(saveButton);
        aggregateForm.addComponent(cancelButton);

        this.addComponent(aggregateForm);
    }

    private Component wrapInput(Component input, float size) {
        CssLayout wrapper = new CssLayout();
        wrapper.setWidth(size, Unit.PERCENTAGE);
        wrapper.addComponent(input);
        wrapper.addStyleName("create-aggregate-form-field-wrapper");
        return wrapper;
    }

    private void createAggregate() {
        this.newAggregateItem = new DCAAggregateItem();
        this.newAggregateItem.setDescription(this.description.getTextField().getValue());
        this.newAggregateItem.setType(this.type.getComboBox().getValue() == null ? "" : String.valueOf(this.type.getComboBox().getValue()));
        this.newAggregateItem.setEntityTaxon(this.entityTaxon.getComboBox().getValue() == null ? "" : String.valueOf(this.entityTaxon.getComboBox().getValue()));
        this.newAggregateItem.setClusterTaxon(this.clusterTaxon.getComboBox().getValue() == null ? "" : String.valueOf(this.clusterTaxon.getComboBox().getValue()));
        this.newAggregateItem.setTags(this.tagsComponent.getTagItemList());
        this.newAggregateItem.setStatic(!this.isStatic.getValue());

        if (this.popularityDecayComponent.getOverridePopularityDecay().getValue()) {
            this.newAggregatePopularity = new DCAAggregatePopularity();
            this.newAggregatePopularity.setMaxCacheAge(this.popularityDecayComponent.getMaxCacheAge().getValue());

            this.newAggregatePopularity.setShortTerm(new DCAAggregatePopularityTerm());
            this.newAggregatePopularity.getShortTerm().setHalftime(this.popularityDecayComponent.getShortTermHalfTime().getValue());
            this.newAggregatePopularity.getShortTerm().setWeight(this.popularityDecayComponent.getShortTermWeight().getValue());

            this.newAggregatePopularity.setLongTerm(new DCAAggregatePopularityTerm());
            this.newAggregatePopularity.getLongTerm().setHalftime(this.popularityDecayComponent.getLongTermHalfTime().getValue());
            this.newAggregatePopularity.getLongTerm().setWeight(this.popularityDecayComponent.getLongTermWeight().getValue());

            this.newAggregateItem.setPopularity(this.newAggregatePopularity);
        }

        Observable<DCAAggregateCreateResponse> aggregateCreateResponseObservable =
                getDashboardPresenter().createAggregate(newAggregateItem, name.getTextField().getValue(), getLoggedInUser());

        aggregateCreateResponseObservable.subscribe(this::onSuccess, this::onError);
    }

    private void onSuccess(DCAAggregateCreateResponse aggregateCreateResponse) {
        getLayoutContainer().getWidgetContainer().removeAllComponents();
        this.newAggregateItem.setIsOverriden(this.popularityDecayComponent.getOverridePopularityDecay().getValue());
        if (this.popularityDecayComponent.getOverridePopularityDecay().getValue()) {
            this.newAggregateItem.setPopularity(this.newAggregatePopularity);
        } else {
            this.newAggregateItem.setPopularity(mDCAServiceConfig.getAggregateDefaults().getPopularity());
        }
        DCABreadCrumb breadCrumb = getLayoutContainer().getBreadCrumb();
        breadCrumb.removeAction(CREATE_NEW_LABEL);
        DCAAggregateViewWrapperComponent dcaAggregateViewWrapperComponent = new DCAAggregateViewWrapperComponent(this.name.getTextField().getValue(),
                this.newAggregateItem, getDashboardPresenter(), aggregateCreateResponse.getOkMessage(), getLayoutContainer());
        addComponentAsLast(dcaAggregateViewWrapperComponent, getLayoutContainer().getWidgetContainer());
    }

    private void onError(Throwable throwable) {
        throwable.printStackTrace();
        logger.error("Error : ", throwable);

        for (int counter = 0; counter < currentComponent.getComponentCount(); counter++) {
            Component component = currentComponent.getComponent(counter);

            if (component instanceof DCAPopupMessageComponent) {
                removeComponent(component, currentComponent);
            }
        }

        DCAPopupMessageComponent popupMessageComponent = new DCAPopupErrorComponent("Error:", throwable.getMessage(), currentComponent);
        addComponentAsLast(popupMessageComponent, currentComponent);
    }

    @Override
    public void updateWidgetContainer(String clickedComponentId) {
        if ("cancel-button-id".equals(clickedComponentId)) {
            getLayoutContainer().getWidgetContainer().removeAllComponents();
            DCAControllerOverviewComponent controllerOverviewComponent = new DCAControllerOverviewComponent(getDashboardPresenter(), getLayoutContainer());
            addComponentAsLast(controllerOverviewComponent, getLayoutContainer().getWidgetContainer());
        }
    }
}
