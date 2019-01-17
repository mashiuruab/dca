package com.sannsyn.dca.vaadin.widgets.admin.dcasetup.component;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import com.sannsyn.dca.model.config.DCASelectedService;
import com.sannsyn.dca.presenter.DCAAdminPresenter;
import com.sannsyn.dca.service.DCACommonService;
import com.sannsyn.dca.service.DCAConfigService;
import com.sannsyn.dca.vaadin.component.custom.DCAConfirmDialog;
import com.sannsyn.dca.vaadin.component.custom.DCAWrapper;
import com.sannsyn.dca.vaadin.component.custom.field.DCAComboBox;
import com.sannsyn.dca.vaadin.component.custom.field.DCALabel;
import com.sannsyn.dca.vaadin.view.DCALayoutContainer;
import com.sannsyn.dca.vaadin.widgets.common.DCABreadCrumb;
import com.sannsyn.dca.vaadin.widgets.operations.controller.component.DCAWidgetContainerComponent;
import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ShortcutListener;
import com.vaadin.ui.Button;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.UI;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.dialogs.ConfirmDialog;
import rx.Observable;

import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.Collections;

/**
 * Created by mashiur on 5/26/17.
 */
public class DCAServiceWidget extends DCAWidgetContainerComponent {
    private static final Logger logger = LoggerFactory.getLogger(DCAServiceWidget.class);

    private DCASelectedService targetService;

    private DCAComboBox pathField = new DCAComboBox();
    private TextArea jsonArea = new TextArea();
    private DCALabel endpointAddressLabel = new DCALabel("", "endpoint-label");
    private DCALabel selectedPathLabel = new DCALabel("", "selected-path");
    private Button saveButton = new Button("Save");
    private Button deleteButton = new Button("Delete");

    public DCAServiceWidget(DCASelectedService targetService, DCALayoutContainer layoutContainer,
                            DCAAdminPresenter adminPresenter) {
        this.targetService = targetService;

        setLayoutContainer(layoutContainer);
        setAdminPresenter(adminPresenter);
        setCurrentComponent(this);

        try {
            init();
        } catch (Exception e) {
            onError(e);
        }
    }

    private void onError(Throwable throwable) {
        logger.error("Error : ", throwable);

        if (throwable instanceof PathNotFoundException) {
            String message = String.format("The path doesn’t exist right now but you can create it. <br/> %s",
                    throwable.getMessage());
            showWarningNotification(message);
        } else {
            showErrorNotification(throwable);
        }
    }

    private String getPrettyJson(Object jsonObject) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String jsonString = gson.toJson(jsonObject);

        if (jsonObject instanceof String) {
            jsonString = jsonObject.toString();
        }

        JsonParser jsonParser = new JsonParser();
        JsonElement jsonElement = jsonParser.parse(jsonString);

