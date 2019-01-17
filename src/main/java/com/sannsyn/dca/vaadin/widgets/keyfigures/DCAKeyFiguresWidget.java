package com.sannsyn.dca.vaadin.widgets.keyfigures;

import com.sannsyn.dca.model.config.DCAWidget;
import com.sannsyn.dca.model.keyfigures.DCAKeyFiguresChangeStatus;
import com.sannsyn.dca.service.analytics.DCAKeyFiguresService;
import com.sannsyn.dca.vaadin.servlet.DCAUtils;
import com.vaadin.ui.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;

import static com.sannsyn.dca.vaadin.ui.DCAUiHelper.addSeparator;
import static com.sannsyn.dca.vaadin.ui.DCAUiHelper.wrapWithCssLayout;
import static com.vaadin.server.Sizeable.Unit.PERCENTAGE;

/**
 * The KeyFigures widget
 * <p>
 * Created by jobaer on 1/17/17.
 */
public class DCAKeyFiguresWidget extends CustomComponent {
    private static final Logger logger = LoggerFactory.getLogger(DCAKeyFiguresWidget.class);
    private final DCAWidget widgetConfig;
    private final UI currentUi;
    private DCAKeyFiguresService service = new DCAKeyFiguresService();

    private Label dateLabel = new Label("");

    private Label conversionLabel = new Label("");
    private Label conversionMaxLabel = new Label("");
    private CssLayout conversionIcon = new CssLayout();

    private Label reminderFrequencyLabel = new Label("");
    private Label reminderFrequencyAvgLabel = new Label("");
    private CssLayout reminderFrequencyIcon = new CssLayout();

    private Label turnoverLabel = new Label("");
    private Label turnoverMaxLabel = new Label("");
    private CssLayout turnoverIcon = new CssLayout();

    private CssLayout rootLayout = new CssLayout();

    public DCAKeyFiguresWidget(DCAWidget widgetConfig, UI current) {
        this.currentUi = current;
        this.widgetConfig = widgetConfig;
        Component root = buildRootComponent();
        setCompositionRoot(root);
    }

    private Component buildRootComponent() {
        CssLayout wrapper = new CssLayout();
        wrapper.setWidth(100, PERCENTAGE);
        wrapper.addStyleName("key-figures-widget");

        Component title = createTitleComponent();
        wrapper.addComponent(title);

        rootLayout.setWidth(100, PERCENTAGE);
        Component content = createWidgetContent();
        rootLayout.addComponent(content);
        wrapper.addComponent(rootLayout);

        return wrapper;
    }

    private Component createWidgetContent() {
        CssLayout placeHolder = new CssLayout();
        updateContentSection();
        return placeHolder;
    }

    private void updateContentSection() {
        DCAUtils.getTargetService().subscribe(targetService -> {
            String serviceId = targetService.getServiceIdentifier();
            List<String> availableServices = widgetConfig.getAvailableServices();
            if (availableServices != null && availableServices.contains(serviceId)) {
                makeContentSectionForData();
            } else {
                makeContentSectionForError();
            }
        });
    }

    private void makeContentSectionForError() {
        CssLayout errorContent = createErrorContent();
        currentUi.access(() -> {
            rootLayout.removeAllComponents();
            rootLayout.addComponent(errorContent);
        });
    }

    private void makeContentSectionForData() {
        CssLayout content = createContentSection();
        currentUi.access(() -> {
            rootLayout.removeAllComponents();
            rootLayout.addComponent(content);
        });

        updateMainNumbers();
        updateMaxNumbers();
        updateIcons();
    }

    private void updateIcons() {
        service.getComparision().subscribe(
            result ->
                currentUi.access(() -> {
                    updateComponentIcon(conversionIcon, result.getConversionStatus());
                    updateComponentIcon(turnoverIcon, result.getTurnoverStatus());
                }),
            e -> {
                logger.error("Error occurred while fetching compared data.", e);
                makeContentSectionForError();
            });
    }

    private void updateComponentIcon(Component component, DCAKeyFiguresChangeStatus status) {
        String className = getIconName(status);
        component.addStyleName(className);
    }

    private void updateMaxNumbers() {
        service.getMaxNumbers().subscribe(
            result -> currentUi.access(() -> {
                updateFloatLabel(conversionMaxLabel, result.getConversion(), "%.1f%%");
                updateFloatLabel(reminderFrequencyAvgLabel, result.getReminderFrequency(), "%.2f");
                updateIntLabel(turnoverMaxLabel, result.getTurnover());
            }), e -> logger.error("Error occurred while fetching max numbers.", e));
    }

