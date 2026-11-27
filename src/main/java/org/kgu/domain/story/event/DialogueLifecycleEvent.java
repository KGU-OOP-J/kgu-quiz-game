package org.kgu.domain.story.event;

import org.kgu.domain.story.view.DialogueView;
import org.kgu.core.window.LifecycleEvent;

public final class DialogueLifecycleEvent implements LifecycleEvent {
    private final DialogueView view;
    private final DialogueGUIEvent event;
    public DialogueLifecycleEvent(DialogueView view, DialogueGUIEvent event) { this.view = view; this.event = event; }
    @Override public void onEnter() { event.enter(view); }
    @Override public void onExit() { event.exit(view); }
}
