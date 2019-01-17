package com.sannsyn.dca.vaadin.customertargeting;

import com.google.gson.JsonElement;
import com.sannsyn.dca.metadata.DCAItem;
import com.sannsyn.dca.model.config.DCAWidget;
import com.sannsyn.dca.model.user.DCAUser;
import com.sannsyn.dca.service.DCACustomerTargetingService;
import com.sannsyn.dca.service.Status;
import com.sannsyn.dca.vaadin.component.custom.field.DCAEmailInput;
import com.sannsyn.dca.vaadin.component.custom.field.DCATextField;
import com.sannsyn.dca.vaadin.helper.DCAFileDownloadWithSupplier;
import com.sannsyn.dca.vaadin.helper.OnDemandFileDownloader;
import com.sannsyn.dca.vaadin.widgets.operations.controller.component.DCAPopupErrorComponent;
import com.sannsyn.dca.vaadin.widgets.operations.controller.component.DCAPopupMessageComponent;
import com.vaadin.data.Property;
import com.vaadin.event.FieldEvents;
import com.vaadin.ui.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static com.sannsyn.dca.vaadin.ui.DCAUiHelper.wrapWithCssLayout;
import static com.vaadin.server.Sizeable.Unit.PERCENTAGE;
import static com.vaadin.server.Sizeable.Unit.PIXELS;
import static java.util.stream.Collectors.toList;

/**
 * A custom layout
 * <p>
 * Created by jobaer on 8/9/16.
 */
class DCACustomerTargetingActionComponent extends CustomComponent {
    private static final Logger logger = LoggerFactory.getLogger(DCACustomerTargetingActionComponent.class);
    private final UI mainUi;
    private final Supplier<List<DCAItem>> selectedItems;
    private final DCAWidget config;
    private DCACustomerTargetingService service;
    private Layout layout;
    private CssLayout errorLayout = new CssLayout();
    private DCATextField numberField;
    private Button downloadButton;
    private Button sendMailButton = new Button("FIRE");
    private CssLayout controlLayout = new CssLayout();
    private OptionGroup formatOption = new OptionGroup("Format: ");
    private OnDemandFileDownloader.MutableStreamSource streamSource = new OnDemandFileDownloader.MutableStreamSource();

    DCACustomerTargetingActionComponent(DCAWidget config, Supplier<List<DCAItem>> selectedItems, UI ui, DCAUser loggedInUser) {
        this.mainUi = ui;
        this.selectedItems = selectedItems;
        this.config = config;
        service = new DCACustomerTargetingService(loggedInUser);
        layout = createEmailActionUi();
        errorLayout.addStyleName("number-of-customer-error");
        errorLayout.addComponent(new Label("* Please provide an integer"));
        errorLayout.setVisible(false);
        setCompositionRoot(layout);
    }

    private void showError() {
        errorLayout.setVisible(true);
    }

    private void hideError() {
        errorLayout.setVisible(false);
    }

