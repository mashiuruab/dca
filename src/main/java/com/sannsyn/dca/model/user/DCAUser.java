package com.sannsyn.dca.model.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by jobaer on 1/24/2016.
 */
//todo make serializable with proper serialversion
public class DCAUser implements HttpSessionBindingListener
{
    private static final Logger logger = LoggerFactory.getLogger(DCAUser.class);
    public static Map<String, Integer> logins = new ConcurrentHashMap<>();

    private String username;
    private String token;
    private String session;

    public DCAUser(String username, String secret, String session) {
        this.username = username;
        this.token = secret;
        this.session = session;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String password) {
        this.token = password;
    }

    public String getSession() {
        return session;
    }

    public void setSession(String session) {
        this.session = session;
    }

    @Override
    public String toString() {
        return "DCAUser{" +
                "username='" + username + '\'' +
                ", token='" + token + '\'' +
                ", session='" + session + '\'' +
                '}';
    }

    @Override
    public void valueBound(HttpSessionBindingEvent event) {
        if (logger.isDebugEnabled()) {
            logger.debug(String.format("Bounding the login object for user %s in session %s", getUsername(), event.getSession().getId()));
        }

        if (logins.containsKey(getUsername())) {
            logins.put(getUsername(), logins.get(getUsername()) + 1);
        } else {
            logins.put(getUsername(), 1);
        }
    }

    @Override
    public void valueUnbound(HttpSessionBindingEvent event) {
        if (logger.isDebugEnabled()) {
            logger.debug(String.format("Un Bounding the login object for user %s in session %s", getUsername(), event.getSession().getId()));
        }

        int currentlyLoggedInUserCount = logins.get(getUsername()) - 1;

        if (currentlyLoggedInUserCount < 1) {
            logins.remove(getUsername());
        } else {
            logins.put(getUsername(), currentlyLoggedInUserCount);
        }
    }
}
