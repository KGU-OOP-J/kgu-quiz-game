package org.kgu.integration.ui.start;

import java.awt.*;
import javax.swing.*;
import org.kgu.integration.mgr.Manager;
import org.kgu.integration.ui.common.UiButton;
import org.kgu.integration.ui.common.UiStyle;
import org.kgu.integration.ui.navigation.GameNavigator;

/** Manager.find()로 선택 메뉴를 검증하고 다음 화면을 결정한다. */
public final class MenuScreen extends JPanel {
    private final GameNavigator navigator;
    private final Manager manager;
    public MenuScreen(GameNavigator navigator, Manager manager) {
        this.navigator = navigator; this.manager = manager; setPreferredSize(new Dimension(UiStyle.WIDTH, UiStyle.HEIGHT)); setLayout(null);
        add(menuButton("게임 시작", "game", 55, new Color(35, 133, 246)));
        add(menuButton("카드 도감", "collection", 290, new Color(239, 151, 43)));
        add(menuButton("설정", "settings", 525, new Color(30, 172, 111)));
    }
    private JButton menuButton(String label, String key, int x, Color color) {
        UiButton button = new UiButton(label, color); button.setBounds(x, 210, 200, 255); button.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 28f)); button.setForeground(Color.WHITE); button.addActionListener(e -> select(key)); return button;
    }
    private void select(String key) {
        if (manager.find(key) == null) return;
        if (key.equals("game")) navigator.showGameSetup();
        else if (key.equals("collection")) navigator.showCollection();
        else navigator.showSettings();
    }
    @Override protected void paintComponent(Graphics source) { Graphics2D g = (Graphics2D) source.create(); UiStyle.prepare(g); g.setColor(new Color(239, 249, 250)); g.fillRect(0, 0, getWidth(), getHeight()); g.setColor(UiStyle.NAVY); g.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 34f)); UiStyle.center(g, "무엇을 해볼까요?", 390, 115); g.setFont(UiStyle.FONT.deriveFont(18f)); UiStyle.center(g, "원하는 메뉴를 선택해주세요.", 390, 150); g.dispose(); }
}
