package org.kgu.domain.login.view.layout;

import org.kgu.core.window.*;
import org.kgu.core.ui.ComponentPainter;
import javax.swing.*;
import java.awt.*;

public final class LoginLayout extends JPanel {
    public LoginLayout(JTextField playerId, JButton login) {
        setLayout(new BorderLayout(50, 30)); setBorder(BorderFactory.createEmptyBorder(80, 75, 80, 75));
        setOpaque(false);
        add(introductionSection(), BorderLayout.CENTER);
        add(formSection(playerId, login), BorderLayout.EAST);
    }

    private JPanel introductionSection() {
        JPanel introduction = GameTheme.clear(new BorderLayout());
        JPanel caption = GameTheme.lightPanel(new GridLayout(0, 1, 0, 6));
        caption.setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 20));
        JLabel title = GameTheme.label("졸업을 향한 마지막 여정", 23, ComponentPainter.TEXT);
        title.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 23f));
        caption.add(title);
        caption.add(GameTheme.label("사라진 교수님을 찾으러 떠나세요.", 15, ComponentPainter.MUTED));
        JPanel lower = GameTheme.clear(new FlowLayout(FlowLayout.LEFT, 0, 0));
        lower.add(caption);
        introduction.add(lower, BorderLayout.SOUTH);
        return introduction;
    }

    private JPanel formSection(JTextField playerId, JButton login) {
        JPanel form = GameTheme.lightPanel(new BorderLayout(0, 28)); form.setPreferredSize(new Dimension(420, 390)); form.setBorder(BorderFactory.createEmptyBorder(35, 30, 35, 30));
        JPanel right = new JPanel(null) {
            @Override public void doLayout() {
                int height = Math.min(getHeight(), form.getPreferredSize().height);
                int belowTitle = Math.round(LoginLayout.this.getHeight() * .33f) - getY();
                int top = Math.min(getHeight() - height, Math.max(belowTitle, (getHeight() - height) / 2));
                form.setBounds(0, top, getWidth(), height);
            }
        };
        right.setOpaque(false);
        right.setPreferredSize(new Dimension(420, 500));
        JLabel heading = GameTheme.label("학생 로그인", 26, ComponentPainter.TEXT);
        heading.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 26f));
        form.add(heading, BorderLayout.NORTH);
        JPanel input = GameTheme.clear(new BorderLayout(0, 12)); input.add(GameTheme.label("개인 ID", 17, ComponentPainter.MUTED), BorderLayout.NORTH);
        playerId.setFont(UiStyle.FONT.deriveFont(23f)); playerId.putClientProperty("lightSurface", true); playerId.setOpaque(false);
        playerId.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        playerId.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusGained(java.awt.event.FocusEvent e) { playerId.repaint(); }
            @Override public void focusLost(java.awt.event.FocusEvent e) { playerId.repaint(); }
        });
        playerId.setForeground(ComponentPainter.TEXT);
        playerId.setCaretColor(ComponentPainter.MUTED);
        playerId.setPreferredSize(new Dimension(350, 55));
        JPanel fieldSlot = GameTheme.clear(new GridBagLayout());
        GridBagConstraints field = new GridBagConstraints();
        field.weightx = 1;
        field.fill = GridBagConstraints.HORIZONTAL;
        fieldSlot.add(playerId, field);
        input.add(fieldSlot, BorderLayout.CENTER);
        input.add(GameTheme.label("이 이름으로 수집 기록과 진행 상황을 불러옵니다.", 13, ComponentPainter.MUTED), BorderLayout.SOUTH); form.add(input);
        login.setPreferredSize(new Dimension(350, 58)); login.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 22f)); form.add(login, BorderLayout.SOUTH); right.add(form); return right;
    }
}
