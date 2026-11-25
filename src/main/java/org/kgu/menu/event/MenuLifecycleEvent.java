package org.kgu.menu.event;

import org.kgu.menu.view.MenuView;
import org.kgu.window.LifecycleEvent;

import javax.swing.*;

public final class MenuLifecycleEvent implements LifecycleEvent {
    private final MenuView view;
    private final MenuGUIEvent event;

    public MenuLifecycleEvent(MenuView view, MenuGUIEvent event) {
        this.view = view;
        this.event = event;
    }

    private AbstractButton[] buttons() {
        return new AbstractButton[]{view.getGameButton(), view.getCollectionButton(), view.getSettingButton(), view.getLoginButton(), view.getStartButton(), view.getBackButton()};
    }

    @Override
    public void onEnter() {
        view.showHome();
        event.refreshProfile();
        event.refreshPersons();
        for (AbstractButton button : buttons()) button.addActionListener(event);
        view.getCategoryBox().addActionListener(event);
        view.getModeBox().addActionListener(event);
    }

    @Override
    public void onExit() {
        view.stopAnimations();
        for (AbstractButton button : buttons()) button.removeActionListener(event);
        view.getCategoryBox().removeActionListener(event);
        view.getModeBox().removeActionListener(event);
    }
}
