package com.sannsyn.dca.vaadin.component.custom.container;

import com.sannsyn.dca.vaadin.component.custom.touch.DCATouchEventExtension;
import com.vaadin.ui.CssLayout;

/**
 * An extension of CssLayout that supports touch events. Currently it only supports
 * swipeLeft, swipeRight and doubleTap events.
 *
 * <p>
 * Created by jobaer on 5/23/17.
 */
public class DCATouchLayout extends CssLayout {
    private DCATouchEventExtension touchEventExtension;

    public DCATouchLayout(String htmlId) {
        setId(htmlId);
        touchEventExtension = new DCATouchEventExtension(this);
        addComponent(touchEventExtension);
    }

    public void addSwipeLeftListener(SwipeLeftListener listener) {
        TouchEventAdapter touchListener = new TouchEventAdapter() {
            @Override
            public void swipeLeft() {
                listener.swipeLeft();
            }
        };
        touchEventExtension.addTouchEventListener(touchListener);
    }

    public void addSwipeRightListener(SwipeRightListener listener) {
        TouchEventAdapter touchListener = new TouchEventAdapter() {
            @Override
            public void swipeRight() {
                listener.swipeRight();
            }
        };
        touchEventExtension.addTouchEventListener(touchListener);
    }

    public void addDoubletapListener(DoubleTapListener listener) {
        TouchEventAdapter eventAdapter = new TouchEventAdapter() {
            @Override
            public void doubleTap() {
                listener.doubleTap();
            }
        };
        touchEventExtension.addTouchEventListener(eventAdapter);
    }

    public interface SwipeLeftListener {
        void swipeLeft();
    }

    public interface SwipeRightListener {
        void swipeRight();
    }

    public interface DoubleTapListener {
        void doubleTap();
    }

    private class TouchEventAdapter implements DCATouchEventExtension.TouchEventListener {
        @Override
        public void swipeLeft() {
        }

        @Override
        public void swipeRight() {
        }

        @Override
        public void doubleTap() {

        }
    }
}