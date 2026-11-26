package org.kgu.domain.game.event;

import org.kgu.domain.game.view.QuestionView;
import org.kgu.core.window.LifecycleEvent;

public final class QuestionLifecycleEvent implements LifecycleEvent {
    private final QuestionView view;
    public QuestionLifecycleEvent(QuestionView view){this.view=view;}
    @Override public void onEnter(){view.display();}
    @Override public void onExit(){view.stopAnimations();}
}
