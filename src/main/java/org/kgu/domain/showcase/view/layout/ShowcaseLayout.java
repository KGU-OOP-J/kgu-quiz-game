package org.kgu.domain.showcase.view.layout;

import org.kgu.core.ui.ComponentPainter;
import org.kgu.core.window.GameTheme;
import org.kgu.core.window.UiStyle;
import org.kgu.domain.showcase.feature.ShowcaseScene;

import javax.swing.*;
import java.awt.*;

public final class ShowcaseLayout extends JPanel {
    public ShowcaseLayout(JList<ShowcaseScene> scenes) {
        super(new BorderLayout(0, 12));
        setOpaque(false);
        setPreferredSize(new Dimension(420, 510));
        add(listSection(scenes), BorderLayout.CENTER);
    }

    private JScrollPane listSection(JList<ShowcaseScene> scenes) {
        scenes.setFixedCellHeight(36);
        scenes.setFont(UiStyle.FONT.deriveFont(16f));
        scenes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        scenes.setSelectedIndex(0);
        scenes.setBackground(Color.WHITE);
        scenes.setForeground(ComponentPainter.TEXT);
        scenes.setSelectionBackground(new Color(0xDFEEFF));
        scenes.setSelectionForeground(ComponentPainter.TEXT);
        scenes.setCellRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> list, Object value,
                                                                    int index, boolean selected, boolean focused) {
                super.getListCellRendererComponent(list, value, index, selected, focused);
                setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(selected ? ComponentPainter.SELECTED : Color.WHITE),
                    BorderFactory.createEmptyBorder(0, 12, 0, 12)));
                return this;
            }
        });
        JScrollPane scroll = GameTheme.scroll(scenes);
        scroll.setBorder(BorderFactory.createLineBorder(ComponentPainter.BORDER));
        return scroll;
    }
}
