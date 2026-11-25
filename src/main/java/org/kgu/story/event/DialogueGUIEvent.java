package org.kgu.story.event;

import org.kgu.model.Person;
import org.kgu.story.feature.DialogueFeature;
import org.kgu.story.view.DialogueView;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.Optional;
import java.util.function.Function;

public final class DialogueGUIEvent implements KeyEventDispatcher {
    private final DialogueFeature feature;
    private final Function<String, Optional<BufferedImage>> images;
    private Window window;
    private final WindowAdapter focus = new WindowAdapter() {
        @Override public void windowLostFocus(WindowEvent event) { feature.releaseControl(); }
    };
    public DialogueGUIEvent(DialogueFeature feature, Function<String, Optional<BufferedImage>> images) {
        this.feature = feature; this.images = images;
    }
    public void enter(DialogueView view) {
        feature.start(System.nanoTime());
        Person person = feature.person();
        view.setPortrait(org.kgu.window.GameTheme.asset("portrait-" + person.getName()).orElseGet(() -> images.apply("cards/" + person.getName() + "_1").orElse(null)));
        window = javax.swing.SwingUtilities.getWindowAncestor(view);
        if (window != null) window.addWindowFocusListener(focus);
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(this);
        view.startAnimation(); view.requestFocusInWindow();
    }
    public void exit(DialogueView view) {
        view.stopAnimation(); feature.releaseControl();
        KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(this);
        if (window != null) window.removeWindowFocusListener(focus);
        window = null;
    }
    public void update(DialogueView view) {
        if (window != null && !window.isFocused()) feature.releaseControl();
        feature.tick(System.nanoTime()); view.repaint();
    }
    public String speaker() { return feature.person().getName() + (feature.person().getCategory().equals("교수님") ? " 교수님" : ""); }
    public String visibleText() { return feature.visibleText(System.nanoTime()); }
    public double elapsed() { return feature.elapsed(System.nanoTime()); }
    public float skipProgress() { return feature.skipProgress(System.nanoTime()); }
    @Override public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() != KeyEvent.VK_CONTROL) return false;
        if (event.getID() == KeyEvent.KEY_PRESSED) feature.pressControl(System.nanoTime());
        if (event.getID() == KeyEvent.KEY_RELEASED) feature.releaseControl();
        return true;
    }
}
