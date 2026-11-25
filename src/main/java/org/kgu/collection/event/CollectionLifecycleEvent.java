package org.kgu.collection.event;

import org.kgu.collection.view.CollectionView;
import org.kgu.window.LifecycleEvent;

public final class CollectionLifecycleEvent implements LifecycleEvent {
    private final CollectionView view;
    private final CollectionGUIEvent event;

    public CollectionLifecycleEvent(CollectionView view, CollectionGUIEvent event) {
        this.view = view;
        this.event = event;
    }

    @Override
    public void onEnter() {
        event.refresh(view);
        view.showMap();
    }
    @Override public void onExit() { view.stopAnimations(); }
}
