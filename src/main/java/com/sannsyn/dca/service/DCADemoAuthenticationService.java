package com.sannsyn.dca.service;

import org.apache.commons.lang3.StringUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * This is just a mock service for authentication. When we integrate with the original service
 * this will be removed.
 * <p/>
 * Created by jobaer on 2/24/16.
 */
public class DCADemoAuthenticationService extends HttpServlet {

    /**
     * wrong/wrong - {"msg":"no such user","status":"error"}
     * user/user - {"msg":"denied","status":"failed"}
     * admin/admin - {"session":"cvqnj16s2qpdg","secret":"kubb2goe3bg4urb12q3kii2agk","issued":"1452866169767","status":"ok"}
     */
    private static final Map<String, String> authMapDummy;

    static {
        authMapDummy = new HashMap<>();
        authMapDummy.put("none", "{\"msg\":\"no such user\",\"status\":\"error\"}");
        authMapDummy.put("wrongwrong", "{\"msg\":\"no such user\",\"status\":\"error\"}");
        authMapDummy.put("useruser", "{\"msg\":\"denied\",\"status\":\"failed\"}");
        authMapDummy.put("adminadmin", "{\"session\":\"cvqnj16s2qpdg\",\"secret\":\"kubb2goe3bg4urb12q3kii2agk\"," +
            "\"issued\":\"1452866169767\",\"status\":\"ok\"}");
    }

    /**
     * Dummy implementation
     *
     * @param req  - the request object. We will read the username/password from it's params.
     * @param resp - the response object. We will write our response json to it.
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String username = req.getParameter("username");
        String password = req.getParameter("password");

        System.out.println("Got username=" + username + " and password=" + password);
        String dummyResponse = getDummyResponse(username, password);

        resp.setContentType("application/json");
        try (PrintWriter writer = resp.getWriter()) {
            writer.print(dummyResponse);
            writer.flush();
        }
    }

    private String getDummyResponse(String username, String password) {
        String defaultKey = "none";
        String key = "";
        if (StringUtils.isNotEmpty(username) && StringUtils.isNotEmpty(password)) {
            key = username + password;
        }

        if (authMapDummy.containsKey(key)) {
            return authMapDummy.get(key);
        } else {
            return authMapDummy.get(defaultKey);
        }
    }
}
