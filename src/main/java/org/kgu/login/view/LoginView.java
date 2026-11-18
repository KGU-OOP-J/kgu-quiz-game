package org.kgu.login.view;

import org.kgu.window.UiButton;
import org.kgu.window.UiStyle;
import org.kgu.window.View;
import org.kgu.window.ViewId;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

/** Design-only login view. Events and login logic remain outside this class. */
public final class LoginView extends JPanel implements View {
    private static final int WIDTH = 1280, HEIGHT = 960;
    private final Image campus = new ImageIcon(LoginView.class.getResource("/org/kgu/battle/campus_background.jpg")).getImage();
    private final JTextField playerId = new JTextField();
    private final UiButton login = new UiButton("게임 시작", new Color(31, 133, 240));

    public LoginView() {
        setLayout(null); setOpaque(false);
        playerId.setBounds(404, 443, 472, 66);
        playerId.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 21f));
        playerId.setBorder(new CompoundBorder(new LineBorder(new Color(112, 163, 215), 2, true), new EmptyBorder(3, 18, 3, 18)));
        add(playerId);
        login.setBounds(404, 558, 472, 78);
        login.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 29f));
        login.setForeground(Color.WHITE); add(login);
    }
    public String getPlayerId() { return playerId.getText(); }
    public JTextField getIdField() { return playerId; }
    public JButton getLoginButton() { return login; }
    @Override public ViewId getViewId() { return ViewId.LOGIN; }
    @Override public JComponent getComponent() { return this; }
    @Override public Dimension getViewSize() { return new Dimension(WIDTH, HEIGHT); }

    @Override protected void paintComponent(Graphics source) {
        super.paintComponent(source); Graphics2D g = (Graphics2D) source.create(); UiStyle.prepare(g);
        g.drawImage(campus, 0, 0, getWidth(), getHeight(), null);
        glass(g, 310, 150, 660, 550, 40);
        g.setColor(new Color(24, 117, 220)); g.fillRoundRect(500, 191, 280, 48, 28, 28);
        g.setColor(Color.WHITE); g.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 18f)); UiStyle.center(g, "PERSON QUIZ", 640, 223);
        g.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 45f)); g.setColor(UiStyle.NAVY); UiStyle.center(g, "교수님 구출 작전", 640, 318);
        g.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 20f)); g.setColor(new Color(45, 81, 124)); UiStyle.center(g, "개인 ID를 입력하고 게임을 시작하세요.", 640, 368);
        g.setColor(UiStyle.NAVY); g.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 20f)); g.drawString("개인 ID", 404, 420);
        g.setFont(UiStyle.FONT.deriveFont(15f)); g.setColor(new Color(65, 105, 145)); g.drawString("입력한 ID로 카드 수집 기록이 저장됩니다.", 423, 675);
        g.dispose();
    }
    private static void glass(Graphics2D g, int x, int y, int w, int h, int arc) {
        g.setPaint(new GradientPaint(x, y, new Color(255,255,255,244), x, y+h, new Color(220,239,255,232))); g.fillRoundRect(x,y,w,h,arc,arc);
        g.setColor(new Color(255,255,255,230)); g.setStroke(new BasicStroke(2f)); g.drawRoundRect(x,y,w,h,arc,arc);
    }
}
