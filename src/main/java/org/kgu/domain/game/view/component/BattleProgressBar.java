package org.kgu.domain.game.view.component;

import org.kgu.core.ui.ComponentPainter;
import org.kgu.core.ui.GradientTokens;
import javax.swing.*;
import java.awt.*;
import org.kgu.core.window.UiStyle;

/** Independent of the global textured progress UI, including at compact heights. */
public final class BattleProgressBar extends JProgressBar {
    private final GradientTokens fill;

    public BattleProgressBar(int maximum, GradientTokens fill) {
        super(0, maximum);
        this.fill = fill;
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder());
    }

    @Override protected void paintComponent(Graphics source) {
        Graphics2D g = (Graphics2D) source.create();
        UiStyle.prepare(g);
        ComponentPainter.progress(g, getWidth(), getHeight(), getPercentComplete(), fill);
        g.dispose();
    }
}
