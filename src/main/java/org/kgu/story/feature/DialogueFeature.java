package org.kgu.story.feature;

import org.kgu.model.Person;
import java.util.*;
import java.util.function.Supplier;

public final class DialogueFeature {
    private final Supplier<Person> source;
    private final Runnable onFinished;
    private Person person;
    private List<String> script = List.of();
    private int line;
    private long startedAt, lineStartedAt, controlPressedAt;
    private boolean finished;

    public DialogueFeature(Supplier<Person> source, Runnable onFinished) {
        this.source = Objects.requireNonNull(source); this.onFinished = Objects.requireNonNull(onFinished);
    }
    public void start(long now) {
        person = source.get(); script = person.getDialogue();
        if (!person.hasDialogue() || script.isEmpty()) throw new IllegalStateException("재생할 대사가 없습니다.");
        line = 0; startedAt = now; lineStartedAt = now + 950_000_000L; controlPressedAt = 0; finished = false;
    }
    public Person person() { return person; }
    public double elapsed(long now) { return Math.max(0, (now - startedAt) / 1_000_000_000.0); }
    public String visibleText(long now) {
        if (script.isEmpty()) return "";
        int[] points = script.get(Math.min(line, script.size() - 1)).codePoints().toArray();
        int count = Math.min(points.length, (int)Math.max(0, (now - lineStartedAt) / 42_000_000L));
        return new String(points, 0, count);
    }
    public void pressControl(long now) { if (!finished && controlPressedAt == 0) controlPressedAt = now; }
    public void releaseControl() { controlPressedAt = 0; }
    public float skipProgress(long now) { return controlPressedAt == 0 ? 0 : Math.min(1f, Math.max(0, (now - controlPressedAt) / 3_000_000_000f)); }
    public void tick(long now) {
        if (finished) return;
        if (skipProgress(now) >= 1) { finish(); return; }
        int count = script.get(line).codePointCount(0, script.get(line).length());
        long duration = count * 42_000_000L + Math.max(1_800_000_000L, Math.min(4_000_000_000L, count * 15_000_000L));
        if (now - lineStartedAt >= duration) {
            line++;
            if (line == script.size()) finish(); else lineStartedAt = now;
        }
    }
    private void finish() { if (!finished) { finished = true; controlPressedAt = 0; onFinished.run(); } }
}
