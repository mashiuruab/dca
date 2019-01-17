package com.sannsyn.dca.vaadin.component.custom.navigation.item;

import com.sannsyn.dca.i18n.Messages;
import com.sannsyn.dca.model.config.DCAContainers;
import com.sannsyn.dca.presenter.DCAAdminPresenter;
import com.sannsyn.dca.presenter.DCADashboardPresenter;
import com.sannsyn.dca.vaadin.component.custom.icon.DCAIcon;
import com.sannsyn.dca.vaadin.component.custom.navigation.DCAWidgetGenerator;
import com.sannsyn.dca.vaadin.view.DCALayoutContainer;
import com.sannsyn.dca.vaadin.widgets.operations.controller.component.DCAWidgetContainerComponent;
import com.vaadin.event.LayoutEvents;
import com.vaadin.server.Page;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by mashiur on 2/25/16.
 */
public class DCALeftPanelItem extends DCAWidgetContainerComponent {
    private static  final Logger logger = LoggerFactory.getLogger(DCALeftPanelItem.class);

    private static final String SELECTED_MENU_ITEM_CLS_NAME = "selected-menu-item";
    private static final String CLICKED_WRAPPER_STYLE_NAME = "clicked-dcaLeftPanel";

    private static final String HOME_ICON = "icon-dashboard";
    private static final String EYE_ICON = "icon-inspect-item";
    private static final String CONTROLLER_ICON = "icon-controller";
    private static final String ANALYTICS_ICON = "icon-analytics";
    public static final String RIGHT_ARROW_ICON = "icon-right";
    public static final String DOWN_ARROW_ICON = "icon-down";
    public static final String UP_ARROW_ICON = "icon-up";
    private static final String DEFAULT_ICON = "icon-settings";
    private static final String USERS_ICON = "icon-users";
    private static final String ROLES_ICON = "icon-roles";

    private Map<String, Label> leftMenuItemIcons = new HashMap<String, Label>() {{
        put("dashboard", new DCAIcon(HOME_ICON));
        put("inspectItem", new DCAIcon(EYE_ICON));
        put("controller", new DCAIcon(CONTROLLER_ICON));
        put("analytics", new DCAIcon(ANALYTICS_ICON));
        put("customer-targeting", new DCAIcon(ANALYTICS_ICON));
        put("dca-setup", new DCAIcon(ANALYTICS_ICON));
        put("dca-users", new DCAIcon(USERS_ICON));
        put("Help", new DCAIcon(HOME_ICON));
        put("defaultIcon", new DCAIcon(DEFAULT_ICON));
    }};

    private static final Map<String, String> i18nKeyMap = new HashMap<String, String>() {{
        put("dashboard", "left.panel.dashboard");
        put("inspectItem", "left.panel.inspect.item");
        put("controller", "left.panel.controller");
        put("aggregate", "left.panel.aggregate");
        put("pipeline", "left.panel.pipeline");
        put("inspect", "left.panel.inspect");
        put("pipes", "left.panel.pipes");
        put("recommenders", "left.panel.recommenders");
        put("inspect-assembly", "left.panel.inspect-assembly");
        put("analytics", "left.panel.analytics");
        put("customer-targeting", "left.panel.customer-targeting");
        put("dca-setup", "left.panel.dca.setup");
        put("dca-users","left.panel.users");
        put("dca-roles", "left.panel.roles");
        put("Help", "left.panel.help");
    }};

    private List<String> subMenuItemList = new ArrayList<>();

    /*Item Components*/
    private Label leftIconLabel;
    private Link labelName = new Link();
    private DCAIcon rightIconLabel = new DCAIcon(RIGHT_ARROW_ICON,"menu-item-rightLink", "");
    private List<DCALeftSubMenuItem> subMenuItems = new ArrayList<DCALeftSubMenuItem>();

    private Link floatingLabelLink = new Link();
    private DCAIcon floatingRightIconLabel = new DCAIcon(RIGHT_ARROW_ICON,"menu-item-rightLink", "");
    private List<DCALeftSubMenuItem> floatingSubMenuItems = new ArrayList<DCALeftSubMenuItem>();
    private CssLayout floatingMenuItemWrapper = new CssLayout();


    private boolean isSelected = false;

