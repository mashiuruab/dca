package com.sannsyn.dca.vaadin.component.custom.field;

import com.sannsyn.dca.vaadin.helper.OnEnterKeyHandler;
import com.vaadin.data.validator.EmailValidator;
import com.vaadin.event.FieldEvents;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.*;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

import static com.sannsyn.dca.vaadin.ui.DCAUiHelper.wrapWithCssLayout;
import static com.vaadin.server.Sizeable.Unit.PERCENTAGE;

/**
 * A custom-styled field for email address input.
 * <p>
 * Created by jobaer on 8/22/16.
 */
public class DCAEmailInput extends CustomComponent {
    private Set<String> model = new LinkedHashSet<>();
    private CssLayout currentNamesWrapper = new CssLayout();
    private CssLayout errorLayout = new CssLayout();
    private TextField input = new TextField();
    private EmailValidator validator = new EmailValidator("");

    public DCAEmailInput() {
        Layout layout = createEmailInputLayout();
        layout.setWidth(100, PERCENTAGE);
        errorLayout.setWidth(100, PERCENTAGE);
        errorLayout.addStyleName("email-input-error-layout");
        errorLayout.addComponent(new Label("* Email address is not valid"));
        errorLayout.setVisible(false);
        setCompositionRoot(layout);
        input.setInputPrompt("Enter email address");
    }

    /**
     * Returns the value
     *
     * @return A collection of email addresses
     */
    public Collection<String> getValue() {
        return Collections.unmodifiableCollection(model);
    }

    /**
     * @param values List of email addresses to be set
     */
    public void setValue(List<String> values) {
        values.forEach(item -> addToModel(item, false, false));
        redraw(false);
    }

    public void setEnabled(boolean enabled) {
        input.setEnabled(enabled);
        //todo and rest of the styles
    }

    private void addToModel(String value, boolean shouldRedraw, boolean getFocus) {
        if (StringUtils.isNotBlank(value)) {
            if(validator.isValid(value)) {
                clearError();
                model.add(value);
                input.clear();
            } else {
                showError();
            }
        }
        if (shouldRedraw) {
            redraw(getFocus);
        }
    }

    private void removeFromModel(String value) {
        model.remove(value);
        redraw(false);
    }

    private void redraw(boolean getFocus) {
        currentNamesWrapper.removeAllComponents();
        model.forEach(this::showNewItem);
        currentNamesWrapper.addComponent(input);
        if (getFocus) {
            input.focus();
        }
    }

    private void showError() {
        errorLayout.setVisible(true);
    }

    private void clearError() {
        errorLayout.setVisible(false);
    }

    private Layout createEmailInputLayout() {
        Layout cssLayout = new CssLayout();
        cssLayout.addStyleName("experimental");

        currentNamesWrapper.addStyleName("email-field-wrapper");
        input.addStyleName("email-field");
        OnEnterKeyHandler enterHandler = new OnEnterKeyHandler() {
            @Override
            public void onEnterKeyPressed() {
                addValueToModel();
            }
        };
        enterHandler.installOn(input);

        input.addBlurListener((FieldEvents.BlurListener) event -> addValueToModel());

        currentNamesWrapper.addComponent(input);

        currentNamesWrapper.addLayoutClickListener(event -> {
            Component childComponent = event.getClickedComponent();
            if(childComponent == null) {
                input.focus();
            } else {
                String styleName = "" + childComponent.getStyleName();
                if(styleName.contains("email-remove")) {
                    String id = childComponent.getId();
                    removeFromModel(id);
                } else {
                    input.focus();
                }
            }
        });


        cssLayout.addComponent(currentNamesWrapper);
        cssLayout.addComponent(errorLayout);
        return cssLayout;
    }

    private void addValueToModel() {
        String value = input.getValue();
        addToModel(value, true, false);
    }

    private void showNewItem(String content) {
        CssLayout cssLayout = createEmailComponent(content);
        currentNamesWrapper.addComponent(cssLayout);
    }

    private CssLayout createEmailComponent(final String content) {
        Label firstEmail = new Label(content);
        CssLayout labelWrapper = wrapWithCssLayout(firstEmail, "email-label");
        labelWrapper.setIcon(FontAwesome.USER);
        CssLayout wrapper = wrapWithCssLayout(labelWrapper, "email-component-wrapper");

        CssLayout removeLayout = new CssLayout();
        removeLayout.addStyleName("email-remove");
        removeLayout.setId(content);

        removeLayout.setIcon(FontAwesome.TIMES_CIRCLE);
        CssLayout layout = wrapWithCssLayout(removeLayout, "email-remove-wrapper");

        wrapper.addComponent(layout);
        return wrapWithCssLayout(wrapper, "email-component");
    }
}
