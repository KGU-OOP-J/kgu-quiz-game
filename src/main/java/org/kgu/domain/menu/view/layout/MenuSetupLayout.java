package org.kgu.domain.menu.view.layout;

import org.kgu.core.ui.ComponentPainter;
import org.kgu.core.window.GameTheme;
import org.kgu.core.window.UiStyle;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public final class MenuSetupLayout extends JPanel {
    public MenuSetupLayout(List<JComboBox<?>> inputs, JLabel personLabel, JLabel gate, JButton back, JButton start) {
        super(new GridBagLayout());
        setBackground(GameTheme.PAGE);
        add(formContainer(inputs, personLabel, gate, back, start));
    }

    private static JPanel formContainer(List<JComboBox<?>> inputs, JLabel personLabel, JLabel gate,
                                        JButton back, JButton start) {
        JPanel frame = GameTheme.lightPanel(new BorderLayout(24, 28));
        frame.setBorder(BorderFactory.createEmptyBorder(36, 40, 32, 40));
        frame.setPreferredSize(new Dimension(760, 570));
        frame.add(GameTheme.label("게임 준비", 32, ComponentPainter.TEXT), BorderLayout.NORTH);
        frame.add(fieldsSection(inputs, personLabel), BorderLayout.CENTER);
        frame.add(actionsSection(gate, back, start), BorderLayout.SOUTH);
        return frame;
    }

    private static JPanel fieldsSection(List<JComboBox<?>> inputs, JLabel personLabel) {
        JPanel fields = GameTheme.clear(new GridLayout(4, 2, 20, 18));
        List<JLabel> labels = List.of(GameTheme.label("분야 선택", 19, ComponentPainter.TEXT),
            GameTheme.label("난이도 선택", 19, ComponentPainter.TEXT),
            GameTheme.label("게임 모드", 19, ComponentPainter.TEXT), personLabel);
        personLabel.setForeground(ComponentPainter.TEXT);
        for (int i = 0; i < inputs.size(); i++) {
            JComboBox<?> box = inputs.get(i);
            box.setFont(UiStyle.FONT.deriveFont(19f));
            fields.add(labels.get(i));
            fields.add(box);
        }
        return fields;
    }

    private static JPanel actionsSection(JLabel gate, JButton back, JButton start) {
        JPanel footer = GameTheme.clear(new BorderLayout(0, 20));
        gate.setHorizontalAlignment(SwingConstants.CENTER);
        gate.setForeground(ComponentPainter.MUTED);
        footer.add(gate, BorderLayout.NORTH);
        JPanel actions = GameTheme.clear(new GridLayout(1, 2, 20, 0));
        back.setPreferredSize(new Dimension(220, 54));
        actions.add(back);
        actions.add(start);
        footer.add(actions, BorderLayout.SOUTH);
        return footer;
    }
    @Override protected void paintComponent(Graphics source) {
        GameTheme.paintBackdrop(source, this, "practice-setup", 0.18f);
    }
}
