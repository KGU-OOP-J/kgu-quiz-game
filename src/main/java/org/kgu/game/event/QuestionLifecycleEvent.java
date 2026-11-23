package org.kgu.game.event;

import org.kgu.game.view.QuestionView;
import org.kgu.window.LifecycleEvent;

public final class QuestionLifecycleEvent implements LifecycleEvent {
    private final QuestionView view;
    public QuestionLifecycleEvent(QuestionView view){this.view=view;}
    @Override public void onEnter(){view.display();}
    @Override public void onExit(){view.stopAnimations();}
}
