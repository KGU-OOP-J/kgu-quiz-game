package org.kgu.domain.setting.feature;

import org.kgu.core.factory.SettingFactory;
import org.kgu.core.model.Setting;
import org.kgu.core.window.ViewId;

import java.io.IOException;
import java.util.Objects;
import java.util.function.Consumer;

public final class SettingFeature {
    private final SettingFactory settingFactory;
    private final Consumer<ViewId> navigate;

    public SettingFeature(SettingFactory settingFactory, Consumer<ViewId> navigate) {
        this.settingFactory = Objects.requireNonNull(settingFactory);
        this.navigate = Objects.requireNonNull(navigate);
    }

    public Setting getSetting() {
        return settingFactory.create();
    }

    public void apply(boolean soundEnabled) throws IOException {
        settingFactory.saveSoundEnabled(soundEnabled);
    }

    public void returnToMenu() {
        navigate.accept(ViewId.MENU);
    }
}