        return gson.toJson(jsonElement);
    }

    private String getRootEndPoint() {
        return DCAConfigService.SERVICE_CONFIG_URI
                .replace("<endpoint>", targetService.getServiceEndpoint().getEndpointAddress())
                .replace("<serviceidentifier>", targetService.getServiceIdentifier());
    }

    private String getUpdateConfigEndPoint() {
        return DCACommonService.UPDATE_SRVS_CONFIG_TMPLT
                .replace("<endpoint>", targetService.getServiceEndpoint().getEndpointAddress())
                .replace("<serviceidentifier>", targetService.getServiceIdentifier());
    }

    private String getDeleteConfigEndPoint() {
        return DCACommonService.DELETE_SRVS_CONFIG_TMPLT
                .replace("<endpoint>", targetService.getServiceEndpoint().getEndpointAddress())
                .replace("<serviceidentifier>", targetService.getServiceIdentifier());
    }

    private String getPathURI() {
        return pathField.getValue() == null ? "" : pathField.getValue().toString();
    }

    private String getResult(String source, String pathPattern) {
        try {
            DocumentContext jsonContext = JsonPath.parse(source);
            Object stringResponse = jsonContext.read(pathPattern);
            return getPrettyJson(stringResponse);
        } catch (PathNotFoundException pnfe) {
            throw pnfe;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Observable<String> responseString(String servicePath) {
        Observable<Response> responseObservable = getAdminPresenter().getEntity(getLoggedInUser(), getRootEndPoint());
        return responseObservable.map(response -> {
            String pathPattern = String.format("$.configurationStr.%s", servicePath);
            return getResult(response.readEntity(String.class), pathPattern);
        }).doOnError(this::onError);
    }


    private CssLayout getActionComponent() {
        CssLayout actionComponent = new CssLayout();
        actionComponent.setStyleName("action-component");

        Button fullConfigButton = new Button("View Full Configuration");
        fullConfigButton.setStyleName("btn-primary full-config");

        fullConfigButton.addClickListener(event -> {
            handleFullServiceConfig(getRootEndPoint());
        });

        pathField.setStyleName("path-text-field");
        pathField.setCaption("Path : ");
        pathField.getComboBox().setNewItemsAllowed(true);
        pathField.addValueChangeListener(event -> {
            UI.getCurrent().access(() -> {
                selectedPathLabel.setValue(getPathURI());
                jsonArea.setEnabled(StringUtils.isNotEmpty(getPathURI()));
                saveButton.setEnabled(StringUtils.isNotEmpty(getPathURI()));
                deleteButton.setEnabled(StringUtils.isNotEmpty(getPathURI()));

                if (StringUtils.isNotEmpty(getPathURI())) {
                    doOpen();
                }
            });
        });

        DCAWrapper pathFieldWrapper = new DCAWrapper(Collections.singletonList(pathField), "path-text-field-wrapper");

        actionComponent.addComponent(fullConfigButton);
        actionComponent.addComponent(pathFieldWrapper);

        return actionComponent;
    }

    private void doOpen() {
        String pathUrl = getPathURI();
        if (StringUtils.isEmpty(pathUrl)) {
            return;
        }

        Observable<String> stringObservable = responseString(pathUrl);
        stringObservable.subscribe(response -> {
            UI.getCurrent().access(() -> {
                jsonArea.setEnabled(true);
                saveButton.setEnabled(true);
                deleteButton.setEnabled(true);
                jsonArea.setValue(response);
                selectedPathLabel.setValue(pathUrl);
            });
        }, throwable -> {
            UI.getCurrent().access(() -> {
                jsonArea.setEnabled(true);
                jsonArea.setValue("");
            });
            onError(throwable);
        }, this::setRootPathLabel);
    }

    private void setRootPathLabel() {
        UI.getCurrent().access(() -> {
            endpointAddressLabel.setValue(getRootEndPoint());
        });
    }

    private void handleFullServiceConfig(String webserviceUrl) {
        Observable<Response> responseObservable = getAdminPresenter().getEntity(getLoggedInUser(), webserviceUrl);
        responseObservable.subscribe(response -> {
            if (response.getStatus() != 200) {
                return;
            }
            String responseString = response.readEntity(String.class);
            String prettyJsonString = getPrettyJson(responseString);
            UI.getCurrent().access(() -> {
                selectedPathLabel.setValue("Selected path: Full Configuration");
                jsonArea.setEnabled(true);
                jsonArea.setValue(prettyJsonString);
                jsonArea.setEnabled(false);
                saveButton.setEnabled(false);
                deleteButton.setEnabled(false);
            });
        }, this::onError, () -> {
            setRootPathLabel();
            showSuccessNotification(String.format("Successfully Fetched Json for url %s", webserviceUrl));
        });
    }

    private Button getSaveButton() {
        saveButton.setStyleName("btn-primary save");
        saveButton.setDisableOnClick(true);

        saveButton.addClickListener(event -> {
            if (StringUtils.isEmpty(getPathURI()) || StringUtils.isEmpty(jsonArea.getValue())) {
                return;
            }

            try {
                String endPointUrl = String.format("%s/%s", getUpdateConfigEndPoint(), getPathURI());
                Response response = getAdminPresenter().putEntity(endPointUrl, jsonArea.getValue(), getLoggedInUser());
                showSuccessNotification(response.readEntity(String.class));
                saveButton.setEnabled(true);
            } catch (Exception e) {
                saveButton.setEnabled(true);
                onError(e);
            }
        });

        return saveButton;
    }

    private Button getDeleteButton() {
        deleteButton.setStyleName("btn-primary delete");
        deleteButton.setDisableOnClick(true);

        deleteButton.addClickListener(event -> {
            if (StringUtils.isEmpty(getPathURI())) {
                return;
            }

            String dialogMessage = String.format("Do you really want to delete path %s", getPathURI());

            DCAConfirmDialog.show(getUI(), dialogMessage, new ConfirmDialog.Listener() {
                @Override
                public void onClose(ConfirmDialog dialog) {
                    if (dialog.isConfirmed()) {
                        String webserviceUrl = String.format("%s/%s", getDeleteConfigEndPoint(), getPathURI());
                        Observable<Response> responseObservable =
                                getAdminPresenter().getEntity(getLoggedInUser(), webserviceUrl);

                        responseObservable.subscribe(response -> {
                            String successMessage = String.format("Successfully Deleted the path %s and response found %s",
                                    getPathURI(), response.readEntity(String.class));
                            showSuccessNotification(successMessage);
                            deleteButton.setEnabled(true);
                        }, throwable -> {
                            onError(throwable);
                        });
                    }
                }
            });

        });

        return deleteButton;
    }

    private CssLayout getBodyContainer() {
        CssLayout bodyContainer = new CssLayout();
        bodyContainer.setStyleName("body-container");

        DCAWrapper selectedPathAsHeader = new DCAWrapper(Collections.singletonList(selectedPathLabel),
                "selected-path-header");

        DCAWrapper jsonAreaWrapper = new DCAWrapper(Collections.singletonList(jsonArea), "json-area-wrapper");
        DCAWrapper endpointWrapper = new DCAWrapper(Collections.singletonList(endpointAddressLabel), "endpoint-wrapper");
        DCAWrapper buttonWrapper = new DCAWrapper(Arrays.asList(getSaveButton(), getDeleteButton()), "btn-wrapper");

        bodyContainer.addComponent(selectedPathAsHeader);
        bodyContainer.addComponent(jsonAreaWrapper);
        bodyContainer.addComponent(endpointWrapper);
        bodyContainer.addComponent(buttonWrapper);

        return bodyContainer;
    }

    private void init() {
        this.setStyleName("service-widget-container");

        DCABreadCrumb breadCrumb = getLayoutContainer().getBreadCrumb();
        breadCrumb.addAction(targetService.getServiceIdentifier(), s -> {});

        CssLayout actionComponent = getActionComponent();
        CssLayout bodyContainer = getBodyContainer();

        this.addComponent(breadCrumb.getView());
        this.addComponent(actionComponent);
        this.addComponent(bodyContainer);
    }
}
