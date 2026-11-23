package org.kgu.application;

import org.kgu.factory.SaveFactory;
import java.util.Objects;

public record PlayerSession(String playerId, SaveFactory saveFactory) {
    public PlayerSession(String playerId) {
        this(playerId, SaveFactory.forPlayer(playerId));
    }

    public PlayerSession(String playerId, SaveFactory saveFactory) {
        this.playerId = Objects.requireNonNull(playerId).trim();

        if (this.playerId.isEmpty()) {
            throw new IllegalArgumentException("개인 ID를 입력해 주세요.");
        }

        this.saveFactory = Objects.requireNonNull(saveFactory);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof PlayerSession && playerId.equals(((PlayerSession) other).playerId);
    }

    @Override
    public int hashCode() {
        return playerId.hashCode();
    }
}
