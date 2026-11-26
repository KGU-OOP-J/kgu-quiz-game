package org.kgu.domain.game.view.component;

import org.kgu.core.window.UiStyle;
import org.kgu.core.ui.GradientTokens;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class VictoryHeader extends JPanel {
    public enum HeaderEffect { NONE, VICTORY, DEFEAT }

    private static final int WIDTH = 1140;
    private static final int HEIGHT = 168;
    private static final Color FRAME = new Color(0x95D1FA);
    private static final Color DEFEAT_TEXT = new Color(185, 191, 202);
    private static final BasicStroke FRAME_STROKE = new BasicStroke(1.5f);
    private static final Paint PANEL_PAINT = GradientTokens.HEADER.paint(0, HEIGHT);

    private final JLabel verdictLabel;
    private final Timer timer = new Timer(25, e -> advance(System.nanoTime()));
    private final Random random = new Random();
    private final Fireworks fireworks = new Fireworks(random);
    private final Storm storm = new Storm(random);
    private final RoundRectangle2D.Double frame = new RoundRectangle2D.Double();
    private String sessionId;
    private HeaderEffect effect = HeaderEffect.NONE;
    private long startedAt;
    private double elapsed;
    private Font defeatFont;

    public VictoryHeader(JLabel title, JLabel verdict, JLabel score) {
        verdictLabel = verdict;
        setLayout(new GridLayout(3, 1, 0, 0));
        setOpaque(false);
        setPreferredSize(new Dimension(0, 150));
        add(title);
        add(verdict);
        add(score);
    }

    public void showEffect(String id, HeaderEffect nextEffect) {
        if (id.equals(sessionId) && effect == nextEffect) return;
        stop();
        sessionId = id;
        effect = nextEffect;
        if (effect == HeaderEffect.NONE) return;
        if (effect == HeaderEffect.VICTORY) fireworks.start();
        else storm.start();
        startedAt = System.nanoTime();
        verdictLabel.setVisible(effect != HeaderEffect.DEFEAT);
        timer.start();
        repaint();
    }

    public void stop() {
        timer.stop();
        sessionId = null;
        elapsed = 0;
        effect = HeaderEffect.NONE;
        fireworks.clear();
        storm.clear();
        verdictLabel.setVisible(true);
        repaint();
    }

    private void advance(long now) {
        if (effect == HeaderEffect.NONE) return;
        elapsed = (now - startedAt) / 1_000_000_000.0;
        if (effect == HeaderEffect.VICTORY) fireworks.advance(elapsed);
        else storm.advance(elapsed);
        repaint();
    }

    @Override protected void paintComponent(Graphics source) {
        super.paintComponent(source);
        int w = getWidth(), h = getHeight();
        if (w < 10 || h < 10) return;
        Graphics2D g = (Graphics2D) source.create();
        UiStyle.prepare(g);
        g.scale((double) w / WIDTH, (double) h / HEIGHT);
        frame.setRoundRect(2, 2, WIDTH - 4, HEIGHT - 4, 24, 24);
        g.setPaint(PANEL_PAINT);
        g.fill(frame);
        if (effect != HeaderEffect.NONE) {
            Graphics2D scene = (Graphics2D) g.create();
            scene.clip(frame);
            if (effect == HeaderEffect.VICTORY) fireworks.paint(scene, elapsed);
            else storm.paint(scene, elapsed);
            scene.dispose();
        }
        g.setColor(FRAME);
        g.setStroke(FRAME_STROKE);
        g.draw(frame);
        g.dispose();
        if (effect == HeaderEffect.DEFEAT) paintDefeatVerdict(source);
    }

    private void paintDefeatVerdict(Graphics source) {
        Graphics2D g = (Graphics2D) source.create();
        UiStyle.prepare(g);
        Rectangle row = verdictLabel.getBounds();
        g.clip(row);
        double remaining = 1 - Math.clamp(elapsed / 1.3, 0.0, 1.0);
        float emphasis = (float) (remaining * remaining * remaining);
        float fontSize = Math.min(row.height * .9f, verdictLabel.getFont().getSize2D() * (.96f + .44f * emphasis));
        if (fontSize > 0) {
            if (defeatFont == null || defeatFont.getSize2D() != fontSize) {
                defeatFont = verdictLabel.getFont().deriveFont(Font.BOLD, fontSize);
            }
            g.setFont(defeatFont);
            g.setColor(emphasis == 0 ? DEFEAT_TEXT : new Color(185 + Math.round(70 * emphasis),
                    191 - Math.round(91 * emphasis), 202 - Math.round(87 * emphasis)));
            var metrics = g.getFontMetrics();
            String text = verdictLabel.getText();
            g.drawString(text, row.x + (row.width - metrics.stringWidth(text)) / 2,
                    row.y + (row.height - metrics.getHeight()) / 2 + metrics.getAscent());
        }
        g.dispose();
    }

    @Override public void removeNotify() {
        stop();
        super.removeNotify();
    }

    private static AlphaComposite[] opacitySteps() {
        var levels = new AlphaComposite[65];
        for (int i = 0; i < levels.length; i++) levels[i] = AlphaComposite.SrcOver.derive(i / 64f);
        return levels;
    }

    private static final AlphaComposite[] OPACITY = opacitySteps();

    private static void opacity(Graphics2D g, double strength) {
        g.setComposite(OPACITY[(int) Math.round(64 * Math.clamp(strength, 0.0, 1.0))]);
    }

    private static RadialGradientPaint glow(float x, float y, float radius, Color color) {
        return new RadialGradientPaint(x, y, radius, new float[]{0, .35f, 1}, new Color[]{color,
                new Color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha() / 3),
                new Color(color.getRed(), color.getGreen(), color.getBlue(), 0)});
    }

    private static final class Fireworks {
        private static final Color[] COLORS = {new Color(255, 201, 104), new Color(255, 230, 174),
                new Color(183, 220, 255)};
        private static final Color WHITE_GOLD = new Color(0xEFF7FF);
        private static final Paint VERDICT_LIGHT = glow(WIDTH / 2f, 87, 186, new Color(149, 209, 250, 40));
        private static final BasicStroke TRAIL = new BasicStroke(.95f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        private final Random random;
        private final List<Burst> bursts = new ArrayList<>();
        private final double[] next = new double[2];
        private final Line2D.Double trail = new Line2D.Double();
        private final Ellipse2D.Double point = new Ellipse2D.Double();

        Fireworks(Random random) { this.random = random; }

        void start() {
            next[0] = .10;
            next[1] = .44;
        }

        void clear() { bursts.clear(); }

        void advance(double elapsed) {
            for (int i = bursts.size() - 1; i >= 0; i--) {
                if (elapsed - bursts.get(i).started > 1.75) bursts.remove(i);
            }
            for (int side = 0; side < next.length; side++) {
                if (elapsed < next[side]) continue;
                float x = random.nextFloat(128, 282);
                if (side == 1) x = WIDTH - x;
                float y = random.nextFloat(50, 100);
                float speed = random.nextFloat(104, 182);
                Color color = COLORS[random.nextInt(COLORS.length)];
                bursts.add(new Burst(x, y, elapsed, speed, color, random.nextInt(36, 56), random));
                if (speed > 140 && random.nextBoolean()) {
                    bursts.add(new Burst(x + random.nextFloat(-24, 24), y + random.nextFloat(-17, 17),
                            elapsed + random.nextDouble(.34, .53), speed * .46f,
                            COLORS[random.nextInt(COLORS.length)], random.nextInt(14, 23), random));
                }
                next[side] = elapsed + random.nextDouble(.68, 1.26);
            }
        }

        void paint(Graphics2D g, double elapsed) {
            g.setPaint(VERDICT_LIGHT);
            g.fillRect(370, 0, 400, HEIGHT);
            g.setStroke(TRAIL);
            for (var burst : bursts) {
                double age = elapsed - burst.started;
                if (age < 0) continue;
                double ignition = Math.max(0, 1 - age / .33);
                if (ignition > 0) {
                    opacity(g, ignition);
                    g.setPaint(burst.light);
                    point.setFrame(burst.x - 36, burst.y - 36, 72, 72);
                    g.fill(point);
                }
                for (var spark : burst.sparks) paintSpark(g, burst, spark, age);
            }
            g.setComposite(AlphaComposite.SrcOver);
        }

        private void paintSpark(Graphics2D g, Burst burst, Spark spark, double age) {
            if (age >= spark.life) return;
            double fade = 1 - age / spark.life;
            double tailAge = Math.max(0, age - spark.tail);
            double travel = age / (1 + 1.15 * age);
            double trailing = tailAge / (1 + 1.15 * tailAge);
            double x = burst.x + spark.vx * travel;
            double y = burst.y + spark.vy * travel + 23 * age * age;
            g.setColor(spark.color);
            opacity(g, .80 * fade);
            trail.setLine(burst.x + spark.vx * trailing, burst.y + spark.vy * trailing + 23 * tailAge * tailAge, x, y);
            g.draw(trail);
            if (spark.bright) {
                opacity(g, .14 * fade);
                point.setFrame(x - 3.3, y - 3.3, 6.6, 6.6);
                g.fill(point);
                g.setColor(WHITE_GOLD);
            }
            double radius = spark.size * (.55 + .45 * fade);
            opacity(g, fade * (.82 + .18 * Math.cos(age * 35 + spark.vx)));
            point.setFrame(x - radius, y - radius, radius * 2, radius * 2);
            g.fill(point);
        }

        private record Spark(double vx, double vy, double life, double tail, double size, Color color, boolean bright) { }

        private static final class Burst {
            private final float x, y;
            private final double started;
            private final Paint light;
            private final Spark[] sparks;

            Burst(float x, float y, double started, float speed, Color color, int count, Random random) {
                this.x = x;
                this.y = y;
                this.started = started;
                light = glow(x, y, 36, WHITE_GOLD);
                sparks = new Spark[count];
                double rotation = random.nextDouble(Math.TAU);
                for (int i = 0; i < count; i++) {
                    double angle = rotation + (i + random.nextDouble(-.35, .35)) * Math.TAU / count;
                    double velocity = speed * random.nextDouble(.48, 1.22);
                    sparks[i] = new Spark(Math.cos(angle) * velocity, Math.sin(angle) * velocity * .78,
                            random.nextDouble(.88, 1.72), random.nextDouble(.09, .18), random.nextDouble(.8, 2.1),
                            i % 6 == 0 ? WHITE_GOLD : color, i % 4 == 0);
                }
            }
        }
    }

    private static final class Storm {
        private static final int TILE_WIDTH = 512;
        private static final Color[] RAIN_COLORS = {new Color(136, 168, 194, 42), new Color(156, 187, 215, 71),
                new Color(197, 219, 232, 107)};
        private static final BasicStroke[] RAIN_STROKES = {new BasicStroke(.55f), new BasicStroke(.9f), new BasicStroke(1.35f)};
        private static final Paint MIST = new GradientPaint(0, 104, new Color(132, 147, 171, 0),
                0, HEIGHT, new Color(132, 147, 171, 42));
        private static final Paint FLASH = new GradientPaint(0, 0, new Color(156, 185, 230, 75),
                0, HEIGHT, new Color(156, 185, 230, 12));
        private final Random random;
        private final RainDrop[] rain = new RainDrop[100];
        private final Line2D.Double line = new Line2D.Double();
        private CloudLayer[] clouds;
        private Lightning lightning;
        private double nextStrike;

        Storm(Random random) {
            this.random = random;
            var rainRandom = new Random(196);
            for (int i = 0; i < rain.length; i++) {
                int depth = i < 46 ? 0 : i < 82 ? 1 : 2;
                rain[i] = new RainDrop(rainRandom.nextDouble(WIDTH + 90), rainRandom.nextDouble(HEIGHT + 48),
                        100 + 69 * depth + rainRandom.nextDouble(59), 8 + 7 * depth + rainRandom.nextDouble(7), depth);
            }
        }

        void start() { nextStrike = random.nextDouble(2.7, 4.5); }
        void clear() { lightning = null; }

        void advance(double elapsed) {
            if (lightning != null && elapsed - lightning.started >= .36) lightning = null;
            if (elapsed < nextStrike) return;
            lightning = new Lightning(elapsed, random);
            nextStrike = elapsed + random.nextDouble(5.4, 9.5);
        }

        void paint(Graphics2D g, double elapsed) {
            if (clouds == null) {
                clouds = new CloudLayer[]{
                        new CloudLayer(135, new Color(64, 76, 96), 7, 18, 0),
                        new CloudLayer(294, new Color(29, 39, 58), -12, 5, 139),
                        new CloudLayer(902, new Color(12, 20, 36), 19, -9, 44)};
            }
            for (var cloud : clouds) cloud.paint(g, elapsed);
            g.setPaint(MIST);
            g.fillRect(0, 104, WIDTH, HEIGHT - 104);
            if (lightning != null) lightning.paint(g, elapsed);
            int depth = -1;
            for (var drop : rain) {
                if (drop.depth != depth) {
                    depth = drop.depth;
                    g.setStroke(RAIN_STROKES[depth]);
                    g.setColor(RAIN_COLORS[depth]);
                }
                double y = (drop.y + elapsed * drop.speed) % (HEIGHT + 48) - 30;
                double x = (drop.x + (drop.speed * elapsed + drop.y) * .25) % (WIDTH + 90) - 45;
                line.setLine(x, y, x + drop.length * .26, y + drop.length);
                g.draw(line);
            }
        }

        private record RainDrop(double x, double y, double speed, double length, int depth) { }

        private static final class CloudLayer {
            private final BufferedImage tile = new BufferedImage(TILE_WIDTH, 118, BufferedImage.TYPE_INT_ARGB_PRE);
            private final int speed, y, offset;

            CloudLayer(long seed, Color color, int speed, int y, int offset) {
                this.speed = speed;
                this.y = y;
                this.offset = offset;
                var random = new Random(seed);
                Graphics2D g = tile.createGraphics();
                UiStyle.prepare(g);
                var base = new Path2D.Float();
                base.moveTo(0, 0);
                base.lineTo(TILE_WIDTH, 0);
                double phase = random.nextDouble(Math.TAU);
                for (int x = TILE_WIDTH; x >= 0; x -= 4) {
                    double angle = Math.TAU * x / TILE_WIDTH;
                    double edge = 47 + 8 * Math.cos(3 * angle + phase) + 7 * Math.cos(7 * angle - phase);
                    base.lineTo(x, edge);
                }
                base.closePath();
                g.setPaint(new GradientPaint(0, 0, color, 0, 61,
                        new Color(color.getRed(), color.getGreen(), color.getBlue(), 38)));
                g.fill(base);
                for (int i = 0; i < 10; i++) {
                    float x = i * (TILE_WIDTH / 10f) + random.nextFloat(-18, 18);
                    float centerY = random.nextFloat(9, 48);
                    float radius = random.nextFloat(45, 91);
                    for (int copy = -1; copy <= 1; copy++) {
                        float centerX = x + copy * TILE_WIDTH;
                        if (centerX + radius < 0 || centerX - radius > TILE_WIDTH) continue;
                        g.setPaint(glow(centerX, centerY, radius, color));
                        g.fill(new Rectangle2D.Float(centerX - radius, centerY - radius, radius * 2, radius * 2));
                    }
                }
                g.dispose();
            }

            void paint(Graphics2D g, double elapsed) {
                int start = -Math.floorMod((int) Math.round(elapsed * speed) + offset, TILE_WIDTH);
                for (int x = start; x < WIDTH; x += TILE_WIDTH) g.drawImage(tile, x, y, null);
            }
        }

        private static final class Lightning {
            private static final BasicStroke HALO = new BasicStroke(7, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
            private static final BasicStroke CORE = new BasicStroke(1.55f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
            private static final Color HALO_COLOR = new Color(118, 160, 232);
            private static final Color CORE_COLOR = new Color(218, 235, 255);
            private final double started;
            private final Path2D.Float path = new Path2D.Float();
            private final Paint cloudLight;
            private final float x;

            Lightning(double started, Random random) {
                this.started = started;
                float startX = random.nextFloat(155, 305);
                x = random.nextBoolean() ? startX : WIDTH - startX;
                cloudLight = glow(x, 21, 174, new Color(151, 189, 249, 115));
                float px = x, py = random.nextFloat(6, 25);
                path.moveTo(px, py);
                for (int i = 0; i < 7; i++) {
                    px += random.nextFloat(-24, 24);
                    py += random.nextFloat(9, 17);
                    path.lineTo(px, py);
                    if (i == 1 || i == 3) {
                        float direction = i == 1 ? -1 : 1;
                        path.moveTo(px, py);
                        path.lineTo(px + direction * random.nextFloat(17, 30), py + 10);
                        path.lineTo(px + direction * random.nextFloat(30, 44), py + 28);
                        path.lineTo(px + direction * random.nextFloat(41, 55), py + 37);
                        path.moveTo(px, py);
                    }
                }
            }

            void paint(Graphics2D g, double elapsed) {
                double envelope = Math.max(0, 1 - (elapsed - started) / .36);
                opacity(g, envelope * .72);
                g.setPaint(FLASH);
                g.fillRect(0, 0, WIDTH, HEIGHT);
                g.setPaint(cloudLight);
                g.fillRect((int) x - 174, 0, 348, HEIGHT);
                opacity(g, envelope * .32);
                g.setColor(HALO_COLOR);
                g.setStroke(HALO);
                g.draw(path);
                opacity(g, envelope * envelope * .84);
                g.setColor(CORE_COLOR);
                g.setStroke(CORE);
                g.draw(path);
                g.setComposite(AlphaComposite.SrcOver);
            }
        }
    }
}
