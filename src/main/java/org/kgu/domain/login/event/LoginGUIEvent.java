package org.kgu.domain.login.event;

import org.kgu.domain.login.feature.LoginFeature;
import org.kgu.domain.login.view.LoginView;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public final class LoginGUIEvent implements ActionListener {
    private final LoginView view;
    private final LoginFeature loginFeature;

    public LoginGUIEvent(LoginView view, LoginFeature loginFeature) {
        this.view = view;
        this.loginFeature = loginFeature;
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        if (view.getPlayerId().trim().isEmpty()) {
            JOptionPane.showMessageDialog(view, "개인 ID를 입력해 주세요.");
            return;
        }
        try {
            loginFeature.login(view.getPlayerId());
        } catch (IOException e) {
            JOptionPane.showMessageDialog(view, "세이브를 읽지 못했습니다.\n" + e.getMessage(), "로그인 실패", JOptionPane.ERROR_MESSAGE);
        }
    }
}