    private DCALeftPanelItem currentItem;
    private DCALeftPanelItemObserver leftPanelItemObserver;
    private DCALeftPanelSubMenuObserver dcaLeftPanelSubMenuObserver;

    private DCAWidgetGenerator dcaWidgetGenerator;
    private String itemId;
    private boolean hasSubMenu;

    private CssLayout mainMenuItem = new CssLayout();


    public DCALeftPanelItem(DCAContainers pDCAContainers, DCALeftPanelItemObserver pLeftPanelItemObserver,
                            DCALeftPanelSubMenuObserver dcaLeftPanelSubMenuObserver, DCALayoutContainer layoutContainer,
                            DCADashboardPresenter pDCADashboardPresenter, DCAAdminPresenter adminPresenter) {
        this.setId(String.format("%s-%s", pDCAContainers.getName(), "dcaLeftPanel"));

        pDCAContainers.getSubmenu().forEach(targetSubMenuItems -> subMenuItemList.add(targetSubMenuItems.getName()));
        this.itemId = pDCAContainers.getName();

        this.currentItem = this;
        setLayoutContainer(layoutContainer);
        this.leftPanelItemObserver = pLeftPanelItemObserver;
        this.dcaLeftPanelSubMenuObserver = dcaLeftPanelSubMenuObserver;
        this.leftPanelItemObserver.attach(currentItem);

        if(leftMenuItemIcons.containsKey(pDCAContainers.getName())) {
            this.leftIconLabel = leftMenuItemIcons.get(pDCAContainers.getName());
        } else {
            this.leftIconLabel = leftMenuItemIcons.get("defaultIcon");
        }

        this.labelName.setCaption(getLabel(pDCAContainers.getName()));
        this.labelName.setCaptionAsHtml(true);
        this.floatingLabelLink.setCaption(getLabel(pDCAContainers.getName()));
        this.floatingLabelLink.setCaptionAsHtml(true);

        this.dcaWidgetGenerator = new DCAWidgetGenerator(pDCADashboardPresenter, adminPresenter, getLoggedInUser());

        this.hasSubMenu = !subMenuItemList.isEmpty();

        init();

        createSubMenuItems();

        createFloatingSubMenuElement();
    }

    public void updateWidgetContainer(String clickedComponentId) {
        this.dcaWidgetGenerator.updateWidgetComponents(getLayoutContainer(), clickedComponentId);
    }

    public CssLayout getFloatingMainMenuItemWrapper() {
        return floatingMenuItemWrapper;
    }

    private String getLabel(String itemName) {
        if (i18nKeyMap.containsKey(itemName)) {
            return Messages.getInstance().getMessage(i18nKeyMap.get(itemName));
        } else {
            return itemName;
        }
    }

    public List<DCALeftSubMenuItem> getSubMenuItems() {
        return subMenuItems;
    }

    public void setSubMenuItems(List<DCALeftSubMenuItem> subMenuItems) {
        this.subMenuItems = subMenuItems;
    }

    public boolean isSelected() {
        return isSelected;
    }


    public CssLayout getMainMenuItem() {
        return mainMenuItem;
    }

    private void changeIndicatorState() {
        isSelected = !isSelected;

        if (isSelected && hasSubMenu) {
            this.rightIconLabel.updateValue(DOWN_ARROW_ICON);
            this.floatingRightIconLabel.updateValue(DOWN_ARROW_ICON);
        }
        else if (hasSubMenu) {
            this.rightIconLabel.updateValue(RIGHT_ARROW_ICON);
            this.floatingRightIconLabel.updateValue(RIGHT_ARROW_ICON);
        }

        if (currentItem.getStyleName().contains(CLICKED_WRAPPER_STYLE_NAME)) {
            currentItem.removeStyleName(CLICKED_WRAPPER_STYLE_NAME);
        } else {
            currentItem.addStyleName(CLICKED_WRAPPER_STYLE_NAME);
        }

        Page.getCurrent().getJavaScript().execute("adjustFooterLogo()");


    }

    private void doNavigation(String navigatorId) {
        leftPanelItemObserver.resetSubscribers();
        dcaLeftPanelSubMenuObserver.resetSubscribers();
        mainMenuItem.addStyleName(SELECTED_MENU_ITEM_CLS_NAME);
        floatingMenuItemWrapper.addStyleName(SELECTED_MENU_ITEM_CLS_NAME);
        updateWidgetContainer(navigatorId);
    }

