package com.sannsyn.dca.vaadin.servlet.dummy;

import org.apache.commons.io.IOUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by mashiur on 2/24/16.
 */
public class DCADummyTopMenuServlet extends HttpServlet {
    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        resp.getWriter().println(getDummyJsonString());
    }

    private String getDummyJsonString() {
        InputStream inputStream = null;
        try {
            inputStream = getClass().getClassLoader().getResourceAsStream("sample-config.json");
            return IOUtils.toString(inputStream, "UTF-8");
        } catch (IOException io) {
            throw new RuntimeException(io);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }
}
