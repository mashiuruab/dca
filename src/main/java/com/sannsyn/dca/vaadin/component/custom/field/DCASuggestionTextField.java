package com.sannsyn.dca.vaadin.component.custom.field;

import com.vaadin.data.Validator;
import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ShortcutListener;
import com.vaadin.server.Resource;
import com.vaadin.ui.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.vaadin.server.Sizeable.Unit.PERCENTAGE;
import static java.util.stream.Collectors.toList;


/**
 * Custom input field with functionality for providing suggestions.
 * <p/>
 * Created by jobaer on 4/27/16.
 */
public class DCASuggestionTextField extends CustomComponent {
    private DCATextField inputField;
    private CssLayout mainLayout = new CssLayout();
    private List<String> suggestions = new ArrayList<>();
    private List<String> selectionModel = new ArrayList<>();
    private VerticalLayout suggestionsLayout = new VerticalLayout();
    private CssLayout errorLayout = new CssLayout();
    private CssLayout inputWrapper;
    private int selectedIndex = -1;
    private int totalSuggestions = 0;

    public DCASuggestionTextField() {
        inputField = new DCATextField(false);
        init();
        setCompositionRoot(mainLayout);
    }

    public DCASuggestionTextField(Resource icon) {
        inputField = new DCATextField(icon, false);
        init();
        setCompositionRoot(mainLayout);
    }

    private void init() {
        initLayout();
        initListeners();
    }

    private void initLayout() {
        mainLayout.setWidth(100, PERCENTAGE);
        mainLayout.setStyleName("dca-suggestion-textfield-container");

        inputWrapper = new CssLayout();
        inputWrapper.setWidth(100, PERCENTAGE);
        inputWrapper.setStyleName("dca-suggestion-textfield-inputwrapper");
        inputWrapper.addComponent(inputField);
        mainLayout.addComponent(inputWrapper);

        inputField.setValidationVisible(false);
        errorLayout.addStyleName("suggestion-field-error-msg");
        errorLayout.setWidth(100, PERCENTAGE);
        errorLayout.setVisible(false);
        mainLayout.addComponent(errorLayout);

        suggestionsLayout.setVisible(false);
        suggestionsLayout.setStyleName("dca-suggestion-textfield-suggestion-container");
        populateSuggestions(suggestions);

        mainLayout.addComponent(suggestionsLayout);
    }

    private void populateSuggestions(List<String> suggestions) {
        for (String suggestion : suggestions) {
            CssLayout suggestionWrapper = new CssLayout();
            suggestionWrapper.setStyleName("dca-suggestion-textfield-suggestion-item");
            addClickListener(suggestionWrapper, suggestion);
            Label suggestionLabel = new Label(suggestion);
            suggestionWrapper.addComponent(suggestionLabel);
            suggestionsLayout.addComponent(suggestionWrapper);
        }
    }

    private void updateSelectionModel(String fragment) {
        List<String> filteredSuggestions = suggestions.stream().filter(
            s -> s.toLowerCase().contains(fragment.toLowerCase()))
            .collect(toList());
        selectionModel.clear();
        selectionModel.addAll(filteredSuggestions);
    }

    private void addClickListener(CssLayout suggestionWrapper, String suggestion) {
        suggestionWrapper.addLayoutClickListener(event -> {
            inputField.setValue(suggestion);
            suggestionsLayout.setVisible(false);
        });
    }

    public void addValidator(Validator validator) {
        inputField.addValidator(validator);
    }

    public void validate() throws Validator.InvalidValueException {
        try{
            inputField.validate();
        } catch (Validator.InvalidValueException ie) {
            String message = ie.getMessage();
            showErrorMessage(message);
        }
    }

    private void showErrorMessage(String message) {
        errorLayout.removeAllComponents();
        inputWrapper.addStyleName("invalid-input");
        errorLayout.addComponent(new Label(message));
        errorLayout.setVisible(true);
    }

    private void clearErrorMessages() {
        errorLayout.removeAllComponents();
        errorLayout.setVisible(false);
        inputWrapper.removeStyleName("invalid-input");
    }

    private void updateSelection() {
        for (Component component : suggestionsLayout) {
            deSelectComponent(component);
        }
        selectAtIndex();
    }

    private void selectAtIndex() {
        int total = suggestionsLayout.getComponentCount();
        if(selectedIndex < 0 || selectedIndex >= total) return;
        Component component = suggestionsLayout.getComponent(selectedIndex);
        selectComponent(component);
    }

    private void selectComponent(Component component) {
        String styleName = component.getStyleName();
        if(!styleName.contains("dca-suggestion-selection")) {
            component.addStyleName("dca-suggestion-selection");
        }
    }

    private void deSelectComponent(Component component) {
        String styleName = component.getStyleName();
        if(styleName.contains("dca-suggestion-selection")) {
            component.removeStyleName("dca-suggestion-selection");
        }
    }

    private void initListeners() {
        inputField.addFocusListener(event -> {
            String value = inputField.getValue();
            clearErrorMessages();
            handleTextChanged(value);
        });

        inputField.addBlurListener(event -> {
            final UI ui = getUI();
            ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
            scheduledExecutorService.schedule(
                (Runnable) () -> ui.access(() -> suggestionsLayout.setVisible(false)), 1, TimeUnit.SECONDS);
        });

        inputField.addShortcutListener(new ShortcutListener("Down Key", ShortcutAction.KeyCode.ARROW_DOWN, null) {
            @Override
            public void handleAction(Object sender, Object target) {
                int total = selectionModel.size();
                if(selectedIndex < total-1) {
                    selectedIndex++;
                    updateSelection();
                }
            }
        });

        inputField.addShortcutListener(new ShortcutListener("Up Key", ShortcutAction.KeyCode.ARROW_UP, null) {
            @Override
            public void handleAction(Object sender, Object target) {
                if(selectedIndex > 0) {
                    selectedIndex--;
                    updateSelection();
                }
            }
        });

        inputField.addShortcutListener(new ShortcutListener("Enter Key", ShortcutAction.KeyCode.ENTER, null) {
            @Override
            public void handleAction(Object sender, Object target) {
                updateValueFromSelection();
                selectedIndex = -1;
                suggestionsLayout.setVisible(false);
            }
        });

        inputField.addTextChangeListener(event -> {
            String text = event.getText();
            handleTextChanged(text);
        });
    }

    private void handleTextChanged(String text) {
        if(text == null) text = "";
        updateSelectionModel(text);
        updateSuggestionsUi();
    }

    private void updateSuggestionsUi() {
        suggestionsLayout.removeAllComponents();
        populateSuggestions(selectionModel);
        selectedIndex = -1;
        suggestionsLayout.setVisible(true);
    }


    private void updateValueFromSelection() {
        int total = selectionModel.size();
        if(selectedIndex >= 0 && selectedIndex < total) {
            String suggestion = selectionModel.get(selectedIndex);
            inputField.setValue(suggestion);
        }
    }

    public void addItems(List<String> items) {
        suggestions.addAll(items);
        Collections.sort(suggestions);
        totalSuggestions = totalSuggestions + items.size();
    }

    public void addItem(String item) {
        if(suggestions.contains(item)) return;

        suggestions.add(item);
        Collections.sort(suggestions);
        totalSuggestions++;
    }

    public String getValue() {
        return inputField.getValue();
    }

    @Override
    public void setStyleName(String style) {
        inputField.setStyleName(style);
    }

    @Override
    public void setStyleName(String style, boolean add) {
        inputField.setStyleName(style, add);
    }

    public void setInputPrompt(String s) {
        inputField.setInputPrompt(s);
    }
}
