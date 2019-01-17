package com.sannsyn.dca.vaadin.servlet;

import com.vaadin.server.VaadinServlet;

import javax.servlet.ServletException;

/**
 * Created by mashiur on 2/23/16.
 */
public class DCAServlet extends VaadinServlet {
    @Override
    protected final void servletInitialized() throws ServletException {
        super.servletInitialized();
        getService().addSessionInitListener(new DashboardSessionInitListener());
    }
}
