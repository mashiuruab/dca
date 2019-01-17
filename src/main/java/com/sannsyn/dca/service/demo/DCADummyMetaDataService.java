package com.sannsyn.dca.service.demo;

import org.apache.commons.lang3.StringUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * This is a mock metadata service. It will give 503, 200, 404 status in random.
 * <p/>
 * Created by jobaer on 3/24/16.
 */
public class DCADummyMetaDataService extends HttpServlet {
    private static final String ALWAYS_503 = "/always503";
    private static final String SUCCESS_FIRST = "/200after1";
    private static final String SUCCESS_THIRD = "/200after3";
    private static final String FAIL_FIRST = "/404after1";
    private static final String FAIL_ALWAYS = "/always404";
    private static final String ALWAYS_SUCCESS = "/always200";

    private static int successFirstCount = 0;
    private static int successThirdCount = 0;
    private static int failFirstCount = 0;

    private Map<String, Consumer<HttpServletResponse>> consumerMap = new HashMap<>();

    private static final String dummyResponse = "{\n" +
        "  \"id\": \"ark:9788202505929\",\n" +
        "  \"title\": \"Den lille kaninen som så gjerne ville so\",\n" +
        "  \"author\": \"Ehrlin, Carl-Johan Forssén\",\n" +
        "  \"keywords\": null,\n" +
        "  \"thumbnailUrl\": \"https://d3oh18gu5j3rjh.cloudfront.net/9788202505929/img/0-M\"\n" +
        "}";

    @Override
    public void init() throws ServletException {
        consumerMap.put(ALWAYS_503, this::send503);
        consumerMap.put(SUCCESS_FIRST, this::successFirst);
        consumerMap.put(SUCCESS_THIRD, this::successThird);
        consumerMap.put(FAIL_FIRST, this::failFirst);
        consumerMap.put(FAIL_ALWAYS, this::sendNotFound);
        consumerMap.put(ALWAYS_SUCCESS, this::send200);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("Got request for metadata at " + new Date());
        String pathInfo = req.getPathInfo();
        System.out.println(pathInfo);
        if (StringUtils.isBlank(pathInfo)) {
            sendBadRequest(resp);
            return;
        }

        Consumer<HttpServletResponse> consumer = consumerMap.get(pathInfo);
        if (consumer == null) {
            sendBadRequest(resp);
        } else {
            consumer.accept(resp);
        }
    }

    private void failFirst(HttpServletResponse resp) {
        if (failFirstCount >= 1) {
            sendNotFound(resp);
            failFirstCount = 0;
        } else {
            failFirstCount++;
            send503(resp);
        }
    }

    private void successThird(HttpServletResponse resp) {
        if (successThirdCount >= 3) {
            send200(resp);
            successThirdCount = 0;
        } else {
            successThirdCount++;
            send503(resp);
        }
    }

    private void successFirst(HttpServletResponse resp) {
        if (successFirstCount >= 1) {
            send200(resp);
            successFirstCount = 0;
        } else {
            successFirstCount++;
            send503(resp);
        }
    }

    private void send503(HttpServletResponse resp) {
        try {
            resp.setHeader("retry-after", "3");
            resp.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "Retry after specified time");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void send200(HttpServletResponse resp) {
        resp.setStatus(200);
        resp.setContentType("application/json");
        try (PrintWriter writer = resp.getWriter()) {
            writer.print(dummyResponse);
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendBadRequest(HttpServletResponse resp) {
        try {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Provide a valid path");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendNotFound(HttpServletResponse resp) {
        try {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Not found");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
