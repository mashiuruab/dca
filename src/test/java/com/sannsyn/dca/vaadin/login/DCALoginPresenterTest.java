package com.sannsyn.dca.vaadin.login;

import com.sannsyn.dca.model.user.DCAUserException;
import com.sannsyn.dca.service.DCAUserService;
import org.junit.Test;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Created by jobaer on 3/2/16.
 */
public class DCALoginPresenterTest {
    @Test
    public void testLoginIsCalled() throws DCAUserException {
        DCALoginView loginView = mock(DCALoginView.class);
        when(loginView.getUsername()).thenReturn("test");
        when(loginView.getPassword()).thenReturn("test");

        DCAUserService mockUserService = mock(DCAUserService.class);

        DCALoginPresenter presenter = new DCALoginPresenter(loginView, mockUserService);
        presenter.login();

        verify(mockUserService).login("test", "test");
        verify(loginView).afterSuccessfulLogin();
    }

    @Test
    public void testLoginFailed() throws DCAUserException {
        DCALoginView loginView = mock(DCALoginView.class);
        DCAUserService mockUserService = mock(DCAUserService.class);
        when(mockUserService.login(anyString(), anyString())).thenThrow(new DCAUserException("failed"));

        DCALoginPresenter presenter = new DCALoginPresenter(loginView, mockUserService);
        presenter.login();

        verify(loginView, never()).afterSuccessfulLogin();
        verify(loginView).failedLogin(anyString());
    }
}
