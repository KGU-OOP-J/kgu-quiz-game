package org.kgu.core.window;

import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.BooleanSupplier;
import java.util.logging.Logger;

public final class GameResources {
    public enum Sound {
        UI_HOVER("ui-hover"), UI_CLICK("ui-click"), UI_CONFIRM("ui-confirm"), UI_CANCEL("ui-cancel"),
        UI_TRANSITION("ui-transition"), CORRECT("quiz-correct"), WRONG("quiz-wrong"), HINT("quiz-hint"),
        ATTACK("battle-attack"), HEAVY_ATTACK("battle-heavy-attack"), COMBO("battle-combo"), FINISHER("battle-finisher"),
        BOSS_ENTRANCE("boss-entrance"), PHASE_TRANSITION("boss-phase-transition"), CARD_FLIP("card-flip"),
        CARD_REVEAL_1("card-reveal-1"), CARD_REVEAL_2("card-reveal-2"), CARD_REVEAL_3("card-reveal-3"),
        COIN("reward-coin"), XP("reward-xp"), LEVEL_UP("reward-level-up"), UNLOCK("progression-unlock"),
        VICTORY("battle-victory"), DEFEAT("battle-defeat");
        private final String file;
        Sound(String file) { this.file = file; }
        public String path() { return "/org/kgu/audio/" + file + ".wav"; }
    }

    private static final Logger LOG = Logger.getLogger(GameResources.class.getName());
    private static final ExecutorService LOADER = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r, "game-resources"); thread.setDaemon(true); return thread;
    });
    private static final Map<String, CompletableFuture<Optional<BufferedImage>>> IMAGES = new ConcurrentHashMap<>();
    private static final Map<Sound, Optional<AudioData>> SOUNDS = new EnumMap<>(Sound.class);
    private static final Set<String> WARNED = ConcurrentHashMap.newKeySet();
    private static final Set<String> BACKGROUNDS = ConcurrentHashMap.newKeySet();
    private static final Set<Clip> CLIPS = ConcurrentHashMap.newKeySet();
    private static volatile BooleanSupplier soundEnabled = () -> false;
    private static final java.util.concurrent.atomic.AtomicLong SOUND_GENERATION = new java.util.concurrent.atomic.AtomicLong();
    private record AudioData(AudioFormat format, byte[] bytes) { }

    private GameResources() { }

    public static void configureSound(BooleanSupplier enabled) { soundEnabled = Objects.requireNonNull(enabled); }

    public static String backgroundPath(String screen) {
        return switch (screen) {
            case "login" -> "/org/kgu/login/main_login.png";
            case "prologue", "dialogue", "result" -> "/org/kgu/result/result_background.png";
            case "study", "review" -> "/images/question-background.png";
            case "menu", "practice-setup", "adventure", "battle" -> "/org/kgu/battle/campus_background.png";
            default -> "/org/kgu/art/scene-" + screen + ".png";
        };
    }

    public static Optional<BufferedImage> background(String screen) {
        String path = backgroundPath(screen);
        var loading = loadImage(path);
        if (BACKGROUNDS.add(path)) loading.thenAccept(result -> {
            if (result.isEmpty()) warn("background", screen.toUpperCase(Locale.ROOT), path);
        });
        return loading.getNow(Optional.empty());
    }

    public static Optional<BufferedImage> image(String path) { return loadImage(path).getNow(Optional.empty()); }
    public static void preloadImage(String path) { loadImage(path).join(); }

    private static CompletableFuture<Optional<BufferedImage>> loadImage(String path) {
        return IMAGES.computeIfAbsent(path, p -> {
            CompletableFuture<Optional<BufferedImage>> loading = CompletableFuture.supplyAsync(() -> {
            try (InputStream stream = GameResources.class.getResourceAsStream(p)) {
                BufferedImage image = stream == null ? null : ImageIO.read(stream);
                if (image != null) return Optional.of(image);
            } catch (IOException | RuntimeException ignored) { }
            return Optional.empty();
            }, LOADER);
            loading.thenRun(() -> SwingUtilities.invokeLater(GameResources::repaintWindows));
            return loading;
        });
    }

    private static void repaintWindows() {
        for (java.awt.Window window : java.awt.Window.getWindows()) if (window.isShowing()) window.repaint();
    }

    public static void play(Sound sound) {
        if (!soundEnabled.getAsBoolean()) return;
        long generation = SOUND_GENERATION.get();
        LOADER.execute(() -> {
            if (generation != SOUND_GENERATION.get()) return;
            Optional<AudioData> data = SOUNDS.computeIfAbsent(sound, GameResources::loadSound);
            if (data.isEmpty() || !soundEnabled.getAsBoolean() || generation != SOUND_GENERATION.get() || CLIPS.size() >= 12) return;
            Clip clip = null;
            try {
                clip = AudioSystem.getClip();
                Clip playing = clip;
                clip.addLineListener(event -> {
                    if (event.getType() == LineEvent.Type.STOP) { CLIPS.remove(playing); playing.close(); }
                });
                AudioData value = data.get();
                clip.open(value.format(), value.bytes(), 0, value.bytes().length);
                if (generation != SOUND_GENERATION.get()) { clip.close(); return; }
                CLIPS.add(clip); clip.start();
            } catch (LineUnavailableException | IllegalArgumentException ex) {
                if (clip != null) clip.close();
                SOUNDS.put(sound, Optional.empty());
                warn("sound", sound.name(), sound.path());
            }
        });
    }

    private static Optional<AudioData> loadSound(Sound sound) {
        try (InputStream stream = GameResources.class.getResourceAsStream(sound.path())) {
            if (stream != null) try (AudioInputStream audio = AudioSystem.getAudioInputStream(new BufferedInputStream(stream))) {
                return Optional.of(new AudioData(audio.getFormat(), audio.readAllBytes()));
            }
        } catch (IOException | UnsupportedAudioFileException | RuntimeException ignored) { }
        warn("sound", sound.name(), sound.path()); return Optional.empty();
    }

    private static void warn(String type, String id, String path) {
        if (WARNED.add(type + path)) LOG.warning("[WARNING] Missing or invalid " + type + " resource: "
            + (type.equals("background") ? "screen=" : "event=") + id + ", file="
            + path.substring(path.lastIndexOf('/') + 1) + ", path=" + path);
    }

    public static void stopSounds() {
        SOUND_GENERATION.incrementAndGet();
        for (Clip clip : CLIPS) { clip.stop(); clip.close(); }
        CLIPS.clear();
    }
}
