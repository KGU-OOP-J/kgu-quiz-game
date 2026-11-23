package org.kgu.battle;

/** Collection and battle share one stable character ID. */
public record BattleCharacter(String id, String name, String category, String imagePath) {}
