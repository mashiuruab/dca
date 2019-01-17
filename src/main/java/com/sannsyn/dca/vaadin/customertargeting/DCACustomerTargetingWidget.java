package com.sannsyn.dca.vaadin.customertargeting;

import com.sannsyn.dca.model.config.DCAWidget;
import com.sannsyn.dca.model.user.DCAUser;
import com.sannsyn.dca.service.DCAConfigService;
import com.sannsyn.dca.vaadin.servlet.DCAUserPreference;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Layout;
import com.vaadin.ui.UI;
import rx.Observable;

import static com.sannsyn.dca.vaadin.ui.DCAUiHelper.addSeparator;
import static com.vaadin.server.Sizeable.Unit.PERCENTAGE;

/**
 * The customer targeting widget.
 * <p>
 * Created by jobaer on 8/16/16.
 */
public class DCACustomerTargetingWidget extends CustomComponent {
    private final DCAUser loggedInUser;
    private final DCAWidget config;
    private UI mainUi;
    private DCACustomerTargetingSelectedItems selectedItems;
    private DCACustomerTargetingSearchComponent searchComponent;
    private DCACustomerTargetingActionComponent searchActionComponent;

    public DCACustomerTargetingWidget(UI mainUi, DCAUser loggedInUser, DCAWidget widgetConfig) {
        this.loggedInUser = loggedInUser;
        this.mainUi = mainUi;
        this.config = widgetConfig;
        selectedItems = createSelectedItems(widgetConfig);
        searchComponent = new DCACustomerTargetingSearchComponent(
            mainUi,
            loggedInUser,
            widgetConfig,
            b -> selectedItems.add(b),
            b -> selectedItems.contains(b),
            v -> selectedItems.setLabelVisibility(v), () -> selectedItems.getItems().size() < 20);
        Layout rootUi = createUi();
        setCompositionRoot(rootUi);
    }

    private Layout createUi() {
        CssLayout cssLayout = new CssLayout();
        cssLayout.setWidth(100, PERCENTAGE);
        cssLayout.addStyleName("customer-targeting");

        cssLayout.addComponent(searchComponent.getSearchInput());
        cssLayout.addComponent(selectedItems);
        cssLayout.addComponent(searchComponent.getSearchResult());
        addSeparator(cssLayout);

        searchActionComponent = createEmailActionUi();
        cssLayout.addComponent(searchActionComponent);

        return cssLayout;
    }

    private DCACustomerTargetingSelectedItems createSelectedItems(DCAWidget widgetConfig) {
        return new DCACustomerTargetingSelectedItems( widgetConfig, b -> searchComponent.updateResultList());
    }

    private DCACustomerTargetingActionComponent createEmailActionUi() {
        return new DCACustomerTargetingActionComponent(config, selectedItems::getItems, mainUi, loggedInUser);
    }

    // todo Find a suitable place for this method
    static String getServiceName() {
        DCAConfigService dcaConfigService = new DCAConfigService();
        DCAUser loggedInUser = DCAUserPreference.getLoggedInUser();
        Observable<String> stringObservable = dcaConfigService.getSelectedService(loggedInUser).flatMap(selectedService -> {
            String name = selectedService.getServiceIdentifier();
            return Observable.just(name);
        });

        return stringObservable.toBlocking().first();
    }

}
