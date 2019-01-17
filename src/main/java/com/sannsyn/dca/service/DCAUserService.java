package com.sannsyn.dca.service;

import com.sannsyn.dca.model.user.DCAUser;
import com.sannsyn.dca.model.user.DCAUserException;

/**
 * Created by jobaer on 1/24/2016.
 */
public interface DCAUserService {
    DCAUser login(String username, String password) throws DCAUserException;
}
