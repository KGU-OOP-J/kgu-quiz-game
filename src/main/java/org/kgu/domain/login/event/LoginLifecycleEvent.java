package org.kgu.domain.login.event;

import org.kgu.domain.login.view.LoginView;
import org.kgu.core.window.LifecycleEvent;

public final class LoginLifecycleEvent implements LifecycleEvent {
    private final LoginView view;
    private final LoginGUIEvent event;

    public LoginLifecycleEvent(LoginView view, LoginGUIEvent event) {
        this.view = view;
        this.event = event;
    }

    @Override
    public void onEnter() {
        view.getLoginButton().addActionListener(event);
        view.getIdField().addActionListener(event);
        view.getIdField().requestFocusInWindow();
    }

    @Override
    public void onExit() {
        view.getLoginButton().removeActionListener(event);
        view.getIdField().removeActionListener(event);
    }
}