    private void init() {
        mainMenuItem.setId(itemId);
        mainMenuItem.setStyleName("dca-left-panel-item-wrapper");
        mainMenuItem.addComponent(this.leftIconLabel);
        mainMenuItem.addComponent(this.labelName);

        if (!this.hasSubMenu) {
            this.rightIconLabel.updateValue("dummy-element");
        }

        mainMenuItem.addComponent(this.rightIconLabel);

        String labelId = String.format("%s-%s", itemId, "label-id");
        this.labelName.setId(labelId);

        String toggleId = String.format("%s-%s", itemId, "toggle-id");
        this.rightIconLabel.setId(toggleId);

        mainMenuItem.addLayoutClickListener(new LayoutEvents.LayoutClickListener() {
            @Override
            public void layoutClick(LayoutEvents.LayoutClickEvent event) {
                String targetId = event.getChildComponent() == null ? "" : event.getChildComponent().getId();

                if (toggleId.equals(targetId)) {
                    changeIndicatorState();
                } else if (labelId.equals(targetId)) {
                    doNavigation(itemId);
                }
            }
        });

        this.addComponent(mainMenuItem);

        /*width should be 40%*/
        this.labelName.setStyleName("menu-item-label");
        this.floatingLabelLink.setStyleName("menu-item-label");

        this.setStyleName("menu-item-unit");
    }

    private void createFloatingSubMenuElement() {
        CssLayout floatingCssLayout = new CssLayout();
        String floatingComponentId = String.format("floating-%s", itemId);

        floatingMenuItemWrapper.setId(floatingComponentId);
        floatingMenuItemWrapper.setStyleName("menu-item-wrapper");

        floatingMenuItemWrapper.addComponent(floatingLabelLink);

        if (!this.hasSubMenu) {
            this.floatingRightIconLabel.updateValue("dummy-element");
        }

        floatingMenuItemWrapper.addComponent(floatingRightIconLabel);

        String labelId = String.format("%s-%s", floatingComponentId, "label-id");
        floatingLabelLink.setId(labelId);

        String toggleId = String.format("%s-%s", floatingComponentId, "toggle-id");
        floatingRightIconLabel.setId(toggleId);

        floatingMenuItemWrapper.addLayoutClickListener(event -> {
            String targetId = event.getChildComponent().getId();

            if (toggleId.equals(targetId)) {
                changeIndicatorState();
            } else if (labelId.equals(targetId)) {
                dcaLeftPanelSubMenuObserver.notifySubscribers(floatingComponentId);
                doNavigation(floatingComponentId);
            }
        });

        floatingCssLayout.setStyleName("left-panel-floating-element");
        floatingCssLayout.addComponent(floatingMenuItemWrapper);

        for (DCALeftSubMenuItem dcaLeftSubMenuItem : floatingSubMenuItems) {
            floatingCssLayout.addComponent(dcaLeftSubMenuItem);
        }

        this.addComponent(floatingCssLayout);
    }

    private void createSubMenuItems() {

        for (String subMenuLabelId : this.subMenuItemList) {
            String subMenuLabel = i18nKeyMap.containsKey(subMenuLabelId) ?
                    Messages.getInstance().getMessage(i18nKeyMap.get(subMenuLabelId)) : subMenuLabelId;

            DCAWidgetGenerator widgetGenerator = dcaWidgetGenerator;

            DCALeftSubMenuItem subMenuItem = new DCALeftSubMenuItem(String.format("%s-%s", itemId, subMenuLabelId),
                    subMenuLabel, widgetGenerator, getLayoutContainer(), dcaLeftPanelSubMenuObserver, leftPanelItemObserver);
            DCALeftSubMenuItem floatingSubMenuItem = new DCALeftSubMenuItem(String.format("floating-%s-%s", itemId, subMenuLabelId),
                    subMenuLabel, widgetGenerator, getLayoutContainer(), dcaLeftPanelSubMenuObserver, leftPanelItemObserver);

            dcaLeftPanelSubMenuObserver.attach(subMenuItem);
            dcaLeftPanelSubMenuObserver.attach(floatingSubMenuItem);

            this.addComponent(subMenuItem);
            this.getSubMenuItems().add(subMenuItem);
            this.floatingSubMenuItems.add(floatingSubMenuItem);
        }

    }

}
