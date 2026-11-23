package org.kgu.application;

public enum Difficulty {
    LOW("하"),
    MEDIUM("중"),
    HIGH("상");

    private final String label;
    Difficulty(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
    public int getStage() {
        return ordinal() + 1;
    }

    public static Difficulty fromLabel(String label) {
        for (Difficulty difficulty : values()) {
            if (difficulty.label.equals(label)) { return difficulty; }
        }
        throw new IllegalArgumentException("알 수 없는 난이도: " + label);
    }

    @Override public String toString() {
        return label;
    }
}
