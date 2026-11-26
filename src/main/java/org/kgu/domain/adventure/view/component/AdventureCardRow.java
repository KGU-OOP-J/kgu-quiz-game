package org.kgu.domain.adventure.view.component;

import org.kgu.core.window.GameTheme;
import javax.swing.*;
import java.awt.*;

public final class AdventureCardRow extends JPanel {
    private final JPanel surface;
    public AdventureCardRow(JPanel outer, JPanel surface) { super(new BorderLayout()); this.surface = surface; setOpaque(false); setAlignmentX(Component.LEFT_ALIGNMENT); super.add(outer, BorderLayout.CENTER); setMaximumSize(outer.getMaximumSize()); setPreferredSize(outer.getPreferredSize()); }
    @Override public void add(Component component, Object constraints) { if (surface == null) super.add(component, constraints); else surface.add(component, constraints); }
}
