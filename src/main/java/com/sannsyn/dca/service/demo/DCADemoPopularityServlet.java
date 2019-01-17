package com.sannsyn.dca.service.demo;

import org.apache.commons.io.IOUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

/**
 * This is a dummy service for the popularity widget.
 *
 * Created by jobaer on 3/7/16.
 */
public class DCADemoPopularityServlet extends HttpServlet {
    private static int currentServing = 1;
    private static final String response1 = "/com/sannsyn/dca/service/demo/popularityDemoResponse1.json";
    private static final String response2 = "/com/sannsyn/dca/service/demo/popularityDemoResponse2.json";
    private static final String response3 = "/com/sannsyn/dca/service/demo/popularityDemoResponse3.json";


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String dummyResponse = getDummyResponse();
        resp.setContentType("application/json");
        try (PrintWriter writer = resp.getWriter()) {
            writer.print(dummyResponse);
            writer.flush();
        }
    }

    private String getDummyResponse() throws IOException {
        if(currentServing == 1) {
            currentServing++;
            return getResponseFromFile(response1);
        } else if (currentServing == 2) {
            currentServing++;
            return getResponseFromFile(response2);
        } else {
            currentServing = 1;
            return getResponseFromFile(response3);
        }
    }

    private String getResponseFromFile(String fileName) throws IOException {
        InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName);
        String s = IOUtils.toString(inputStream);
        IOUtils.closeQuietly(inputStream);
        return s;
    }
}
