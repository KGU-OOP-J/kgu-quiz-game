package org.kgu.domain.login.view;

import org.kgu.domain.login.view.layout.LoginLayout;

import org.kgu.core.window.*;
import javax.swing.*;
import java.awt.*;

public final class LoginView extends JPanel implements View {
    private final JTextField playerId = new JTextField();
    private final JButton login = new UiButton("여정 시작하기", GameTheme.TEAL);

    public LoginView() {
        super(new BorderLayout());
        setBackground(GameTheme.PAGE);
        add(new LoginLayout(playerId, login), BorderLayout.CENTER);
    }

    public String getPlayerId() { return playerId.getText(); }
    public JTextField getIdField() { return playerId; }
    public JButton getLoginButton() { return login; }
    @Override protected void paintComponent(Graphics source) {
        GameTheme.paintBackdrop(source, this, "login", 0f);
    }
    @Override public ViewId getViewId() { return ViewId.LOGIN; }
    @Override public JComponent getComponent() { return this; }
    @Override public Dimension getViewSize() { return new Dimension(1280, 960); }
}
