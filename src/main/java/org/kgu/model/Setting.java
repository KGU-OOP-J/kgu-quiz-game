package org.kgu.model;

/**
 * 프로토타입의 사용 설정 model. 현재는 효과음 설정만 있음.
 */
public final class Setting {
    private boolean soundEnabled = true;

    public boolean isSoundEnabled() {
        return soundEnabled;
    }

    public void setSoundEnabled(boolean soundEnabled) {
        this.soundEnabled = soundEnabled;
    }
}
