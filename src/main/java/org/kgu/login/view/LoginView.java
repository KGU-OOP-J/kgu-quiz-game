package org.kgu.login.view;

import org.kgu.window.UiButton;
import org.kgu.window.UiStyle;
import org.kgu.window.View;
import org.kgu.window.ViewId;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

public final class LoginView extends JPanel implements View {
    private final JTextField playerId = new JTextField();
    private final UiButton login = new UiButton("시작하기", new Color(36, 126, 238));

    public LoginView() {
        setLayout(null);
        playerId.setBounds(245, 300, 290, 52);
        playerId.setFont(UiStyle.FONT.deriveFont(20f));
        playerId.setBorder(new CompoundBorder(new LineBorder(new Color(180, 190, 202), 1, true),
            new EmptyBorder(2, 15, 2, 10)));
        add(playerId);
        login.setBounds(245, 385, 290, 62);
        login.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 23f));
        login.setForeground(Color.WHITE);
        add(login);
    }

    public String getPlayerId() {
        return playerId.getText();
    }

    public JTextField getIdField() {
        return playerId;
    }

    public JButton getLoginButton() {
        return login;
    }

    @Override
    public ViewId getViewId() {
        return ViewId.LOGIN;
    }

    @Override
    public JComponent getComponent() {
        return this;
    }

    @Override
    public Dimension getViewSize() {
        return new Dimension(UiStyle.WIDTH, UiStyle.HEIGHT);
    }

    @Override
    protected void paintComponent(Graphics source) {
        super.paintComponent(source);
        Graphics2D g = (Graphics2D) source.create();
        UiStyle.prepare(g);
        g.setColor(new Color(245, 248, 252));
        g.fillRect(0, 0, getWidth(), getHeight());
        UiStyle.round(g, 170, 135, 440, 345, 16, Color.WHITE);
        g.setColor(UiStyle.NAVY);
        g.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 38f));
        UiStyle.center(g, "WHO’S NEXT?", 390, 210);
        g.setFont(UiStyle.FONT.deriveFont(18f));
        UiStyle.center(g, "개인 ID를 입력하고 시작하세요.", 390, 248);
        g.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 18f));
        g.drawString("개인 ID", 245, 285);
        g.dispose();
    }
}
