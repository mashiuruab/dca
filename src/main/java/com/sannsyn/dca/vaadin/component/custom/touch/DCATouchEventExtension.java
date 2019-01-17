package com.sannsyn.dca.vaadin.component.custom.touch;

import com.vaadin.annotations.JavaScript;
import com.vaadin.ui.AbstractJavaScriptComponent;
import com.vaadin.ui.Component;
import com.vaadin.ui.JavaScriptFunction;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * An extension that will allow components to detect touch events. Specially
 * swipeLeft, swipeRight and doubleTap events. It attaches the events
 * based on the "id" of the component. So the component must have the id attribute.
 * <p>
 * Created by jobaer on 5/19/17.
 */
@JavaScript({"touchevent-extension.js", "touchevent-extension-connector.js"})
public class DCATouchEventExtension extends AbstractJavaScriptComponent {
    public DCATouchEventExtension(Component component) {
        getState().setIdentifier(component.getId());

        addFunction("swipeLeft", (JavaScriptFunction) arguments -> {
            for (TouchEventListener listener : listeners)
                listener.swipeLeft();
        });

        addFunction("swipeRight", (JavaScriptFunction) arguments -> {
            for (TouchEventListener listener : listeners)
                listener.swipeRight();
        });

        addFunction("doubletap", (JavaScriptFunction) arguments -> {
            for (TouchEventListener listener : listeners)
                listener.doubleTap();
        });
    }

    public interface TouchEventListener extends Serializable {
        void swipeLeft();

        void swipeRight();

        void doubleTap();
    }

    private ArrayList<TouchEventListener> listeners = new ArrayList<>();

    public void addTouchEventListener(TouchEventListener listener) {
        listeners.add(listener);
    }

    @Override
    protected DCATouchComponentState getState() {
        return (DCATouchComponentState) super.getState();
    }
}

