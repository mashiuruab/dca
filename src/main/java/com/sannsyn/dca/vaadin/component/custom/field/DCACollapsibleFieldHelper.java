package com.sannsyn.dca.vaadin.component.custom.field;

import com.sannsyn.dca.vaadin.component.custom.icon.DCAIcon;
import com.sannsyn.dca.vaadin.component.custom.icon.DCASimpleIcon;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Label;

import java.util.function.Consumer;

import static com.sannsyn.dca.vaadin.component.custom.navigation.item.DCALeftPanelItem.DOWN_ARROW_ICON;
import static com.sannsyn.dca.vaadin.component.custom.navigation.item.DCALeftPanelItem.UP_ARROW_ICON;
import static com.sannsyn.dca.vaadin.ui.DCAUiHelper.createSpacer;

/**
 * An collapsible component.
 * <p>
 * Created by jobaer on 4/19/17.
 */
class DCACollapsibleFieldHelper extends CustomComponent {
    private Consumer<CssLayout> painter = l -> {
    };
    private String label = "";
    private Label valueLabel = new Label("");
    private Boolean expanded = true;

    private CssLayout root = new CssLayout();
    private DCASimpleIcon collapsedIcon = new DCASimpleIcon(DOWN_ARROW_ICON, "field-collapsed-icon");
    private DCASimpleIcon expandedIcon = new DCASimpleIcon(UP_ARROW_ICON, "field-expanded-icon");
    private CssLayout iconWrapper = new CssLayout();
    private CssLayout controlWrapper = new CssLayout();
    private CssLayout labelLayout = new CssLayout();

    DCACollapsibleFieldHelper(String label,
                              Consumer<CssLayout> painter) {
        this.label = label;
        this.painter = painter;
        root.addStyleName("collapsible-input-root");
        root.setWidth(100, Unit.PERCENTAGE);
        iconWrapper.setWidth(2, Unit.PERCENTAGE);
        controlWrapper.setWidth(100, Unit.PERCENTAGE);
        controlWrapper.addStyleName("collapsible-input-wrapper");

        labelLayout.addStyleName("collapsible-input-label");
        labelLayout.setWidth(48, Unit.PERCENTAGE);

        paint();
        setCompositionRoot(root);
    }

    private void paint() {
        root.removeAllComponents();
        labelLayout.removeAllComponents();

        CssLayout topWrapper = new CssLayout();
        topWrapper.setWidth(100, Unit.PERCENTAGE);
        topWrapper.addLayoutClickListener(event -> changeExpandCollapseState());

        iconWrapper.addComponent(expandedIcon);
        topWrapper.addComponent(iconWrapper);

        CssLayout valueLayout = new CssLayout();
        valueLayout.setWidth(50, Unit.PERCENTAGE);
        valueLabel.setWidthUndefined();
        valueLabel.addStyleName("pull-right");
        valueLayout.addComponent(valueLabel);

        Label label = new Label(this.label);
        labelLayout.addComponent(label);

        topWrapper.addComponent(labelLayout);
        topWrapper.addComponent(valueLayout);
        root.addComponent(topWrapper);

        paintExpandedPart();
    }

    private void changeExpandCollapseState() {
        if (expanded) {
            expanded = false;
            collapse();
        } else {
            expanded = true;
            expand();
        }
    }

    private void expand() {
        iconWrapper.removeAllComponents();
        iconWrapper.addComponent(expandedIcon);
        labelLayout.removeStyleName("collapsed");
        paintExpandedPart();
    }

    private void collapse() {
        iconWrapper.removeAllComponents();
        iconWrapper.addComponent(collapsedIcon);
        root.removeComponent(controlWrapper);
        labelLayout.addStyleName("collapsed");
    }

    private void paintExpandedPart() {
        controlWrapper.removeAllComponents();
        Label spacer = createSpacer(2);
        CssLayout controls = new CssLayout();
        controls.setWidth(98, Unit.PERCENTAGE);
        painter.accept(controls);

        controlWrapper.addComponent(spacer);
        controlWrapper.addComponent(controls);
        root.addComponent(controlWrapper);
    }

    /**
     * Set the value shown at the right side.
     *
     * @param value the string value to be set
     */
    void setValueLabel(String value) {
        valueLabel.setValue(value);
    }
}
