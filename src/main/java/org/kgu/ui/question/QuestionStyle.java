
package org.kgu.ui.question;

import java.awt.*;
import java.awt.event.ActionListener;
import javax.swing.*;
import org.kgu.game.PlayerSession;

public final class QuestionStyle {

    // ===== 공통 색상 및 글꼴 =====
    public static final Color NAVY = new Color(27, 52, 91);
    public static final Color BLUE = new Color(38, 126, 239);
    public static final Color GREEN = new Color(19, 164, 119);
    public static final Color BACKGROUND = new Color(239, 246, 255);

    private QuestionStyle() {}

    public static Font font(float size, boolean bold) {
        return new Font("SansSerif", bold ? Font.BOLD : Font.PLAIN, Math.round(size));
    }

    // ===== 선택지 버튼 디자인 =====
    public static class OptionRow extends JPanel {
        private boolean selected = false;

        public OptionRow() {
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics source) {
            super.paintComponent(source);
            Graphics2D g = (Graphics2D) source.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();

            if (selected) {
                g.setColor(new Color(15, 83, 175, 85));
                g.fillRoundRect(3, 8, w - 7, h - 12, 28, 28);
                g.setPaint(new GradientPaint(0, 0, new Color(220, 240, 255), 0, h, new Color(105, 178, 250)));
                g.fillRoundRect(1, 1, w - 4, h - 10, 26, 26);
                g.setColor(new Color(255, 255, 255, 120));
                g.fillRoundRect(5, 4, w - 12, 15, 20, 20);
                g.setColor(new Color(35, 128, 245));
                g.setStroke(new BasicStroke(2.5f));
                g.drawRoundRect(1, 1, w - 4, h - 10, 26, 26);
            } else {
                g.setColor(new Color(150, 112, 20, 65));
                g.fillRoundRect(3, 8, w - 7, h - 12, 28, 28);
                g.setPaint(new GradientPaint(0, 0, new Color(255, 253, 215), 0, h, new Color(255, 225, 125)));
                g.fillRoundRect(1, 1, w - 4, h - 10, 26, 26);
                g.setColor(new Color(255, 255, 255, 145));
                g.fillRoundRect(5, 4, w - 12, 15, 20, 20);
                g.setColor(new Color(238, 204, 105));
                g.setStroke(new BasicStroke(1.2f));
                g.drawRoundRect(1, 1, w - 4, h - 10, 26, 26);
            }
            g.dispose();
        }
    }

    // ===== 하단 버튼 생성 및 디자인 =====
    public static JButton createBottomButton(String text, int x, Color color, ActionListener action) {
        JButton button = new RoundedButton(text, color);
        button.setBounds(x, 845, 260, 75);
        button.setFont(font(28f, true));
        button.setForeground(Color.WHITE);
        button.addActionListener(action);
        return button;
    }

    private static class RoundedButton extends JButton {
        private final Color baseColor;

        RoundedButton(String text, Color color) {
            super(text);
            baseColor = color;
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics source) {
            Graphics2D g = (Graphics2D) source.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color top = getModel().isPressed() ? baseColor.darker() : baseColor.brighter();

            g.setColor(new Color(25, 48, 80, 55));
            g.fillRoundRect(3, 7, getWidth() - 6, getHeight() - 10, 35, 35);
            g.setPaint(new GradientPaint(0, 0, top, 0, getHeight(), baseColor.darker()));
            g.fillRoundRect(2, 1, getWidth() - 6, getHeight() - 9, 35, 35);
            g.setColor(new Color(255, 255, 255, 180));
            g.setStroke(new BasicStroke(2f));
            g.drawRoundRect(3, 2, getWidth() - 8, getHeight() - 12, 35, 35);
            g.dispose();
            super.paintComponent(source);
        }
    }

