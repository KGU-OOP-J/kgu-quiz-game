package org.kgu.story.feature;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

public final class PrologueFeature {
    public record Passage(String text, boolean emphasis, boolean title) { }
    private final List<Passage> passages;
    private final Runnable onFinished;
    private long startedAt, exitingAt;
    private boolean finished;

    public PrologueFeature(Path source, Runnable onFinished) throws IOException {
        this.onFinished = Objects.requireNonNull(onFinished);
        String text;
        if (Files.isRegularFile(source)) text = Files.readString(source, StandardCharsets.UTF_8);
        else {
            try (var stream = PrologueFeature.class.getResourceAsStream("/org/kgu/story/prologue-dialog.txt")) {
                if (stream == null) throw new IOException("프롤로그 대본을 찾을 수 없습니다: " + source);
                text = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            }
        }
        passages = text.lines().map(line -> {
            boolean title = line.startsWith("#"), emphasis = line.contains("**");
            String clean = line.replaceFirst("^#{1,6}\\s*", "").replace("**", "");
            if (clean.startsWith("- ")) clean = "· " + clean.substring(2);
            return new Passage(clean, emphasis, title);
        }).toList();
        if (passages.stream().allMatch(p -> p.text().isBlank())) throw new IOException("프롤로그 대본이 비어 있습니다.");
    }

    public List<Passage> passages() { return passages; }
    public void start(long now) { startedAt = now; exitingAt = 0; finished = false; }
    public double elapsed(long now) { return Math.max(0, (now - startedAt) / 1_000_000_000.0); }
    public boolean skipAvailable(long now) { return !finished && exitingAt == 0 && elapsed(now) >= 1; }
    public void skip(long now) { if (skipAvailable(now)) requestFinish(now); }
    public void requestFinish(long now) { if (!finished && exitingAt == 0) exitingAt = now; }
    public float fade(long now) { return exitingAt == 0 ? 0 : Math.min(1f, (now - exitingAt) / 550_000_000f); }
    public void tick(long now) {
        if (!finished && exitingAt != 0 && fade(now) >= 1) { finished = true; onFinished.run(); }
    }
}
