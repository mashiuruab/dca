package com.sannsyn.dca.vaadin.widgets.analytics;

import com.google.gson.JsonObject;
import com.sannsyn.dca.util.DCADateUtils;
import com.sannsyn.dca.vaadin.component.custom.field.DCACollapsibleCheckbox;
import com.sannsyn.dca.vaadin.component.custom.field.DCACollapsibleDateRangeField;
import com.sannsyn.dca.vaadin.component.custom.field.DCACollapsibleTextField;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.CustomComponent;

import java.time.LocalDate;
import java.util.Date;
import java.util.Optional;
import java.util.function.Consumer;

import static com.sannsyn.dca.vaadin.ui.DCAUiHelper.wrapWithCssLayout;
import static com.sannsyn.dca.vaadin.widgets.analytics.DCAAnalyticsDataParser.*;

/**
 * Dynamic fields for SalesByRecommendation type analytics.
 * <p>
 * Created by jobaer on 5/10/17.
 */
public class DCASalesByRecommendationFields extends CustomComponent implements DCAAnalyticsWidgetDynamicFields {
    private JsonObject item;
    private DCACollapsibleDateRangeField dateRangeField;
    private DCACollapsibleCheckbox logInput;
    private DCACollapsibleTextField salesDesc;
    private DCACollapsibleTextField recsDesc;

    public DCASalesByRecommendationFields() {
        CssLayout salesByRecFields = new CssLayout();
        salesByRecFields.setWidth(100, Unit.PERCENTAGE);
        salesByRecFields.addStyleName("sales-by-rec-fields");

        createFormFields(salesByRecFields);
        setCompositionRoot(salesByRecFields);
    }

    private void createFormFields(CssLayout salesByRecFields) {
        dateRangeField = new DCACollapsibleDateRangeField("Date range");
        CssLayout dateWrapper = wrapWithCssLayout(dateRangeField, "analytics-form-field", 100);
        salesByRecFields.addComponent(dateWrapper);

        logInput = new DCACollapsibleCheckbox("Logarithmic scale: ");
        CssLayout logWrapper = wrapWithCssLayout(logInput, "analytics-form-field", 100);
        salesByRecFields.addComponent(logWrapper);

        salesDesc = new DCACollapsibleTextField("Descriptive text sales: ");
        CssLayout salesWrapper = wrapWithCssLayout(salesDesc, "analytics-form-field", 100);
        salesByRecFields.addComponent(salesWrapper);

        recsDesc = new DCACollapsibleTextField("Descriptive text recommendations: ");
        CssLayout recsWrapper = wrapWithCssLayout(recsDesc, "analytics-form-field", 100);
        salesByRecFields.addComponent(recsWrapper);
    }

    @Override
    public void setData(JsonObject data) {
        this.item = data;
        populateValues();
    }

    private void populateValues() {
        setDateValueFromBackend(item, dateRangeField);

        Boolean booleanProperty = getBooleanProperty(item, IS_LOGARITHMIC);
        logInput.setValue(booleanProperty);

        String salesVal = getPropertySafe(item, SALES_TEXT);
        salesDesc.setValue(salesVal);

        String recsVal = getPropertySafe(item, RECS_TEXT);
        recsDesc.setValue(recsVal);
    }

    @Override
    public JsonObject getData() {
        String salesText = salesDesc.getValue();
        String recsText = recsDesc.getValue();
        Boolean isLogarithmic = logInput.getValue();

        Date fromDate = dateRangeField.getFromDate();
        Date toDate = dateRangeField.getToDate();

        return createFormattedJson(fromDate, toDate, isLogarithmic, salesText, recsText);
    }

    private void setDateValueFromBackend(JsonObject item, DCACollapsibleDateRangeField dateRangeField) {
        getSafelyAndExecute(item, FROM_DATE, dateRangeField::setFromDate);
        getSafelyAndExecute(item, TO_DATE, dateRangeField::setToDate);
    }

    private void getSafelyAndExecute(JsonObject item, String propertyName, Consumer<Date> setter) {
        Optional<LocalDate> toDateOption = getDateProperty(item, propertyName);
        toDateOption.ifPresent(toDate -> {
            Date dateValue = DCADateUtils.toDate(toDate);
            setter.accept(dateValue);
        });
    }

    private JsonObject createFormattedJson(Date fromDate, Date toDate,
                                           Boolean isLogarithmic,
                                           String salesText, String recsText) {
        return DCAAnalyticsDataParser.createDbObject("", "", "", salesText, recsText, isLogarithmic, fromDate, toDate);
    }
}