    // ===== 전체 화면 배경 및 상단 정보 =====
    public static void paintScreen(Graphics source, JPanel panel, Image backgroundImage, PlayerSession session, int questionIndex) {
        Graphics2D g = (Graphics2D) source.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, panel.getWidth(), panel.getHeight(), panel);
        } else {
            g.setColor(BACKGROUND);
            g.fillRect(0, 0, panel.getWidth(), panel.getHeight());
        }

        String difficulty = session.getDifficulty();
        String stars = "하".equals(difficulty) ? "★" : "중".equals(difficulty) ? "★★" : "★★★";
        String mode = "연습".equals(session.getMode()) ? "연습모드" : "대진모드";

        // 상단 배지
        drawBadge(g, 55, 38, 280, 75, "▤  분야: " + session.getField());
        drawBadge(g, 350, 38, 280, 75, stars + "  난이도: " + difficulty);
        drawBadge(g, 645, 38, 300, 75, mode);

        // 문제 번호 및 진행도
        g.setPaint(new GradientPaint(1000, 35, new Color(49, 145, 255), 1220, 95, new Color(17, 67, 151)));
        g.fillRoundRect(990, 35, 245, 75, 35, 35);
        g.setColor(Color.WHITE);
        g.setStroke(new BasicStroke(2f));
        g.drawRoundRect(990, 35, 245, 75, 35, 35);
        g.setFont(font(32f, true));
        drawCentered(g, "문제 " + (questionIndex + 1) + " / 10", 1112, 82);

        g.setColor(Color.WHITE);
        g.fillRoundRect(60, 125, 1160, 29, 20, 20);
        g.setColor(new Color(197, 220, 248));
        g.fillRoundRect(65, 130, 1150, 19, 15, 15);
        g.setColor(BLUE);
        g.fillRoundRect(65, 130, 1150 * (questionIndex + 1) / 10, 19, 15, 15);

        // 메인 카드
        g.setColor(new Color(24, 57, 103, 55));
        g.fillRoundRect(37, 190, 1208, 635, 35, 35);
        g.setColor(new Color(255, 255, 255, 245));
        g.fillRoundRect(40, 185, 1200, 635, 35, 35);
        g.setColor(new Color(104, 171, 255));
        g.setStroke(new BasicStroke(3f));
        g.drawRoundRect(40, 185, 1200, 635, 35, 35);

        // 인물 사진 영역
        g.setColor(new Color(160, 205, 176, 90));
        g.fillRoundRect(80, 280, 390, 460, 32, 32);
        g.setColor(new Color(218, 242, 225));
        g.fillRoundRect(85, 275, 380, 450, 30, 30);
        g.setColor(new Color(177, 216, 190));
        g.setStroke(new BasicStroke(3f));
        g.drawRoundRect(85, 275, 380, 450, 30, 30);
        g.setColor(new Color(75, 126, 91));
        g.setFont(font(31f, true));
        drawCentered(g, "인물 사진 영역", 275, 520);

        // 문제 제목 및 문제 카드
        g.setPaint(new GradientPaint(500, 205, new Color(65, 157, 255), 760, 255, new Color(20, 83, 180)));
        g.fillRoundRect(500, 205, 260, 52, 25, 25);
        g.setColor(Color.WHITE);
        g.setFont(font(25f, true));
        drawCentered(g, "▣  QUESTION", 630, 240);

        g.setColor(new Color(212, 226, 245));
        g.fillRoundRect(495, 260, 715, 110, 25, 25);
        g.setColor(Color.WHITE);
        g.fillRoundRect(500, 255, 705, 105, 25, 25);
        g.setColor(new Color(190, 212, 242));
        g.setStroke(new BasicStroke(2f));
        g.drawRoundRect(500, 255, 705, 105, 25, 25);

        // 선택 안내 문구
        g.setColor(NAVY);
        g.setFont(font(25f, true));
        g.drawString("🎯  정답을 선택해 주세요.", 510, 395);
        g.dispose();
    }

    // ===== 상단 배지 디자인 =====
    private static void drawBadge(Graphics2D g, int x, int y, int width, int height, String text) {
        g.setColor(new Color(10, 48, 105, 85));
        g.fillRoundRect(x + 2, y + 7, width - 2, height - 7, 32, 32);
        g.setPaint(new GradientPaint(x, y, new Color(65, 173, 255), x, y + height, new Color(9, 75, 166)));
        g.fillRoundRect(x, y, width, height - 5, 32, 32);
        g.setColor(new Color(255, 255, 255, 55));
        g.fillRoundRect(x + 5, y + 4, width - 10, 18, 25, 25);
        g.setColor(new Color(215, 238, 255));
        g.setStroke(new BasicStroke(2.5f));
        g.drawRoundRect(x, y, width, height - 5, 32, 32);
        g.setColor(Color.WHITE);
        g.setFont(font(23f, true));
        FontMetrics fm = g.getFontMetrics();
        int textY = y + (height - fm.getHeight()) / 2 + fm.getAscent() - 2;
        drawCentered(g, text, x + width / 2, textY);
    }

    // ===== 텍스트 가운데 정렬 =====
    private static void drawCentered(Graphics2D g, String text, int centerX, int y) {
        int textWidth = g.getFontMetrics().stringWidth(text);
        g.drawString(text, centerX - textWidth / 2, y);
    }
}
