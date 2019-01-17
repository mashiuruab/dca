package com.sannsyn.dca.vaadin.component.custom.container;

import com.sannsyn.dca.vaadin.ui.DCAUiHelper;
import com.vaadin.server.Sizeable;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;

import java.util.*;

//todo Make this a Vaadin CustomComponent
public class ItemContainer<T> {
    public static final String DEFAULT_VIEW = "default";
    private List<T> items = new ArrayList<>();
    private Map<T, ComponentContainer> componentMap = new LinkedHashMap<>();
    private Map<String, ItemPainter<T>> painters = new HashMap<>();

    private Layout mainLayout = new VerticalLayout();
    private String currentView = "default";
    private String title = "";
    private Label titleLabel = new Label(title);
    private boolean paintTitle = true;
    private CssLayout messageLayout = new CssLayout();
    private ResetAction resetAction;

    public ItemContainer(boolean paintTitle) {
        this.paintTitle = paintTitle;
        init();
    }

    public void init() {
        addTitle();

        ItemPainter<T> itemPainter = painters.get(currentView);
        for (T item : items) {
            ComponentContainer comp = itemPainter.draw(item);
            componentMap.put(item, comp);
            mainLayout.addComponent(comp);
        }

        messageLayout.setWidth(100, Sizeable.Unit.PERCENTAGE);
    }

    private void addTitle() {
        CssLayout labelLayout = new CssLayout();
        labelLayout.setStyleName("item-container-title-div");
        labelLayout.addComponent(titleLabel);
        if (paintTitle) {
            mainLayout.addComponent(labelLayout);
        }
    }

    public void setTitle(String title) {
        this.title = title;
        titleLabel.setValue(this.title);
    }

    public void registerPainter(String viewName, ItemPainter<T> painter) {
        painters.put(viewName, painter);
    }

    public Component getComponent() {
        return mainLayout;
    }

    public void addItem(T p) {
        items.add(p);
        ItemPainter<T> itemPainter = painters.get(currentView);
//      todo Should not throw NPE even though no painter is registered
        ComponentContainer componentFor = itemPainter.draw(p);
        mainLayout.addComponent(componentFor);
        componentMap.put(p, componentFor);
    }

    public void removeItem(T p) {
        ComponentContainer component = componentMap.get(p);
        if(component != null) {
            mainLayout.removeComponent(component);
        }
    }

    public void updateItem(T p) {
        ComponentContainer component = componentMap.get(p);
        ItemPainter<T> tItemPainter = painters.get(currentView);
        if (component != null) {
            tItemPainter.redraw(component, p);
            componentMap.remove(p);
            componentMap.put(p, component);
        }
    }

    public void showErrorMessage(String msg) {
        messageLayout.removeAllComponents();
        CssLayout errorLayout = DCAUiHelper.createErrorLayout(98);
        errorLayout.addStyleName("item-container-error-message-layout");
        errorLayout.addStyleName("visible");
        messageLayout.addComponent(errorLayout);
        mainLayout.addComponent(messageLayout);
    }

    public void clearForce() {
        mainLayout.removeAllComponents();
    }

    public void clear(){
        mainLayout.removeComponent(messageLayout);
        mainLayout.removeAllComponents();
        addTitle();
        items.clear();
        componentMap.clear();
        if(resetAction != null) {
            resetAction.reset();
        }
    }

    public void setResetAction(ResetAction resetAction) {
        this.resetAction = resetAction;
    }

    public void redraw() {
        if(resetAction != null) {
            resetAction.reset();
        }

        ArrayList<T> copy = new ArrayList<>(items);
        clear();
        copy.forEach(item -> addItem(item));
    }

    public void switchToView(String viewName) {
        if ("in-progress".equals(currentView) || viewName.equalsIgnoreCase(currentView)) {
            System.out.println("View switching in progress.. will do nothing");
            return;
        }

        ItemPainter<T> personItemPainter = painters.get(viewName);
        if (personItemPainter == null) return;

        currentView = "in-progress";
        for (Map.Entry<T, ComponentContainer> entry : componentMap.entrySet()) {
            T item = entry.getKey();
            ComponentContainer component = entry.getValue();
            personItemPainter.redraw(component, item);
        }
        currentView = viewName;
    }

    public void switchToDefaultView() {
        switchToView(DEFAULT_VIEW);
    }

    public interface ResetAction {
        void reset();
    }
}
