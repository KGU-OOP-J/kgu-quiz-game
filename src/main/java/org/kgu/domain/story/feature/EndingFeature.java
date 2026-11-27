package org.kgu.domain.story.feature;

import org.kgu.domain.game.feature.GameFeature;
import org.kgu.core.progression.PlayerProgress;

public final class EndingFeature {
    private final GameFeature game;
    public EndingFeature(GameFeature game) { this.game = game; }
    public String summary() {
        PlayerProgress profile = game.profile();
        return game.playerId() + " 학생의 여정\n\nLv." + profile.level()
            + " · 수집한 동료 " + game.ownedCards().size() + " / 80"
            + " · 수집률 " + game.ownedCards().size() * 100 / 80 + "%\n"
            + "보스 격파 기록 " + profile.bossWins() + " · 모은 별 " + profile.totalStars()
            + "\n" + java.util.List.of("과학자", "한국사", "아이돌", "교수님").stream()
                .map(category -> category + " " + (profile.keys("stage.boss." + category + "|").isEmpty() ? "미완료" : "격파"))
                .collect(java.util.stream.Collectors.joining(" · "))
            + "\n최초 구출 " + profile.text("ending.firstClear");
    }
    public boolean seen() { return game.profile().flag("ending.seen"); }
    public void confirm() throws java.io.IOException { game.progress().recordEnding(); }
    public void continuePlaying() { game.returnToMenu(); }
}
