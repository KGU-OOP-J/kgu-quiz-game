package org.kgu.game.event;

import org.kgu.game.view.ResultView;
import org.kgu.window.LifecycleEvent;

public final class ResultLifecycleEvent implements LifecycleEvent {
    private final ResultView view;
    private final ResultGUIEvent event;
    public ResultLifecycleEvent(ResultView view,ResultGUIEvent event){this.view=view;this.event=event;}
    @Override public void onEnter(){event.refresh(view);}
}
