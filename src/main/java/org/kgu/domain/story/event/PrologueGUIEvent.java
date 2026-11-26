package org.kgu.domain.story.event;

import org.kgu.domain.story.feature.PrologueFeature;
import org.kgu.domain.story.view.PrologueView;
import java.awt.event.*;
import java.util.List;

public final class PrologueGUIEvent implements ActionListener {
    private final PrologueFeature feature;
    public PrologueGUIEvent(PrologueFeature feature) { this.feature = feature; }
    public List<PrologueFeature.Passage> passages() { return feature.passages(); }
    public double elapsed() { return feature.elapsed(System.nanoTime()); }
    public float fade() { return feature.fade(System.nanoTime()); }
    public void enter(PrologueView view) {
        feature.start(System.nanoTime());
        view.getSkipButton().setVisible(false); view.getSkipButton().setEnabled(false);
        view.getSkipButton().addActionListener(this); view.startAnimation();
    }
    public void exit(PrologueView view) { view.stopAnimation(); view.getSkipButton().removeActionListener(this); }
    public void update(PrologueView view) {
        long now = System.nanoTime(); boolean available = feature.skipAvailable(now);
        view.getSkipButton().setVisible(available); view.getSkipButton().setEnabled(available);
        feature.tick(now); view.repaint();
    }
    public void reachedEnd() { feature.requestFinish(System.nanoTime()); }
    @Override public void actionPerformed(ActionEvent event) { feature.skip(System.nanoTime()); }
}
