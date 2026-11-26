package org.kgu.domain.story.view;

import org.kgu.domain.story.view.layout.DialogueLayout;

import org.kgu.domain.story.event.DialogueGUIEvent;
import org.kgu.core.window.*;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.util.List;

public final class DialogueView extends JPanel implements View {
    private final DialogueGUIEvent event;
    private final Timer animation = new Timer(16, e -> update());
    private BufferedImage portrait;

    public DialogueView(DialogueGUIEvent event) {
        this.event = event; setFocusable(true); setBackground(Color.BLACK);
    }
    public void setPortrait(BufferedImage portrait) { this.portrait = portrait; }
    public void startAnimation() { animation.start(); }
    public void stopAnimation() { animation.stop(); }
    private void update() { event.update(this); }
    @Override protected void paintComponent(Graphics source) {
        DialogueLayout.paint(source, getWidth(), getHeight(), portrait, event);
    }
    @Override public void removeNotify() { stopAnimation(); super.removeNotify(); }
    @Override public ViewId getViewId() { return ViewId.DIALOGUE; }
    @Override public JComponent getComponent() { return this; }
    @Override public Dimension getViewSize() { return new Dimension(1280, 960); }
}
