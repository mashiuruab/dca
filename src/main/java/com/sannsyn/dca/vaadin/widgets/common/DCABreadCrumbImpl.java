package com.sannsyn.dca.vaadin.widgets.common;

import com.sannsyn.dca.vaadin.ui.DCAUiHelper;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * A generic implementation of the DCABreadCrumb interface.
 * <p>
 * Created by jobaer on 10/5/16.
 */
public class DCABreadCrumbImpl implements DCABreadCrumb {
    private CssLayout root = new CssLayout();
    private LinkedHashMap<String, Consumer<String>> actions = new LinkedHashMap<>();

    public DCABreadCrumbImpl() {
        root.setStyleName("breadcrumb-wrapper");
    }

    @Override
    public Component getView() {
        return root;
    }

    @Override
    public void addAction(String key, Consumer<String> action) {
        actions.put(key, action);
        UI.getCurrent().access(() -> redraw());
    }

    private void redraw() {
        root.removeAllComponents();
        int numberOfActions = actions.size();
        int i = 0;
        for (Map.Entry<String, Consumer<String>> stringConsumerEntry : actions.entrySet()) {
            boolean isLast = numberOfActions - 1 == i;
            Component component = createNavigationComponent(stringConsumerEntry.getKey(), stringConsumerEntry.getValue(), isLast);
            root.addComponent(component);
            i++;
        }
    }

    private Component createNavigationComponent(String key, Consumer<String> action, boolean isLast) {
        CssLayout wrapper = new CssLayout();
        wrapper.setStyleName("breadcrumb-item-wrapper");

        CssLayout nav = new CssLayout();
        nav.addStyleName("breadcrumb-item");
        nav.addComponent(new Label(key, ContentMode.HTML));
        nav.addLayoutClickListener(event -> action.accept(key));
        if(isLast) {
            nav.addStyleName("active");
        }
        wrapper.addComponent(nav);


        if(!isLast) {
            CssLayout separator = DCAUiHelper.wrapWithCssLayout(new Label("&nbsp;/&nbsp;", ContentMode.HTML), "breadcrumb-separator");
            wrapper.addComponent(separator);
        }

        return wrapper;
    }

    @Override
    public void removeAction(String key) {
        actions.remove(key);
        redraw();
    }

    @Override
    public void navigateTo(String key) {
        if(actions.containsKey(key)) {
            Consumer<String> stringConsumer = actions.get(key);
            if(stringConsumer != null) {
                stringConsumer.accept(key);
            }
        }

    }
}
