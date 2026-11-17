package org.kgu.setting.event;

import org.kgu.setting.feature.SettingFeature;
import org.kgu.setting.view.SettingView;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class SettingGUIEvent implements ActionListener {
    private final SettingView view;
    private final SettingFeature settingFeature;

    public SettingGUIEvent(SettingView view, SettingFeature settingFeature) {
        this.view = view;
        this.settingFeature = settingFeature;
    }

    public void refresh() {
        view.display(settingFeature.getSetting().isSoundEnabled());
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        if (event.getSource() == view.getBackButton()) settingFeature.returnToMenu();
        else if (event.getSource() == view.getApplyButton()) {
            try {
                settingFeature.apply(view.isSoundEnabled());
                view.showSaved();
            } catch (IOException e) {
                JOptionPane.showMessageDialog(view, "설정 저장 실패: " + e.getMessage(), "저장 실패", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
