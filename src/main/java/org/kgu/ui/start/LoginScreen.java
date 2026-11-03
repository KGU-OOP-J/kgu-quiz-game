package org.kgu.ui.start;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import org.kgu.ui.common.UiButton;
import org.kgu.ui.common.UiStyle;
import org.kgu.ui.navigation.GameNavigator;

/** 개인 ID를 입력받는 시작 화면. */
public final class LoginScreen extends JPanel {
    public LoginScreen(GameNavigator navigator) {
        setPreferredSize(new Dimension(UiStyle.WIDTH, UiStyle.HEIGHT)); setLayout(null);
        JTextField id = new JTextField(); id.setBounds(245, 300, 290, 52); id.setFont(UiStyle.FONT.deriveFont(20f)); id.setBorder(new CompoundBorder(new LineBorder(new Color(180, 190, 202), 1, true), new EmptyBorder(2, 15, 2, 10))); add(id);
        UiButton enter = new UiButton("시작하기", new Color(36, 126, 238)); enter.setBounds(245, 385, 290, 62); enter.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 23f)); enter.setForeground(Color.WHITE);
        enter.addActionListener(e -> { String value = id.getText().trim(); if (value.isEmpty()) { JOptionPane.showMessageDialog(this, "개인 ID를 입력해주세요."); return; } navigator.getSession().setPlayerId(value); navigator.showMenu(); }); add(enter);
    }
    @Override protected void paintComponent(Graphics source) { Graphics2D g = (Graphics2D) source.create(); UiStyle.prepare(g); g.setColor(new Color(245, 248, 252)); g.fillRect(0, 0, getWidth(), getHeight()); UiStyle.round(g, 170, 135, 440, 345, 16, Color.WHITE); g.setColor(UiStyle.NAVY); g.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 38f)); UiStyle.center(g, "교수님 이름 맞추기", 390, 210); g.setFont(UiStyle.FONT.deriveFont(Font.PLAIN, 18f)); UiStyle.center(g, "개인 ID를 입력하고 시작하세요.", 390, 248); g.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 18f)); g.drawString("개인 ID", 245, 285); g.dispose(); }
}
