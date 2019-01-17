package com.sannsyn.dca.vaadin.component.custom.container;

import com.vaadin.ui.ComponentContainer;

public interface ItemPainter<T> {
    ComponentContainer draw(T item);

    void redraw(ComponentContainer comp, T item);
}