    private void updateMainNumbers() {
        service.getLatestResult().subscribe(
            result -> currentUi.access(() -> {
                updateDateLabel(result.getDate());
                updateFloatLabel(conversionLabel, result.getConversion(), "%.1f%%");
                updateFloatLabel(reminderFrequencyLabel, result.getReminderFrequency(), "%.2f");
                updateIntLabel(turnoverLabel, result.getTurnover());
            }), e -> logger.error("Error occurred while fetching key numbers", e));
    }

    private void updateDateLabel(LocalDate date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG);
        String dateString = date.format(formatter);
        dateLabel.setValue(dateString);
    }

    private void updateIntLabel(Label label, int content) {
        String turnover = String.format("%d", content);
        label.setValue(turnover);
    }

    private void updateFloatLabel(Label label, float content, String formatter) {
        String conversion = String.format(formatter, content);
        label.setValue(conversion);
    }

    private CssLayout createContentSection() {
        CssLayout widgetContent = new CssLayout();
        widgetContent.addStyleName("widget-content-wrapper");

        CssLayout conversion = createSection(
            "Conversion", conversionLabel, conversionMaxLabel, conversionIcon, "Highest last 30 days");
        conversion.setWidth(33, PERCENTAGE);
        widgetContent.addComponent(conversion);

        CssLayout converage = createSection(
            "Reminder frequency", reminderFrequencyLabel, reminderFrequencyAvgLabel, reminderFrequencyIcon, "Average last 30 days");
        converage.setWidth(33, PERCENTAGE);
        widgetContent.addComponent(converage);

        CssLayout turnover = createSection(
            "Turnover", turnoverLabel, turnoverMaxLabel, turnoverIcon, "Highest last 30 days");
        turnover.setWidth(34, PERCENTAGE);
        widgetContent.addComponent(turnover);

        return widgetContent;
    }

    private CssLayout createErrorContent() {
        CssLayout widgetContent = new CssLayout();
        widgetContent.addStyleName("widget-content-wrapper");

        CssLayout errorLayout = new CssLayout();
        errorLayout.addStyleName("error-layout");
        Label label = new Label("No data to display");
        label.setWidthUndefined();
        errorLayout.addComponent(label);
        widgetContent.addComponent(errorLayout);

        return widgetContent;
    }

    private String getIconName(DCAKeyFiguresChangeStatus status) {
        switch (status) {
            case INC:
                return "icon-wrapper-up";
            case DEC:
                return "icon-wrapper-down";
            case SAME:
                return "icon-wrapper-straight";
            default:
                return "";
        }
    }

    private CssLayout createSection(String title, Label currentLabel, Label last30ContentLabel, Component icon, String last30LabelText) {
        CssLayout firstContent = new CssLayout();
        CssLayout numberLabel = wrapWithCssLayout(currentLabel, "number-label");
        CssLayout numberWrapper = wrapWithCssLayout(numberLabel, "number-wrapper", 100);
        firstContent.addComponent(numberWrapper);

        Label last30Label = new Label(last30LabelText);
        CssLayout last30LabelSection = wrapWithCssLayout(last30Label, "last-number-label-section");
        CssLayout lastNumberContentSection = wrapWithCssLayout(last30ContentLabel, "last-number-content-section");

        CssLayout lastNumberLayout = new CssLayout();
        lastNumberLayout.addStyleName("last-number-label");
        lastNumberLayout.addComponent(last30LabelSection);
        lastNumberLayout.addComponent(lastNumberContentSection);

        CssLayout last30Wrapper = wrapWithCssLayout(lastNumberLayout, "last-number-wrapper", 100);
        firstContent.addComponent(last30Wrapper);

        icon.addStyleName("");
        CssLayout iconWrapper = wrapWithCssLayout(icon, "icon-container");
        firstContent.addComponent(iconWrapper);

        Label titleLabel = new Label(title);
        CssLayout titlelabel = wrapWithCssLayout(titleLabel, "title-label");
        CssLayout titleWrapper = wrapWithCssLayout(titlelabel, "title-wrapper", 100);
        firstContent.addComponent(titleWrapper);

        return firstContent;
    }

    private Component createTitleComponent() {
        CssLayout layout = new CssLayout();
        layout.setWidth(100, PERCENTAGE);
        layout.addStyleName("widget-title-wrapper");

        Label title = new Label("Key Numbers");
        title.setWidthUndefined();
        title.addStyleName("widget-title");

        dateLabel.setWidthUndefined();
        dateLabel.addStyleName("widget-subtitle");

        layout.addComponent(title);
        layout.addComponent(dateLabel);

        addSeparator(layout);

        return layout;
    }
}