    private Layout createEmailActionUi() {
        CssLayout cssLayout = new CssLayout();
        cssLayout.setWidth(100, PERCENTAGE);
        cssLayout.addStyleName("customer-targeting-email-action");

        CssLayout firstInputLine = new CssLayout();
        firstInputLine.addStyleName("first-input-line");
        firstInputLine.setWidth(100, PERCENTAGE);

        Component numberFieldComponent = createNumberField();
        CssLayout numCustomersWrapped = wrapWithCssLayout(numberFieldComponent, "number-of-customers-wrapper");
        numCustomersWrapped.setWidth(40, PERCENTAGE);
        firstInputLine.addComponent(numCustomersWrapped);

        OptionGroup deliveryMethod = new OptionGroup("Delivery method: ");
        deliveryMethod.addStyleName("horizontal");
        deliveryMethod.addItems("download", "email");
        deliveryMethod.select("download");
        CssLayout deliveryMethodLayout = wrapWithCssLayout(deliveryMethod, "delivery-method");
        CssLayout deliveryWrapped = wrapWithCssLayout(deliveryMethodLayout, "delivery-method-wrapper");
        deliveryWrapped.setWidth(31, PERCENTAGE);
        firstInputLine.addComponent(deliveryWrapped);

        Component formatField = createFormatField();
        CssLayout formatWrapped = wrapWithCssLayout(formatField, "format-wrapper");
        formatWrapped.setWidth(29, PERCENTAGE);
        firstInputLine.addComponent(formatWrapped);

        cssLayout.addComponent(firstInputLine);

        CssLayout labelWrapper = new CssLayout();
        labelWrapper.addStyleName("email-input-label-wrapper");
        labelWrapper.setWidth(100, PERCENTAGE);

        Label sendListLabel = new Label("Send list of customers to email address: ");
        sendListLabel.addStyleName("send-email-label");
        labelWrapper.addComponent(sendListLabel);
        sendListLabel.setVisible(false);

        cssLayout.addComponent(labelWrapper);

        CssLayout secondInputLine = new CssLayout();
        secondInputLine.addStyleName("second-input-line");
        secondInputLine.setWidth(100, PERCENTAGE);

        controlLayout.setWidth(100, PERCENTAGE);
        controlLayout.setStyleName("controls");

        DCAEmailInput emailInput = new DCAEmailInput();
        CssLayout emailInputLayout = wrapWithCssLayout(emailInput, "targeting-email-input-wrapper");
        emailInputLayout.setWidth(81, PERCENTAGE);
        controlLayout.addComponent(emailInputLayout);
        emailInput.setVisible(false);

        sendMailButton.setWidth(18, PERCENTAGE);
        sendMailButton.addStyleName("btn-primary-style");
        sendMailButton.addStyleName("hidden");

        downloadButton = createDownloadButton();
        controlLayout.addComponent(downloadButton);

        sendMailButton.addClickListener(event -> {
            final StringBuilder message = new StringBuilder();
            sendMailButton.setEnabled(false);
            List<String> emails = new ArrayList<>(emailInput.getValue());
            boolean invalid = false;
            if (emails.isEmpty()) {
                message.append("Email address field is not valid.\n");
                invalid = true;
            }

            int numberOfCustomerIds = getNumberOfCustomerIds();
            if (numberOfCustomerIds < 0) {
                invalid = true;
                message.append("Number of customers field is not valid.\n");
            }

            List<String> ids = selectedItems.get().stream().map(DCAItem::getId).collect(toList());
            if (ids.isEmpty()) {
                invalid = true;
                message.append("There are no selected items.\n");
            }

            if (invalid) {
                showErrorMessage(message.toString());
                sendMailButton.setEnabled(true);
                return;
            }

            String format = "json";
            if (formatOption.getValue() != null && "csv".equals(formatOption.getValue().toString())) {
                format = "csv";
            }
            String serviceName = DCACustomerTargetingWidget.getServiceName();
            Optional<String> recommenderNameOption = getRecommenderNameFromConfig(config, serviceName);
            if(recommenderNameOption.isPresent()) {
                String recommenderName = recommenderNameOption.get();
                service.sendCustomerList(recommenderName, numberOfCustomerIds, emails, ids, format)
                    .subscribe(res -> handleStatus(sendMailButton, res));
            }

        });
        controlLayout.addComponent(sendMailButton);

        deliveryMethod.addValueChangeListener((Property.ValueChangeListener) event -> {
            Object value = event.getProperty().getValue();
            if ("download".equals(value)) {
                downloadButton.removeStyleName("hidden");
                sendMailButton.addStyleName("hidden");
                emailInput.setVisible(false);
                sendListLabel.setVisible(false);
            } else if ("email".equals(value)) {
                sendMailButton.removeStyleName("hidden");
                downloadButton.addStyleName("hidden");
                emailInput.setVisible(true);
                sendListLabel.setVisible(true);
            }
        });

        cssLayout.addComponent(controlLayout);

        return cssLayout;
    }

    private Button createDownloadButton() {
        Button downloadButton = styleDownloadButton();

        DCAFileDownloadWithSupplier extender = new DCAFileDownloadWithSupplier(streamSource, this::getTargetCustomers);
        extender.extend(downloadButton);

        return downloadButton;
    }

