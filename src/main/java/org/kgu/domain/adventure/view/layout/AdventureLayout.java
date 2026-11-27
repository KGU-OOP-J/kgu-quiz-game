package org.kgu.domain.adventure.view.layout;

import javax.swing.*;
import java.awt.*;

public final class AdventureLayout extends JPanel {
    public AdventureLayout(JComponent header, JComponent navigation, JComponent pages, JComponent notice) {
        super(new BorderLayout(24, 16));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(24, 32, 20, 32));
        add(header, BorderLayout.NORTH);
        add(navigation, BorderLayout.WEST);
        add(pages, BorderLayout.CENTER);
        add(notice, BorderLayout.SOUTH);
    }
}
