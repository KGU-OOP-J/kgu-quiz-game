package org.kgu.ui.start;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import org.kgu.ui.common.UiButton;
import org.kgu.ui.common.UiStyle;
import org.kgu.ui.navigation.GameNavigator;

/** 사진과 같은 시작 화면의 입력 UI와 배경 일러스트를 담당한다. */
public final class LoginScreen extends JPanel {
    private static final String PLACEHOLDER = "이름을 입력하세요";
    private final GameNavigator navigator;
    private final JTextField nameField = new JTextField(PLACEHOLDER);

    public LoginScreen(GameNavigator navigator) {
        this.navigator = navigator;
        setPreferredSize(new Dimension(UiStyle.WIDTH, UiStyle.HEIGHT)); setLayout(null); setOpaque(false);
        addNameInput(); addModeSelector(); addStartButton();
    }

    private void addNameInput() {
        nameField.setBounds(333, 253, 248, 43); nameField.setFont(UiStyle.FONT.deriveFont(15f)); nameField.setForeground(new Color(166, 172, 180));
        nameField.setBorder(new CompoundBorder(new LineBorder(new Color(219, 225, 231), 1, true), new EmptyBorder(2, 13, 2, 10)));
        nameField.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { if (nameField.getText().equals(PLACEHOLDER)) { nameField.setText(""); nameField.setForeground(new Color(55, 65, 80)); } }
            @Override public void focusLost(FocusEvent e) { if (nameField.getText().isBlank()) { nameField.setText(PLACEHOLDER); nameField.setForeground(new Color(166, 172, 180)); } }
        });
        add(nameField);
    }

    private void addModeSelector() {
        JComboBox<String> mode = new JComboBox<>(new String[] { "일반 모드 (60초)", "빠른 모드 (40초)" });
        mode.setBounds(333, 316, 248, 44); mode.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 15f)); mode.setBackground(Color.WHITE); mode.setForeground(new Color(38, 47, 58));
        mode.setBorder(new LineBorder(new Color(169, 181, 195), 1, true)); add(mode);
    }

    private void addStartButton() {
        UiButton start = new UiButton("▶  게임 시작", new Color(29, 122, 239));
        start.setBounds(228, 431, 385, 67); start.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 27f)); start.setForeground(Color.WHITE);
        start.addActionListener(e -> navigator.showMenu()); add(start);
    }

    @Override protected void paintComponent(Graphics source) {
        Graphics2D g = (Graphics2D) source.create(); UiStyle.prepare(g);
        paintCampus(g); paintTitle(g); paintCard(g); paintBooks(g); g.dispose();
    }

    private void paintCampus(Graphics2D g) {
        g.setColor(new Color(192, 229, 250)); g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(new Color(255, 255, 255, 160)); cloud(g, 58, 72, 36); cloud(g, 680, 61, 39);
        g.setColor(new Color(218, 204, 168, 155)); g.fillRect(3, 252, 178, 188); g.fillRect(670, 161, 90, 285);
        g.setColor(new Color(205, 189, 154)); g.fillPolygon(new int[] {654, 714, 776}, new int[] {162, 107, 162}, 3);
        g.setColor(new Color(130, 191, 113, 175)); g.fillRect(0, 426, getWidth(), 150); tree(g, 57, 218, 66); tree(g, 730, 261, 52);
    }

    private void paintTitle(Graphics2D g) {
        g.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 61f));
        UiStyle.outlinedText(g, "교수님이름", 103, 160, new Color(249, 251, 254), new Color(22, 47, 75));
        UiStyle.outlinedText(g, "맞추기", 448, 160, new Color(255, 202, 69), new Color(22, 47, 75));
        g.setColor(new Color(24, 49, 78)); g.fillPolygon(new int[] {596, 647, 703, 648}, new int[] {80, 60, 83, 105}, 4); g.fillRoundRect(622, 93, 49, 19, 7, 7);
        UiStyle.round(g, 181, 181, 411, 35, 18, new Color(255, 255, 255, 235)); g.setColor(UiStyle.NAVY); g.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 16f)); UiStyle.center(g, "제한 시간 안에 힌트를 보고 교수님을 맞춰보세요!", 386, 204);
    }

    private void paintCard(Graphics2D g) {
        UiStyle.round(g, 128, 234, 517, 317, 12, new Color(255, 255, 255, 240));
        g.setColor(new Color(45, 49, 56)); g.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 19f));
        drawPersonIcon(g, 186, 273); g.drawString("사용자 이름", 217, 280);
        drawBookIcon(g, 186, 336); g.drawString("게임 난이도", 217, 344);
        UiStyle.round(g, 243, 377, 369, 73, 11, new Color(249, 246, 237)); drawBulb(g, 276, 414);
        g.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 16f)); g.drawString("제시되는 힌트를 보고", 305, 408); g.drawString("해당 교수님의 이름을 맞혀보세요!", 305, 432);
    }

    private void paintBooks(Graphics2D g) {
        g.setColor(new Color(21, 48, 80)); g.fillPolygon(new int[] {3, 68, 194, 97}, new int[] {449, 411, 455, 503}, 4); g.fillRoundRect(55, 494, 73, 30, 5, 5);
        book(g, 3, 501, 170, new Color(26, 80, 145)); book(g, 0, 527, 173, new Color(210, 146, 43)); book(g, 13, 551, 159, new Color(185, 113, 37));
    }
    private void book(Graphics2D g, int x, int y, int width, Color cover) { g.setColor(cover); g.fillRoundRect(x, y, width, 22, 5, 5); g.setColor(new Color(248, 235, 197)); g.fillRoundRect(x + 18, y + 4, width - 28, 13, 2, 2); }
    private void cloud(Graphics2D g, int x, int y, int s) { g.fillOval(x - s, y, s * 2, s); g.fillOval(x, y - s / 2, s * 2, s + 8); g.fillOval(x + s, y, s * 2, s); }
    private void tree(Graphics2D g, int x, int y, int s) { g.setColor(new Color(102, 81, 58, 150)); g.fillRect(x - 5, y + s, 10, 150); g.setColor(new Color(101, 174, 105, 180)); g.fillOval(x - s, y, s * 2, s * 2); g.fillOval(x - s / 2, y - s / 2, s * 2, s * 2); }
    private void drawPersonIcon(Graphics2D g, int x, int y) { g.setColor(new Color(135, 151, 164)); g.fillOval(x - 7, y - 15, 14, 15); g.fillRoundRect(x - 11, y + 3, 22, 13, 7, 7); g.setColor(UiStyle.NAVY); g.drawOval(x - 7, y - 15, 14, 15); }
    private void drawBookIcon(Graphics2D g, int x, int y) { g.setColor(new Color(183, 118, 70)); g.setStroke(new BasicStroke(2)); g.drawRect(x - 14, y - 12, 13, 19); g.drawRect(x + 1, y - 12, 13, 19); g.drawLine(x, y - 12, x, y + 8); }
    private void drawBulb(Graphics2D g, int x, int y) { g.setColor(new Color(255, 198, 31)); g.fillOval(x - 8, y - 16, 16, 20); g.setColor(new Color(107, 80, 43)); g.fillRect(x - 4, y + 4, 8, 7); }
}
