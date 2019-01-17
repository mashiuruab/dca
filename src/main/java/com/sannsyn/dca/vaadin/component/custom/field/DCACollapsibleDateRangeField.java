package com.sannsyn.dca.vaadin.component.custom.field;

import com.sannsyn.dca.vaadin.ui.DCAUiHelper;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.DateField;
import com.vaadin.ui.Label;

import java.util.Date;

import static com.sannsyn.dca.util.DCADateUtils.makeDateString;

/**
 * A custom date range field that can be collapsed. When collapsed it's value will be shown at the right side.
 * <p>
 * <p>
 * Created by jobaer on 5/08/17.
 */
public class DCACollapsibleDateRangeField extends CustomComponent {
    private Label fromLabel = new Label("From: ");
    private Label toLabel = new Label("To: ");
    private DateField fromDateField = new DateField();
    private DateField toDateField = new DateField();

    private DCACollapsibleFieldHelper collapsedText;

    public DCACollapsibleDateRangeField(String label) {
        fromLabel.setWidth(8, Unit.PERCENTAGE);
        fromDateField.setWidth(90, Unit.PERCENTAGE);
        toLabel.setWidth(5, Unit.PERCENTAGE);
        toDateField.setWidth(95, Unit.PERCENTAGE);

        collapsedText = new DCACollapsibleFieldHelper(label, layout -> {
            CssLayout fromWrapper = new CssLayout();
            fromWrapper.addStyleName("collapsible-date-from-wrapper");
            fromWrapper.setWidth(50, Unit.PERCENTAGE);
            fromWrapper.addComponent(fromLabel);
            fromWrapper.addComponent(fromDateField);
            layout.addComponent(fromWrapper);

            CssLayout toWrapper = new CssLayout();
            toWrapper.addStyleName("collapsible-date-to-wrapper");
            toWrapper.setWidth(50, Unit.PERCENTAGE);
            toWrapper.addComponent(toLabel);
            toWrapper.addComponent(toDateField);
            layout.addComponent(toWrapper);

        });

        fromDateField.addValueChangeListener(event -> updateValue());
        toDateField.addValueChangeListener(event -> updateValue());

        setCompositionRoot(collapsedText);
    }

    private void updateValue() {
        String displayValue = getDisplayValue();
        collapsedText.setValueLabel(displayValue);
    }

    private String getDisplayValue() {
        Date fromDate = fromDateField.getValue();
        Date toDte = toDateField.getValue();

        return makeDateString(fromDate) + "  to  " + makeDateString(toDte);
    }

    public void setFromDate(Date date) {
        fromDateField.setValue(date);
    }

    public void setToDate(Date date) {
        toDateField.setValue(date);
    }

    public Date getFromDate() {
        return fromDateField.getValue();
    }

    public Date getToDate() {
        return toDateField.getValue();
    }
}
