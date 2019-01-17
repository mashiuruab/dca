package com.sannsyn.dca.vaadin.component.custom.navigation;

import com.sannsyn.dca.i18n.Messages;
import com.sannsyn.dca.model.config.DCAContainers;
import com.sannsyn.dca.model.config.DCASelectedService;
import com.sannsyn.dca.presenter.DCAAdminPresenter;
import com.sannsyn.dca.presenter.DCADashboardPresenter;
import com.sannsyn.dca.vaadin.component.custom.field.DCAError;
import com.sannsyn.dca.vaadin.component.custom.navigation.collapse.DCACollapseMenuItem;
import com.sannsyn.dca.vaadin.component.custom.navigation.collapse.DCACollapseMenuObserver;
import com.sannsyn.dca.vaadin.component.custom.navigation.item.DCALeftPanelItem;
import com.sannsyn.dca.vaadin.component.custom.navigation.item.DCALeftPanelItemObserver;
import com.sannsyn.dca.vaadin.component.custom.navigation.item.DCALeftPanelSubMenuObserver;
import com.sannsyn.dca.vaadin.servlet.DCAUtils;
import com.sannsyn.dca.vaadin.view.DCALayoutContainer;
import com.sannsyn.dca.vaadin.widgets.operations.controller.component.DCAWidgetContainerComponent;
import com.vaadin.ui.CssLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.util.ArrayList;
import java.util.List;

import static com.sannsyn.dca.vaadin.ui.DCAUiHelper.runInUiThread;

/**
 * Created by mashiur on 3/4/16.
 */
public class DCALeftPanelContainer extends DCAWidgetContainerComponent {
    private static final Logger logger = LoggerFactory.getLogger(DCALeftPanelContainer.class);

    private List<DCALeftPanelItem> dcaLeftPanelItemList = new ArrayList<DCALeftPanelItem>();
    private DCALeftPanelItemObserver dcaLeftPanelItemObserver = new DCALeftPanelItemObserver();
    private DCALeftPanelSubMenuObserver dcaLeftPanelSubMenuObserver = new DCALeftPanelSubMenuObserver();

    /*TODO: This should be parsed from the DCAStructure json file*/
    private String logoUrl = "static/img/footer-logo.jpg";
    private String smallLogoUrl  =  "static/img/footer-logo-small.png";

    /*member variables related to collapse menu*/
    private String collapseMenuCaption = Messages.getInstance().getMessage("left.panel.collapse.menu");
    private DCACollapseMenuObserver dcaCollapseMenuObserver = new DCACollapseMenuObserver();
    private DCACollapseMenuItem dcaCollapseMenuItem = new DCACollapseMenuItem(collapseMenuCaption, dcaCollapseMenuObserver);

    private Observable<DCASelectedService> selectedServiceObservable;

    private String identifier;
    private List<DCAContainers> dcaContainersList;
    private DCADashboardPresenter dcaDashboardPresenter;
    private DCAAdminPresenter adminPresenter;

    private CssLayout navHolder = new CssLayout();
    private CssLayout footerHolder = new CssLayout();
    private DCASelectedServiceComponent serviceInfoHolderComponent;
    private DCAExternalImageComponent customerLogoComponent;


    public DCALeftPanelContainer(DCADashboardPresenter dcaDashboardPresenter, String pIdentifier,
                                 DCALayoutContainer layoutContainer, DCAAdminPresenter adminPresenter) {
        this.setStyleName("left-panel-container global-nav-wrapper");
        setLayoutContainer(layoutContainer);
        getLayoutContainer().setLeftPanelContainer(this);

        this.dcaCollapseMenuObserver.attach(this);
        this.dcaDashboardPresenter = dcaDashboardPresenter;
        this.adminPresenter = adminPresenter;
        this.identifier = pIdentifier;

        try {
            this.dcaContainersList = this.dcaDashboardPresenter.getLeftMenuContainers(this.identifier, getLoggedInUser());

            init();

            Observable<DCASelectedService> selectedServiceObservable = DCAUtils.getTargetService();
            selectedServiceObservable.subscribe(dcaSelectedService -> {
                loadCustomerLogoComponent(dcaSelectedService);
                loadSelectedServiceInfoComponent(dcaSelectedService);
            }, throwable -> {
                logger.error("Error : ", throwable);
            });
        } catch (Exception e) {
            logger.info("Error :", e);
            addComponentAsLast(new DCAError("Error Happened in the Graph Component Loading"), this);
        }
    }

    private void init() {
        navHolder.setStyleName("nav-holder");

        footerHolder.setStyleName("footer-holder");

        for (DCALeftPanelItem dcaLeftPanelItem : getLeftPanelItems()) {
            navHolder.addComponent(dcaLeftPanelItem);
        }


        navHolder.addComponent(dcaCollapseMenuItem);
        this.addComponent(navHolder);
        this.addComponent(footerHolder);

        DCAExternalImageComponent footerWithText = new DCAExternalImageComponent(this.logoUrl);
        footerHolder.addComponent(footerWithText);

        DCAExternalImageComponent footerWithoutText = new DCAExternalImageComponent(this.smallLogoUrl);
        footerWithoutText.addStyleName("small");
        footerHolder.addComponent(footerWithoutText);
    }


    public void loadCustomerLogoComponent(DCASelectedService selectedService) {
        runInUiThread(() -> {
            updateCustomerLogoComponent(selectedService);
        });
    }

    private void updateCustomerLogoComponent(DCASelectedService selectedService) {
        if (customerLogoComponent != null) {
            removeComponent(customerLogoComponent, this);
        }
        customerLogoComponent = new DCAExternalImageComponent(selectedService);
        addComponentAsFirst(customerLogoComponent, this);
    }

    public void loadSelectedServiceInfoComponent(DCASelectedService selectedService) {
        runInUiThread(() -> {
            updateSelectedServiceInformation(selectedService);
        });
    }

    private void updateSelectedServiceInformation(DCASelectedService selectedService) {
        if (serviceInfoHolderComponent != null) {
            removeComponent(serviceInfoHolderComponent, footerHolder);
        }
        serviceInfoHolderComponent = new DCASelectedServiceComponent(selectedService);
        addComponentAsLast(serviceInfoHolderComponent, footerHolder);
    }

    public List<DCALeftPanelItem> getLeftPanelItems() {
        for(DCAContainers dcaContainers : this.dcaContainersList) {
            DCALeftPanelItem dcaLeftPanelItem = new DCALeftPanelItem(dcaContainers, dcaLeftPanelItemObserver,
                    dcaLeftPanelSubMenuObserver, getLayoutContainer(), dcaDashboardPresenter, adminPresenter);
            this.dcaLeftPanelItemList.add(dcaLeftPanelItem);
        }
        return this.dcaLeftPanelItemList;
    }

    public DCALeftPanelItemObserver getDcaLeftPanelItemObserver() {
        return dcaLeftPanelItemObserver;
    }

    public DCALeftPanelSubMenuObserver getDcaLeftPanelSubMenuObserver() {
        return dcaLeftPanelSubMenuObserver;
    }

}
