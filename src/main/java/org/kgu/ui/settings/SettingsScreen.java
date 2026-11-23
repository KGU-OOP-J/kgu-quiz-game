package org.kgu.ui.settings;

import java.awt.*;
import javax.swing.*;
import org.kgu.ui.common.UiButton;
import org.kgu.ui.common.UiStyle;
import org.kgu.ui.navigation.GameNavigator;

/** 사운드 등 앱 환경을 관리하는 설정 화면. */
public final class SettingsScreen extends JPanel {
    public SettingsScreen(GameNavigator navigator) { setPreferredSize(new Dimension(UiStyle.WIDTH, UiStyle.HEIGHT)); setLayout(null); JCheckBox sound = new JCheckBox("효과음 사용", true); sound.setBounds(260, 260, 240, 45); sound.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 20f)); add(sound); UiButton back = new UiButton("메뉴로", new Color(112, 128, 144)); back.setBounds(300, 380, 180, 55); back.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 18f)); back.setForeground(Color.WHITE); back.addActionListener(e -> navigator.showMenu()); add(back); }
    @Override protected void paintComponent(Graphics s) { Graphics2D g = (Graphics2D) s.create(); UiStyle.prepare(g); g.setColor(new Color(245, 248, 252)); g.fillRect(0, 0, getWidth(), getHeight()); UiStyle.round(g, 185, 145, 410, 335, 16, Color.WHITE); g.setColor(UiStyle.NAVY); g.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 34f)); UiStyle.center(g, "설정", 390, 210); g.dispose(); }
}
