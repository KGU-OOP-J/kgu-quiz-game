package org.kgu.domain.story.view.layout;

import org.kgu.core.window.*;
import org.kgu.core.ui.ComponentPainter;
import javax.swing.*;
import java.awt.*;

public final class EndingLayout extends JPanel {
    public EndingLayout(JTextArea summary, JButton next, JLabel status) {
        super(new GridBagLayout()); setOpaque(false);
        JPanel body = GameTheme.lightPanel(new BorderLayout(24, 24));
        body.setPreferredSize(new Dimension(800, 650)); body.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));
        JPanel heading = GameTheme.clear(new GridLayout(0, 1, 0, 14));
        JLabel title = GameTheme.label("GAME CLEAR", 44, GameTheme.BLUE_TEXT);
        title.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 44f));
        heading.add(title); heading.add(GameTheme.label("교수님 구출 작전 완료", 28, ComponentPainter.TEXT));
        heading.add(GameTheme.label("되찾은 기억과 함께, 졸업을 향한 여정은 계속됩니다.", 17, ComponentPainter.MUTED));
        body.add(heading, BorderLayout.NORTH);
        summary.setEditable(false); summary.setOpaque(false); summary.setLineWrap(true); summary.setWrapStyleWord(true);
        summary.setForeground(ComponentPainter.TEXT); summary.setFont(UiStyle.FONT.deriveFont(20f));
        body.add(GameTheme.scroll(summary), BorderLayout.CENTER);
        JPanel footer = GameTheme.clear(new BorderLayout(0, 18));
        JTextArea credits = new JTextArea("WHO’S NEXT? · KGU-OOP-J\n제작 참여 · Git 기록 기준\nTaeho1218 · Metacode0 · choonarine / Choo Narine\nMinSu · jaypower199 · jyan0928 · LMSOO333");
        credits.setEditable(false); credits.setOpaque(false); credits.setForeground(ComponentPainter.MUTED);
        credits.setFont(UiStyle.FONT.deriveFont(15f)); credits.setLineWrap(true); credits.setWrapStyleWord(true);
        footer.add(credits, BorderLayout.NORTH); footer.add(status, BorderLayout.CENTER);
        next.setPreferredSize(new Dimension(0, 54)); footer.add(next, BorderLayout.SOUTH); body.add(footer, BorderLayout.SOUTH); add(body);
    }
}
