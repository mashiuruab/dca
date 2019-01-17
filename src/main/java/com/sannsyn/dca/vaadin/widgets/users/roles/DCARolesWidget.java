package com.sannsyn.dca.vaadin.widgets.users.roles;

import com.google.gson.JsonObject;
import com.sannsyn.dca.service.DCAAdminService;
import com.sannsyn.dca.vaadin.component.custom.container.collapsible.DCAColumnSpec;
import com.sannsyn.dca.vaadin.component.custom.container.simple.DCAClickableItemContainer;
import com.sannsyn.dca.vaadin.component.custom.container.simple.DCAClickableItemContainerImpl;
import com.sannsyn.dca.vaadin.component.custom.container.simple.DCASimpleItemContainer;
import com.sannsyn.dca.vaadin.component.custom.field.DCATextField;
import com.sannsyn.dca.vaadin.component.custom.notification.DCANotification;
import com.sannsyn.dca.vaadin.helper.OnEnterKeyHandler;
import com.sannsyn.dca.vaadin.icons.SannsynIcons;
import com.vaadin.ui.*;
import rx.Observable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.vaadin.server.Sizeable.Unit.PERCENTAGE;

/**
 * The roles widget. Initially it will show the list of all roles.
 * <p>
 * Created by jobaer on 7/3/17.
 */
public class DCARolesWidget extends CustomComponent {
    private UI currentUi;

    private DCAAdminService adminService = new DCAAdminService();

    private CssLayout rootLayout = new CssLayout();
    private DCAClickableItemContainer container = new DCAClickableItemContainerImpl();
    private Label heading = new Label("Manage roles");

    public DCARolesWidget(UI current) {
        currentUi = current;
        setupRootComponent();
    }

    private void setupRootComponent() {
        rootLayout.setWidth(100, PERCENTAGE);
        rootLayout.addStyleName("roles-widget-root");
        rootLayout.addStyleName("recomndr-container-component");

        addHeader(rootLayout);
        addRoleFilterUi(rootLayout);
        addRoleListUi(rootLayout);

        setCompositionRoot(rootLayout);
    }

    private void addRoleListUi(CssLayout layout) {
        List<DCAColumnSpec> columnSpecs = Arrays.asList(
            new DCAColumnSpec("Name", 40, "name"),
            new DCAColumnSpec("Description", 60, "description")
        );

        container.setColumnSpecs(columnSpecs);
        requestUpdatedData(container);
        container.registerClickHandler(this::createRoleEditDialog);

        layout.addComponent(container);
    }

    private void requestUpdatedData(DCASimpleItemContainer container) {
        Observable<List<JsonObject>> rolesListObservable = adminService.getAllRoles().toList();
        rolesListObservable.subscribe(
            rolesList -> currentUi.access(() -> {
                container.clear();
                container.addItems(rolesList);
            }),
            e -> {
                DCANotification.ERROR.show("Error occurred while fetching all roles");
                currentUi.access(() -> container.addItems(new ArrayList<>())); // this will trigger the smile face empty result to show
            });
    }

    private void createRoleEditDialog(JsonObject item) {
        new DCAEditRoleDialog(currentUi, rootLayout, item, () -> requestUpdatedData(container), adminService);
        currentUi.scrollIntoView(heading);
    }

    private void addRoleFilterUi(CssLayout layout) {
        CssLayout filterLayout = new CssLayout();
        filterLayout.addStyleName("role-filter-root");

        DCATextField searchInput = new DCATextField(SannsynIcons.SEARCH, false);
        searchInput.setInputPrompt("Search for roles or role descriptions");
        searchInput.setWidth(80, PERCENTAGE);
        filterLayout.addComponent(searchInput);

        CssLayout spacer = new CssLayout();
        spacer.addStyleName("role-filter-spacer");
        spacer.setWidth(1, PERCENTAGE);
        filterLayout.addComponent(spacer);

        Button searchButton = new Button("SEARCH");
        searchButton.addStyleName("btn-primary-style");
        searchButton.setWidth(19, PERCENTAGE);
        filterLayout.addComponent(searchButton);

        searchInput.installEnterKeyHandler(new OnEnterKeyHandler() {
            @Override
            public void onEnterKeyPressed() {
                String value = searchInput.getTextField().getValue();
                doSearch(value);
            }
        });
        searchButton.addClickListener(event -> {
            String value = searchInput.getTextField().getValue();
            doSearch(value);
        });

        layout.addComponent(filterLayout);
    }

    private void doSearch(String query) {
        container.clear(); // called from listeners, no need for UI.access
        Observable<List<JsonObject>> rolesListObservable = adminService.getRolesByFilter(query).toList();
        rolesListObservable.subscribe(
            rolesList -> currentUi.access(() -> container.addItems(rolesList)),
            e -> {
                DCANotification.ERROR.show("Error occurred while searching for roles");
                currentUi.access(() -> container.addItems(new ArrayList<>())); // this will trigger the smile face empty result to show
            });
    }

    private void addHeader(CssLayout layout) {
        heading.setWidth(100, PERCENTAGE);
        layout.addComponent(heading);
        heading.addStyleName("dca-widget-title-container");
    }
}
