package com.sannsyn.dca.service;

import com.google.gson.Gson;
import com.sannsyn.dca.model.user.DCAUser;
import com.sannsyn.dca.model.user.DCAUserException;
import com.sannsyn.dca.util.DCAConfigProperties;
import com.sannsyn.dca.vaadin.servlet.DCAUserPreference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

/**
 * Created by jobaer on 1/24/2016.
 */
public class DCAUserLoginService implements DCAUserService {
    private static final Logger logger = LoggerFactory.getLogger(DCAUserLoginService.class.getName());
    public static final String AUTH_SERVICE = String.format("%s/loginJson", DCAConfigProperties.getAdminServerUrl());
    public static final String HMAC_SHA_256 = "HmacSHA256";

    @Override
    public DCAUser login(String username, String password) throws DCAUserException {
        String post = post(username, password);
        Gson gson = new Gson();
        HashMap hashMap = gson.fromJson(post, HashMap.class);

        if (hashMap.containsKey("status") && "ok".equals(hashMap.get("status"))) {
            if (!hashMap.containsKey("session") || !hashMap.containsKey("secret") || !hashMap.containsKey("issued")) {
                logger.warn("Session and secret info not found in the response. Login failed!");
                throw new DCAUserException("Session and secret key not present in the response. login failed!");
            }
            try {
                String token = computeTokenFromLoginResponse(username, hashMap.get("session").toString(),
                    hashMap.get("secret").toString(), hashMap.get("issued").toString());
                DCAUser dcaUser = new DCAUser(username, token, hashMap.get("session").toString());
                storeUserInVaadinSession(dcaUser);
                return dcaUser;
            } catch (NoSuchAlgorithmException | InvalidKeyException e) {
                logger.warn("Unable to compute token from session key and secret", e);
                throw new DCAUserException("Failed to compute secret. Login failed!");
            } catch (Exception e) {
                throw new DCAUserException(e.getMessage());
            }
        } else {
            System.out.println("hashMap.get(\"status\") = " + hashMap.get("status"));
            throw new DCAUserException("Invalid login!");
        }
    }

    private void storeUserInVaadinSession(DCAUser dcaUser) {
        DCAUserPreference.setLoggedInUserSessionKey(dcaUser);
    }

    /**
     * Compute the token from the provided data.
     *
     * @param username   username provided by the user
     * @param sessionKey the session key returned by the auth server
     * @param secret     the secret key returned by the auth server
     * @param issued     the date string returned by the auth server
     * @return A token if the computation is successful
     * @throws NoSuchAlgorithmException If the HMAC_SHA_256 algorithm is not available
     * @throws InvalidKeyException      If the algorithm can't be initialized with proper key
     */
    private String computeTokenFromLoginResponse(String username, String sessionKey, String secret, String issued)
                throws NoSuchAlgorithmException, InvalidKeyException {
        String data = sessionKey + ":" + username + ":" + issued;

        Mac hmacSHA256 = Mac.getInstance(HMAC_SHA_256);
        SecretKeySpec keySpec = new SecretKeySpec(secret.getBytes(), hmacSHA256.getAlgorithm());
        hmacSHA256.init(keySpec);

        byte[] rawAuth = hmacSHA256.doFinal(data.getBytes());

        // Convert to hex
        String token = (new BigInteger(1, rawAuth)).toString(16);
        System.out.println("token = " + token);
        return token;
    }

    private String post(String username, String password) {
        MultivaluedMap<String, String> formData = new MultivaluedHashMap<>();
        formData.add("username", username);
        formData.add("password", password);

        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(AUTH_SERVICE);
        String response = target.request(MediaType.APPLICATION_FORM_URLENCODED_TYPE).post(Entity.form(formData), String.class);
        return response;
    }
}
