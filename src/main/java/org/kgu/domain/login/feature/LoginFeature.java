package org.kgu.domain.login.feature;

import org.kgu.core.application.PlayerSession;

import java.io.IOException;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * 원본처럼 ID로 로컬 사용자를 선택한다. 비밀번호 인증을 가장하지 않는다.
 */
public final class LoginFeature {
    private final Consumer<PlayerSession> onLogin;

    public LoginFeature(Consumer<PlayerSession> onLogin) {
        this.onLogin = Objects.requireNonNull(onLogin);
    }

    public void login(String playerId) throws IOException {
        PlayerSession playerSession = new PlayerSession(playerId);
        playerSession.saveFactory().open();
        playerSession.saveFactory().progress().syncCollected(playerSession.saveFactory().create().getCollectedCount());
        onLogin.accept(playerSession);
    }
}
