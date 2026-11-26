package org.kgu.domain.setting.event;

import org.kgu.domain.setting.view.SettingView;
import org.kgu.core.window.LifecycleEvent;

public final class SettingLifecycleEvent implements LifecycleEvent {
    private final SettingView view;
    private final SettingGUIEvent event;

    public SettingLifecycleEvent(SettingView view, SettingGUIEvent event) {
        this.view = view;
        this.event = event;
    }

    @Override
    public void onEnter() {
        event.refresh();

        view.getApplyButton().addActionListener(event);
        view.getBackButton().addActionListener(event);
    }

    @Override
    public void onExit() {
        view.getApplyButton().removeActionListener(event);
        view.getBackButton().removeActionListener(event);
    }
}
