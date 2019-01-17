package com.sannsyn.dca.vaadin.ui;

import com.google.gson.JsonObject;
import com.sannsyn.dca.model.user.DCAUser;
import com.sannsyn.dca.vaadin.helper.DCAFileDownloadWithSupplier;
import com.sannsyn.dca.vaadin.helper.OnDemandFileDownloader;
import com.sannsyn.dca.vaadin.servlet.DCAUserPreference;
import com.sannsyn.dca.vaadin.widgets.analytics.DCAEditAnalyticsFormComponent;
import com.sannsyn.dca.vaadin.widgets.analytics.DCASalesByRecommendationFields;
import com.vaadin.annotations.Push;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Widgetset;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.*;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

/**
 * Created by mashiur on 2/22/16.
 */
@Push
@Theme("dcatheme")
@Widgetset("com.sannsyn.dca.DCAAppWidgetset")
public class DCADemoUI extends UI {
    private CssLayout layout;

    @Override
    protected void init(final VaadinRequest request) {
        layout = new CssLayout();
        layout.setStyleName("demo-ui");
        Component component = createTestUi();
        layout.addComponent(component);

        DCAUser loggedInUser = DCAUserPreference.getLoggedInUser();
        Label test = new Label("Not logged in");
        if (loggedInUser != null) {
            test.setValue(loggedInUser.getUsername());
        }

        Button button = new Button("Test new window");
        button.addClickListener(click -> {
            Page.getCurrent().open("http://localhost:8080/vaadin-test", "_blank");
        });

        layout.addComponent(test);
        layout.addComponent(button);

        setContent(layout);
    }

    private Component createTestUi() {
        CssLayout layout = new CssLayout();
        layout.setWidth(640, Unit.PIXELS);

        Component widget = createFormUi();
//        layout.addComponent(widget);
        Component test = createTestComponents();
        layout.addComponent(test);

        return layout;
    }

    private Component createTestComponents() {
        CssLayout test = new CssLayout();
        test.setWidth(100, Unit.PERCENTAGE);
        test.addComponent(new Label("Testing..."));

        DCASalesByRecommendationFields fields = new DCASalesByRecommendationFields();
        fields.setData(new JsonObject());
        test.addComponent(fields);

        return test;
    }

    private Component createFormUi() {
        return new DCAEditAnalyticsFormComponent(
            new JsonObject());
    }

    private void createDownloadUi(Layout root) {
        TextField input = new TextField();
        root.addComponent(input);

        final OnDemandFileDownloader.MutableStreamSource streamSource = new OnDemandFileDownloader.MutableStreamSource();
        streamSource.setFileName("customerData.json");

        Button downloadButton = new Button("Downlaod");
        DCAFileDownloadWithSupplier downloader = new DCAFileDownloadWithSupplier(streamSource, this::getBackendData);
        downloader.extend(downloadButton);

        root.addComponent(downloadButton);
    }

    private String getBackendData() {
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target("http://localhost:9000/admin/service");
        return target.request(MediaType.APPLICATION_FORM_URLENCODED_TYPE).get().readEntity(String.class);
    }
}
