package org.kgu.domain.menu.view.layout;

import org.kgu.core.window.GameTheme;
import org.kgu.core.ui.ComponentPainter;
import org.kgu.core.window.UiButton;
import org.kgu.core.window.UiStyle;
import org.kgu.domain.menu.view.component.MenuCard;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public final class MenuHomeLayout extends JPanel {
    private final JButton more = new UiButton("더 보기", GameTheme.TEAL);
    private final JPanel otherActions = GameTheme.clear(new FlowLayout(FlowLayout.RIGHT, 8, 0));

    public MenuHomeLayout(JLabel welcome, List<JLabel> progress, JLabel goal, List<MenuCard> cards,
                          JButton settings, JButton tutorial, JButton login) {
        super(new BorderLayout(24, 24));
        setBackground(new Color(0xEDF1F6));
        setBorder(BorderFactory.createEmptyBorder(45, 65, 40, 65));
        add(headerSection(welcome, progress, goal), BorderLayout.NORTH);
        add(cardContainer(cards), BorderLayout.CENTER);
        for (JButton action : List.of(settings, tutorial, login)) {
            action.setPreferredSize(new Dimension(144, 40));
            otherActions.add(action);
        }
        otherActions.setVisible(false);
        more.addActionListener(e -> {
            boolean expanded = !otherActions.isVisible();
            otherActions.setVisible(expanded);
            more.setText(expanded ? "접기" : "더 보기");
            revalidate();
            repaint();
        });
    }

    private JPanel headerSection(JLabel welcome, List<JLabel> progress, JLabel goal) {
        JPanel header = GameTheme.clear(new BorderLayout(0, 16));
        JPanel introduction = GameTheme.clear(new GridLayout(0, 1, 0, 8));
        JLabel title = GameTheme.label("WHO’S NEXT?", 32, ComponentPainter.TEXT);
        title.setFont(title.getFont().deriveFont(Font.BOLD));
        introduction.add(title);
        introduction.add(welcome);
        JPanel top = GameTheme.clear(new BorderLayout());
        top.add(introduction, BorderLayout.CENTER);
        JPanel controls = GameTheme.clear(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        more.putClientProperty("secondaryAction", true);
        more.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 15f));
        more.setPreferredSize(new Dimension(112, 40));
        controls.add(more);
        top.add(controls, BorderLayout.EAST);
        top.add(otherActions, BorderLayout.SOUTH);
        header.add(top, BorderLayout.NORTH);
        JPanel metrics = GameTheme.clear(new GridLayout(1, 4, 12, 0));
        var labels = List.of("레벨", "경험치 (XP)", "코인", "별");
        for (int i = 0; i < progress.size(); i++) {
            JPanel metric = GameTheme.lightPanel(new GridLayout(2, 1, 0, 4));
            metric.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
            metric.add(GameTheme.label(labels.get(i), 14, ComponentPainter.MUTED));
            metric.add(progress.get(i));
            metrics.add(metric);
        }
        header.add(metrics, BorderLayout.CENTER);
        header.add(goal, BorderLayout.SOUTH);
        return header;
    }

    private static JPanel cardContainer(List<MenuCard> cards) {
        JPanel panel = GameTheme.clear(new GridLayout(1, 3, 24, 0));
        cards.forEach(panel::add);
        return panel;
    }
    @Override protected void paintComponent(Graphics source) {
        GameTheme.paintBackdrop(source, this, "menu", 0.53f);
    }
}