    private Optional<String> getRecommenderNameFromConfig(DCAWidget config, String serviceName) {
        if (config.getJsonConfig() != null) {
            if (config.getJsonConfig().has("recommenderNames")) {
                JsonElement recommenderNames = config.getJsonConfig().get("recommenderNames");
                if (recommenderNames.getAsJsonObject().has(serviceName)) {
                    String name = recommenderNames.getAsJsonObject().get(serviceName).getAsString();
                    return Optional.of(name);
                }
            }
        }

        return Optional.empty();
    }

    private String getTargetCustomers() {
        List<DCAItem> items = selectedItems.get();
        List<String> bookIds = items.stream().map(DCAItem::getId).collect(toList());
        int numberOfCustomerIds = getNumberOfCustomerIds();
        String format = formatOption.getValue().toString();
        String serviceName = DCACustomerTargetingWidget.getServiceName();
        Optional<String> recommenderNameOption = getRecommenderNameFromConfig(config, serviceName);

        if (recommenderNameOption.isPresent()) {
            String recommenderName = recommenderNameOption.get();
            Observable<String> customerIdResponseString = service.getCustomerIdResponseString(recommenderName, numberOfCustomerIds, bookIds, format);
            return customerIdResponseString.toBlocking().single();
        } else {
            String errorMsg = "No recommenderName is configured for " + serviceName;
            showErrorMessage(errorMsg);
            return errorMsg;
        }
    }

    private Button styleDownloadButton() {
        Button downloadButton = new Button("DOWNLOAD");
        downloadButton.setWidth(18, PERCENTAGE);
        downloadButton.addStyleName("btn-primary-style");
        if (!sendMailButton.getStyleName().contains("hidden")) {
            downloadButton.addStyleName("hidden");
        }
        return downloadButton;
    }

    private Component createFormatField() {
        formatOption.addStyleName("horizontal");
        formatOption.addItems("csv", "json");
        formatOption.select("csv");

        String fileName = "target_customers";
        streamSource.setFileName(fileName + ".csv");
        formatOption.addValueChangeListener(event -> {
            Object value = event.getProperty().getValue();
            if (value == null) return;
            if ("csv".equals(value.toString())) {
                streamSource.setFileName(fileName + ".csv");
            } else {
                streamSource.setFileName(fileName + ".json");
            }
        });

        return wrapWithCssLayout(formatOption, "format");
    }

    private Component createNumberField() {
        CssLayout layout = new CssLayout();
        layout.addStyleName("number-of-customers-field");

        Label label = new Label("Number of customers for targeting: ");
        label.addStyleName("number-field-label");
        CssLayout labelWrapper = wrapWithCssLayout(label, "number-field-label-wrapper");
        layout.addComponent(labelWrapper);

        numberField = new DCATextField(false);
        CssLayout numberWrapper = wrapWithCssLayout(numberField, "number-field-wrapper");
        numberWrapper.addComponent(errorLayout);
        numberWrapper.setWidth(90, PIXELS);
        numberField.addFocusListener((FieldEvents.FocusListener) event -> hideError());
        layout.addComponent(numberWrapper);
        return layout;
    }

    private void handleStatus(Button button, Status res) {
        if (Status.SUCCESS == res) {
            logger.info("Mail sent successfully!");
            mainUi.access(() -> {
                showSuccessMessage("Mail sent successfully!");
                button.setEnabled(true);
            });
        } else {
            logger.warn("Error sending email");
            mainUi.access(() -> {
                showErrorMessage("Error while sending email.");
                button.setEnabled(true);
            });
        }
    }

    private int getNumberOfCustomerIds() {
        int numberOfCustomerIds = 10; // default
        if (numberField == null || StringUtils.isBlank(numberField.getValue())) {
            return numberOfCustomerIds;
        }

        String value = numberField.getValue();
        try {
            numberOfCustomerIds = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            showError();
        }
        return numberOfCustomerIds;
    }

    private void showSuccessMessage(String message) {
        DCAPopupMessageComponent successMessageComponent = new DCAPopupMessageComponent("Success:", message, layout);
        layout.addComponent(successMessageComponent);
    }

    private void showErrorMessage(String message) {
        DCAPopupErrorComponent successMessageComponent = new DCAPopupErrorComponent("Failure:", message, layout);
        layout.addComponent(successMessageComponent);
    }
}
