package com.sannsyn.dca.vaadin.widgets.operations.dashboard.recommendation;

import com.sannsyn.dca.vaadin.view.DCALayout;

/**
 * Created by mashiur on 3/31/16.
 */
public abstract class DCAWidgetLiveUpdateComponent extends DCALayout{
    public abstract void startLiveUpdate();
    public abstract void shutDownLiveUpdate();
}
