package org.kgu.domain.battle.feature;

/** Answer index is zero based. Data belongs to the practice question bank. */
public record BattleQuestion(String id, String characterId, String difficulty, String text,
                       String[] options, int answerIndex, String hint, String explanation) {
    public BattleQuestion {
        if(options == null || options.length != 4 || answerIndex < 0 || answerIndex >= 4)
            throw new IllegalArgumentException("BattleQuestion requires four options and a valid answer index");
        options = options.clone();
    }
    @Override public String[] options() { return options.clone(); }
}
