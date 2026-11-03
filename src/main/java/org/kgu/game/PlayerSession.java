package org.kgu.game;

/** 화면 사이에서 사용자 선택값을 보관하는 게임 세션. */
public final class PlayerSession {
    private String playerId = "";
    private String field = "IT";
    private String difficulty = "중";
    private String mode = "연습";
    public String getPlayerId() { return playerId; }
    public void setPlayerId(String playerId) { this.playerId = playerId; }
    public String getField() { return field; }
    public void setField(String field) { this.field = field; }
    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }
}
