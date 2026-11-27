package org.kgu.domain.story.view;

import org.kgu.core.window.*;
import org.kgu.domain.story.event.EndingGUIEvent;
import org.kgu.domain.story.view.layout.EndingLayout;
import javax.swing.*;
import java.awt.*;

public final class EndingView extends JPanel implements View {
    private final JTextArea summary = new JTextArea();
    private final JLabel status = new JLabel(" ");
    private final JButton next = new UiButton("계속 플레이", GameTheme.BLUE_TEXT);
    public EndingView(EndingGUIEvent event) {
        super(new BorderLayout()); add(new EndingLayout(summary, next, status));
        next.addActionListener(e -> event.finish(this));
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ESCAPE"), "skip");
        getActionMap().put("skip", new AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) { event.finish(EndingView.this); }
        });
    }
    public void display(String text, boolean seen) { summary.setText(text); summary.setCaretPosition(0); status.setText(seen ? "다시 찾아 준 학생에게 · ESC로 돌아가기" : "모든 마을과 카드 수집은 계속 플레이할 수 있습니다."); }
    public void showError(String text) { status.setText(text); }
    @Override protected void paintComponent(Graphics source) { GameTheme.paintBackdrop(source, this, "ending", 0f); }
    @Override public ViewId getViewId() { return ViewId.ENDING; }
    @Override public JComponent getComponent() { return this; }
    @Override public Dimension getViewSize() { return new Dimension(1280, 960); }
}
