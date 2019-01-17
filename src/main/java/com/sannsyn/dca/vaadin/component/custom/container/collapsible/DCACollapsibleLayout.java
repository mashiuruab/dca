package com.sannsyn.dca.vaadin.component.custom.container.collapsible;

import com.vaadin.ui.Component;

/**
 * A collapsible layout interface.
 * <p>
 * Created by jobaer on 3/7/17.
 */
public interface DCACollapsibleLayout extends Component {
    /**
     * Set the component which will be shown when the layout is expanded.
     * Calling this method will remove the previous component.
     *
     * @param component the component to be shown in expanded state.
     */
    public void setExpansionComponent(Component component);

    /**
     * Set the component which will be shown when the layout is collpased.
     * Calling this method will remove the previous component.
     *
     * @param component the component to be shown in collapsed state.
     */
    public void setCollapseComponent(Component component);
}
