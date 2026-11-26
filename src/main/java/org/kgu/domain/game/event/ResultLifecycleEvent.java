package org.kgu.domain.game.event;

import org.kgu.domain.game.view.ResultView;
import org.kgu.core.window.LifecycleEvent;

public final class ResultLifecycleEvent implements LifecycleEvent {
    private final ResultView view;
    private final ResultGUIEvent event;
    public ResultLifecycleEvent(ResultView view,ResultGUIEvent event){this.view=view;this.event=event;}
    @Override public void onEnter(){event.refresh(view);}
    @Override public void onExit(){view.stopAnimations();}
}
