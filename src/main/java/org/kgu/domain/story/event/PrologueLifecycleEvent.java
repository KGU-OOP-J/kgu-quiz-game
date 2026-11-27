package org.kgu.domain.story.event;

import org.kgu.domain.story.view.PrologueView;
import org.kgu.core.window.LifecycleEvent;

public final class PrologueLifecycleEvent implements LifecycleEvent {
    private final PrologueView view;
    private final PrologueGUIEvent event;
    public PrologueLifecycleEvent(PrologueView view, PrologueGUIEvent event) { this.view = view; this.event = event; }
    @Override public void onEnter() { event.enter(view); }
    @Override public void onExit() { event.exit(view); }
}
