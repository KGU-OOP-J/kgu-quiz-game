package org.kgu.menu.event;

import org.kgu.menu.feature.MenuFeature;
import org.kgu.menu.view.MenuView;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public final class MenuGUIEvent implements ActionListener {
    private final MenuView view;
    private final MenuFeature menuFeature;

    public MenuGUIEvent(MenuView view, MenuFeature menuFeature) {
        this.view = view;
        this.menuFeature = menuFeature;
    }

    public void refreshPersons() {
        view.setPersons(menuFeature.getCandidates(view.getCategory()));
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        Object source = event.getSource();
        if (source == view.getGameButton()) {
            refreshPersons();
            view.showSetup();
        } else if (source == view.getCollectionButton()) menuFeature.openCollection();
        else if (source == view.getSettingButton()) menuFeature.openSetting();
        else if (source == view.getBackButton()) view.showHome();
        else if (source == view.getCategoryBox()) refreshPersons();
        else if (source == view.getModeBox()) view.updateMode();
        else if (source == view.getStartButton()) {
            try {
                menuFeature.start(view.getCategory(), view.getDifficulty(), view.getMode(), view.getSelectedPerson());
            } catch (IllegalArgumentException e) {
                JOptionPane.showMessageDialog(view, e.getMessage(), "게임 준비", JOptionPane.WARNING_MESSAGE);
            }
        }
    }
}
