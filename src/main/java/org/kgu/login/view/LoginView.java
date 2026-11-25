package org.kgu.login.view;

import org.kgu.window.*;
import javax.swing.*;
import java.awt.*;

public final class LoginView extends JPanel implements View {
    private final JTextField playerId = new JTextField();
    private final JButton login = new UiButton("여정 시작하기", GameTheme.TEAL);

    public LoginView() {
        setLayout(new BorderLayout(50, 30)); setBorder(BorderFactory.createEmptyBorder(80, 75, 80, 75));
        JPanel introduction = GameTheme.clear(new GridBagLayout());
        JPanel story = GameTheme.clear(new GridLayout(0, 1, 0, 22));
        story.add(GameTheme.label("A QUIZ ADVENTURE", 18, GameTheme.GOLD));
        story.add(GameTheme.label("졸업을 향한", 45, GameTheme.PAPER)); story.add(GameTheme.label("마지막 여정", 52, GameTheme.PAPER));
        story.add(GameTheme.label("사라진 교수님, 멈춰 버린 졸업.", 21, GameTheme.MUTED));
        story.add(GameTheme.label("당신의 지식으로 구출 경로를 열어 보세요.", 18, GameTheme.MUTED)); introduction.add(story); add(introduction, BorderLayout.CENTER);
        JPanel right = GameTheme.clear(new GridBagLayout()); right.setPreferredSize(new Dimension(420, 500));
        JPanel form = GameTheme.panel(new BorderLayout(0, 28)); form.setPreferredSize(new Dimension(420, 410)); form.setBorder(BorderFactory.createEmptyBorder(35, 30, 35, 30));
        JPanel heading = GameTheme.clear(new GridLayout(2, 1, 0, 12)); heading.add(GameTheme.label("TRAVELER'S RECORD", 16, GameTheme.GOLD)); heading.add(GameTheme.label("탐험가 이름", 29, GameTheme.PAPER)); form.add(heading, BorderLayout.NORTH);
        JPanel input = GameTheme.clear(new BorderLayout(0, 12)); input.add(GameTheme.label("개인 ID", 17, GameTheme.MUTED), BorderLayout.NORTH);
        playerId.setFont(UiStyle.FONT.deriveFont(23f)); playerId.setForeground(GameTheme.PAPER); playerId.setBackground(GameTheme.DARK); playerId.setCaretColor(GameTheme.GOLD); playerId.setPreferredSize(new Dimension(350, 60)); playerId.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(GameTheme.TEAL), BorderFactory.createEmptyBorder(8, 12, 8, 12))); input.add(playerId, BorderLayout.CENTER);
        input.setPreferredSize(new Dimension(350, 102));
        JPanel inputArea = GameTheme.clear(new BorderLayout()); inputArea.add(input, BorderLayout.NORTH); form.add(inputArea);
        login.setPreferredSize(new Dimension(350, 58)); login.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 22f)); form.add(login, BorderLayout.SOUTH); right.add(form); add(right, BorderLayout.EAST);
    }
    public String getPlayerId() { return playerId.getText(); }
    public JTextField getIdField() { return playerId; }
    public JButton getLoginButton() { return login; }
    @Override protected void paintComponent(Graphics source) { Graphics2D g = (Graphics2D)source.create(); UiStyle.prepare(g); GameTheme.backdrop(g, getWidth(), getHeight(), "login", 0.74f); g.dispose(); }
    @Override public ViewId getViewId() { return ViewId.LOGIN; }
    @Override public JComponent getComponent() { return this; }
    @Override public Dimension getViewSize() { return new Dimension(1280, 960); }
}
