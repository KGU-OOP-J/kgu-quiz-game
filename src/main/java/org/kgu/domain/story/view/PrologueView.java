package org.kgu.domain.story.view;

import org.kgu.domain.story.view.layout.PrologueLayout;

import org.kgu.domain.story.event.PrologueGUIEvent;
import org.kgu.domain.story.feature.PrologueFeature;
import org.kgu.core.window.*;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public final class PrologueView extends JPanel implements View {
    private final PrologueGUIEvent event;
    private final JButton skip = new UiButton("프롤로그 건너뛰기", GameTheme.TEAL);
    private final Timer animation = new Timer(16, e -> update());

    public PrologueView(PrologueGUIEvent event) {
        this.event = event; setLayout(null); setBackground(GameTheme.PAGE);
        skip.setName("prologueSkip"); skip.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 16f));
        skip.setForeground(GameTheme.PAPER); skip.setVisible(false); add(skip);
    }
    public JButton getSkipButton() { return skip; }
    public void startAnimation() { animation.start(); }
    public void stopAnimation() { animation.stop(); }
    private void update() { event.update(this); }
    @Override public void doLayout() { skip.setBounds(Math.max(18, getWidth() - 284), getHeight() - 78, 248, 44); }
    @Override protected void paintComponent(Graphics source) {
        PrologueLayout.paint(source, getWidth(), getHeight(), event);
    }
    @Override public void removeNotify() { stopAnimation(); super.removeNotify(); }
    @Override public ViewId getViewId() { return ViewId.PROLOGUE; }
    @Override public JComponent getComponent() { return this; }
    @Override public Dimension getViewSize() { return new Dimension(1280, 960); }
}
