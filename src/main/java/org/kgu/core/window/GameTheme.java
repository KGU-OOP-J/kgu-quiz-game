package org.kgu.core.window;

import org.kgu.core.ui.ComponentPainter;
import org.kgu.core.ui.GradientTokens;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.plaf.*;
import javax.swing.plaf.basic.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public final class GameTheme {
    public static final Color INK = new Color(8, 16, 29);
    public static final Color DARK = new Color(15, 29, 45);
    public static final Color PAPER = new Color(242, 233, 211);
    public static final Color GOLD = new Color(222, 180, 104);
    public static final Color TEAL = new Color(44, 113, 122);
    public static final Color MUTED = new Color(157, 174, 186);
    public static final Color PAGE = new Color(0xEDF1F6);
    public static final Color BLUE_TEXT = new Color(0x275898);
    private static final Map<String, Optional<BufferedImage>> ASSETS = new ConcurrentHashMap<>();
    private static boolean installed;

    private GameTheme() { }

    public static void install() {
        if (installed) return;
        installed = true;
        UIManager.put("ButtonUI", ButtonUI.class.getName());
        UIManager.put("ToggleButtonUI", ButtonUI.class.getName());
        UIManager.put("ComboBoxUI", ComboUI.class.getName());
        UIManager.put("ScrollBarUI", ScrollUI.class.getName());
        UIManager.put("ProgressBarUI", ProgressUI.class.getName());
        UIManager.put("TextFieldUI", FieldUI.class.getName());
        UIManager.put("TabbedPaneUI", TabsUI.class.getName());
        FontUIResource font = new FontUIResource(UiStyle.FONT);
        for (String component : List.of("Button", "ToggleButton", "Label", "TextField", "TextArea", "ComboBox", "List", "TabbedPane", "OptionPane", "CheckBox", "ToolTip", "ProgressBar")) {
            UIManager.put(component + ".font", font);
            UIManager.put(component + ".background", new ColorUIResource(Color.WHITE));
            UIManager.put(component + ".foreground", new ColorUIResource(ComponentPainter.TEXT));
        }
        UIManager.put("Panel.background", new ColorUIResource(PAGE));
        UIManager.put("Viewport.background", new ColorUIResource(PAGE));
        UIManager.put("ScrollPane.background", new ColorUIResource(PAGE));
        UIManager.put("ScrollPane.border", BorderFactory.createEmptyBorder());
        UIManager.put("TextField.border", BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(TEAL), BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        UIManager.put("TextField.caretForeground", BLUE_TEXT);
        UIManager.put("TextArea.caretForeground", PAPER);
        UIManager.put("TextArea.selectionBackground", TEAL);
        UIManager.put("TextField.selectionBackground", TEAL);
        UIManager.put("ComboBox.selectionBackground", TEAL);
        UIManager.put("ComboBox.selectionForeground", PAPER);
        UIManager.put("ComboBox.disabledForeground", MUTED);
        UIManager.put("List.selectionBackground", TEAL);
        UIManager.put("List.selectionForeground", PAPER);
        UIManager.put("ToolTip.border", BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(ComponentPainter.BORDER), BorderFactory.createEmptyBorder(7, 10, 7, 10)));
        UIManager.put("CheckBox.icon", new IconUIResource(new Icon() {
            @Override public int getIconWidth() { return 27; }
            @Override public int getIconHeight() { return 27; }
            @Override public void paintIcon(Component component, Graphics source, int x, int y) {
                Graphics2D g = (Graphics2D)source.create(); UiStyle.prepare(g); ComponentPainter.surface(g, x, y, 24, 24, 4, GradientTokens.PANEL, ComponentPainter.SELECTED); g.setColor(ComponentPainter.SELECTED);
                if (((AbstractButton)component).isSelected()) { g.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)); g.drawLine(x + 5, y + 12, x + 10, y + 18); g.drawLine(x + 10, y + 18, x + 20, y + 6); } g.dispose();
            }
        }));
        UIManager.put("OptionPane.messageForeground", ComponentPainter.TEXT);
        UIManager.put("OptionPane.background", new ColorUIResource(PAGE));
        UIManager.put("OptionPane.messageAreaBorder", BorderFactory.createEmptyBorder(16, 16, 12, 16));
        UIManager.put("OptionPane.buttonAreaBorder", BorderFactory.createEmptyBorder(8, 16, 16, 16));
        IconUIResource notice = new IconUIResource(new Icon() {
            @Override public int getIconWidth() { return 34; }
            @Override public int getIconHeight() { return 34; }
            @Override public void paintIcon(Component component, Graphics source, int x, int y) {
                Graphics2D g = (Graphics2D)source.create(); UiStyle.prepare(g); g.setColor(ComponentPainter.SELECTED); g.setStroke(new BasicStroke(2)); g.drawOval(x + 2, y + 2, 28, 28); g.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 22f)); g.drawString("!", x + 13, y + 24); g.dispose();
            }
        });
        for (String key : List.of("OptionPane.errorIcon", "OptionPane.warningIcon", "OptionPane.informationIcon", "OptionPane.questionIcon")) UIManager.put(key, notice);
    }

    public static Optional<BufferedImage> asset(String name) {
        return resourceImage("/org/kgu/art/" + name + ".png");
    }

    public static Optional<BufferedImage> resourceImage(String path) {
        return ASSETS.computeIfAbsent(path, key -> {
            try (var stream = GameTheme.class.getResourceAsStream(key)) {
                return stream == null ? Optional.empty() : Optional.ofNullable(ImageIO.read(stream));
            } catch (java.io.IOException e) { return Optional.empty(); }
        });
    }

    public static void cover(Graphics2D g, Image image, int x, int y, int width, int height) {
        int iw = image.getWidth(null), ih = image.getHeight(null);
        if (iw <= 0 || ih <= 0 || width <= 0 || height <= 0) return;
        double scale = Math.max(width / (double)iw, height / (double)ih);
        int w = (int)Math.ceil(iw * scale), h = (int)Math.ceil(ih * scale);
        Shape clip = g.getClip(); g.clipRect(x, y, width, height);
        g.drawImage(image, x + (width - w) / 2, y + (height - h) / 2, w, h, null); g.setClip(clip);
    }

    public static void backdrop(Graphics2D g, int width, int height, String scene, float shade) {
        g.setColor(PAGE); g.fillRect(0, 0, width, height);
        Optional<BufferedImage> art = asset("scene-" + scene);
        if (art.isPresent()) cover(g, art.get(), 0, 0, width, height);
        else {
            String resource = switch (scene) {
                case "login" -> "/org/kgu/login/main_login.png";
                case "prologue", "dialogue" -> "/org/kgu/result/result_background.png";
                case "study" -> "/images/question-background.png";
                default -> "/org/kgu/battle/campus_background.png";
            };
            resourceImage(resource).ifPresent(image -> cover(g, image, 0, 0, width, height));
        }
        g.setColor(new Color(PAGE.getRed(), PAGE.getGreen(), PAGE.getBlue(), Math.round(255 * shade)));
        g.fillRect(0, 0, width, height);
    }

    public static void paintBackdrop(Graphics source, JComponent component, String scene, float shade) {
        Graphics2D g = (Graphics2D)source.create();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        backdrop(g, component.getWidth(), component.getHeight(), scene, shade);
        g.dispose();
    }

    public static JPanel background(String scene, LayoutManager layout) {
        return new JPanel(layout) {
            @Override protected void paintComponent(Graphics source) { Graphics2D g = (Graphics2D)source.create(); UiStyle.prepare(g); backdrop(g, getWidth(), getHeight(), scene, 0.82f); g.dispose(); }
        };
    }

    public static JPanel panel(LayoutManager layout) {
        JPanel panel = new JPanel(layout) {
            @Override protected void paintComponent(Graphics source) { Graphics2D g = (Graphics2D)source.create(); UiStyle.prepare(g); paintSurface(g, 0, 0, getWidth(), getHeight(), 18, false); g.dispose(); }
        };
        panel.setOpaque(false); return panel;
    }

    public static JPanel clear(LayoutManager layout) { JPanel panel = new JPanel(layout); panel.setOpaque(false); return panel; }

    public static JPanel lightPanel(LayoutManager layout) {
        JPanel panel = new JPanel(layout) {
            @Override protected void paintComponent(Graphics source) {
                Graphics2D g = (Graphics2D)source.create();
                UiStyle.prepare(g);
                ComponentPainter.surface(g, 0, 0, getWidth(), getHeight(), 16,
                    GradientTokens.PANEL, ComponentPainter.BORDER);
                g.dispose();
            }
        };
        panel.setOpaque(false);
        return panel;
    }

    public static void paintSurface(Graphics2D g, int x, int y, int width, int height, int radius, boolean selected) {
        ComponentPainter.surface(g, x, y, width, height, radius,
            selected ? GradientTokens.SELECTED : GradientTokens.PANEL,
            selected ? ComponentPainter.SELECTED : ComponentPainter.BORDER);
    }

    public static JScrollPane scroll(JComponent component) {
        JScrollPane scroll = new JScrollPane(component); scroll.setOpaque(false); scroll.getViewport().setOpaque(false);
        scroll.setBorder(BorderFactory.createEmptyBorder()); scroll.getVerticalScrollBar().setUnitIncrement(22);
        scroll.getHorizontalScrollBar().setUnitIncrement(22); return scroll;
    }

    public static JLabel label(String text, float size, Color color) {
        JLabel label = new JLabel(text); label.setFont(UiStyle.FONT.deriveFont(size)); label.setForeground(color); return label;
    }

    public static List<String> wrap(String text, FontMetrics metrics, int width) {
        List<String> lines = new ArrayList<>(); StringBuilder line = new StringBuilder();
        for (int point : text.codePoints().toArray()) {
            if (point == '\n') { lines.add(line.toString()); line.setLength(0); continue; }
            String next = new String(Character.toChars(point));
            if (!line.isEmpty() && metrics.stringWidth(line + next) > Math.max(1, width)) { lines.add(line.toString().stripTrailing()); line.setLength(0); }
            if (point != ' ' || !line.isEmpty()) line.append(next);
        }
        if (!line.isEmpty() || lines.isEmpty()) lines.add(line.toString()); return lines;
    }

    public static void ninePatch(Graphics2D g, BufferedImage image, int x, int y, int width, int height, int edge) {
        int sx = Math.min(image.getWidth() / 3, edge), sy = Math.min(image.getHeight() / 3, edge);
        int dx = Math.min(width / 3, edge), dy = Math.min(height / 3, edge);
        int[] sourceX = {0, sx, image.getWidth() - sx, image.getWidth()}, sourceY = {0, sy, image.getHeight() - sy, image.getHeight()};
        int[] targetX = {x, x + dx, x + width - dx, x + width}, targetY = {y, y + dy, y + height - dy, y + height};
        for (int row = 0; row < 3; row++) for (int col = 0; col < 3; col++) g.drawImage(image, targetX[col], targetY[row], targetX[col + 1], targetY[row + 1], sourceX[col], sourceY[row], sourceX[col + 1], sourceY[row + 1], null);
    }

    public static void paintButton(Graphics2D g, AbstractButton button, Color tint) {
        UiStyle.prepare(g); int w = button.getWidth(), h = button.getHeight();
        boolean selected = button.getModel().isSelected() || Boolean.TRUE.equals(button.getClientProperty("selected"));
        boolean hover = button.getModel().isRollover(), pressed = button.getModel().isPressed();
        boolean correct = Boolean.TRUE.equals(button.getClientProperty("correct"));
        boolean incorrect = Boolean.TRUE.equals(button.getClientProperty("incorrect"));
        GradientTokens style = selected ? GradientTokens.SELECTED
            : !button.isEnabled() ? GradientTokens.DISABLED
            : Boolean.TRUE.equals(button.getClientProperty("secondaryAction")) ? GradientTokens.NEUTRAL
            : GradientTokens.PRIMARY;
        Color textColor = ComponentPainter.button(g, w, h, style, selected, correct, incorrect,
            hover && button.isEnabled(), pressed && button.isEnabled(), button.hasFocus());
        g.setFont(button.getFont());
        g.setColor(textColor);
        javax.swing.text.View html = (javax.swing.text.View)button.getClientProperty(BasicHTML.propertyKey);
        if (html != null) {
            html.setSize(Math.max(1, w - 30), Math.max(1, h - 12)); html.paint(g, new Rectangle(15, 6, Math.max(1, w - 30), Math.max(1, h - 12)));
        } else {
            String text = Objects.toString(button.getText(), ""); Font font = button.getFont();
            while (g.getFontMetrics(font).stringWidth(text) > w - 24 && font.getSize2D() > 11) font = font.deriveFont(font.getSize2D() - 1);
            g.setFont(font); FontMetrics metrics = g.getFontMetrics();
            int x = button.getHorizontalAlignment() == SwingConstants.LEFT ? 15 : (w - metrics.stringWidth(text)) / 2;
            g.drawString(text, x, (h - metrics.getHeight()) / 2 + metrics.getAscent() - 1);
        }
    }

    public static final class ButtonUI extends BasicButtonUI {
        public static ComponentUI createUI(JComponent component) { return new ButtonUI(); }
        @Override protected void installDefaults(AbstractButton button) { super.installDefaults(button); button.setOpaque(false); button.setContentAreaFilled(false); button.setBorderPainted(false); button.setRolloverEnabled(true); button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); button.setMargin(new Insets(9, 16, 9, 16)); }
        @Override public void paint(Graphics graphics, JComponent component) { Graphics2D g = (Graphics2D)graphics.create(); AbstractButton button = (AbstractButton)component; paintButton(g, button, button.getBackground()); g.dispose(); }
    }

    public static final class ComboUI extends BasicComboBoxUI {
        public static ComponentUI createUI(JComponent component) { return new ComboUI(); }
        @Override protected void installDefaults() { super.installDefaults(); comboBox.setOpaque(false); comboBox.setBorder(BorderFactory.createEmptyBorder(7, 10, 7, 5)); }
        @Override protected JButton createArrowButton() {
            JButton arrow = new BasicArrowButton(SwingConstants.SOUTH, Color.WHITE, Color.WHITE, BLUE_TEXT, Color.WHITE);
            arrow.setBorder(BorderFactory.createEmptyBorder());
            arrow.setPreferredSize(new Dimension(28, 28));
            return arrow;
        }
        @Override protected ComboPopup createPopup() {
            BasicComboPopup popup = new BasicComboPopup(comboBox);
            popup.setBorder(BorderFactory.createLineBorder(ComponentPainter.BORDER)); popup.getList().setBackground(Color.WHITE); popup.getList().setForeground(ComponentPainter.TEXT);
            popup.getList().setSelectionBackground(PAGE); popup.getList().setSelectionForeground(ComponentPainter.TEXT);
            return popup;
        }
        @Override public void paint(Graphics graphics, JComponent component) {
            Graphics2D g = (Graphics2D)graphics.create(); UiStyle.prepare(g); ComponentPainter.surface(g, 0, 0, component.getWidth(), component.getHeight(), 8, GradientTokens.PANEL,
                comboBox.hasFocus() ? ComponentPainter.SELECTED : ComponentPainter.BORDER);
            paintCurrentValue(g, rectangleForCurrentValue(), comboBox.hasFocus()); g.dispose();
        }
        @Override public void paintCurrentValueBackground(Graphics graphics, Rectangle bounds, boolean focus) { }
        @Override public void paintCurrentValue(Graphics graphics, Rectangle bounds, boolean focus) {
            var renderer = comboBox.getRenderer();
            Component value = renderer.getListCellRendererComponent(listBox, comboBox.getSelectedItem(), -1, false, false);
            value.setFont(comboBox.getFont()); value.setForeground(comboBox.isEnabled() ? ComponentPainter.TEXT : ComponentPainter.MUTED); value.setBackground(Color.WHITE);
            if (value instanceof JComponent c) c.setOpaque(false);
            currentValuePane.paintComponent(graphics, value, comboBox, bounds.x, bounds.y, bounds.width, bounds.height, true);
        }
    }

    public static final class ScrollUI extends BasicScrollBarUI {
        public static ComponentUI createUI(JComponent component) { return new ScrollUI(); }
        @Override protected void configureScrollBarColors() { thumbColor = new Color(0x95D1FA); trackColor = PAGE; scrollBarWidth = 9; }
        private JButton empty() { JButton b = new JButton(); b.setPreferredSize(new Dimension(0, 0)); b.setMinimumSize(new Dimension(0, 0)); b.setMaximumSize(new Dimension(0, 0)); return b; }
        @Override protected JButton createDecreaseButton(int direction) { return empty(); }
        @Override protected JButton createIncreaseButton(int direction) { return empty(); }
        @Override protected void paintTrack(Graphics graphics, JComponent component, Rectangle bounds) { graphics.setColor(PAGE); graphics.fillRect(bounds.x, bounds.y, bounds.width, bounds.height); }
        @Override protected void paintThumb(Graphics graphics, JComponent component, Rectangle bounds) { Graphics2D g = (Graphics2D)graphics.create(); UiStyle.prepare(g); g.setColor(isThumbRollover() ? ComponentPainter.SELECTED : thumbColor); g.fillRoundRect(bounds.x + 1, bounds.y + 2, Math.max(2, bounds.width - 2), Math.max(2, bounds.height - 4), 7, 7); g.dispose(); }
    }

    public static final class ProgressUI extends BasicProgressBarUI {
        public static ComponentUI createUI(JComponent component) { return new ProgressUI(); }
        @Override protected void installDefaults() { super.installDefaults(); progressBar.setOpaque(false); progressBar.setBorder(BorderFactory.createEmptyBorder()); }
        @Override protected void paintDeterminate(Graphics graphics, JComponent component) {
            Graphics2D g = (Graphics2D)graphics.create(); UiStyle.prepare(g); int w = component.getWidth(), h = component.getHeight();
            double fraction = progressBar.getPercentComplete();
            ComponentPainter.progress(g, w, h, fraction, GradientTokens.PROGRESS);
            if (progressBar.isStringPainted()) {
                g.setFont(progressBar.getFont());
                FontMetrics metrics = g.getFontMetrics();
                String text = progressBar.getString();
                int x = (w - metrics.stringWidth(text)) / 2;
                int baseline = (h - metrics.getHeight()) / 2 + metrics.getAscent();
                g.setColor(ComponentPainter.TEXT);
                g.drawString(text, x, baseline);
                g.clipRect(0, 0, (int)Math.round(w * fraction), h);
                g.setColor(Color.WHITE);
                g.drawString(text, x, baseline);
            }
            g.dispose();
        }
        @Override protected void paintIndeterminate(Graphics graphics, JComponent component) { paintDeterminate(graphics, component); }
    }

    public static final class FieldUI extends BasicTextFieldUI {
        public static ComponentUI createUI(JComponent component) { return new FieldUI(); }
        @Override protected void paintSafely(Graphics graphics) {
            if (!getComponent().isOpaque()) paintBackground(graphics);
            super.paintSafely(graphics);
        }

        @Override protected void paintBackground(Graphics graphics) {
            Graphics2D g = (Graphics2D)graphics.create();
            UiStyle.prepare(g);
            if (Boolean.TRUE.equals(getComponent().getClientProperty("lightSurface"))) {
                ComponentPainter.surface(g, 0, 0, getComponent().getWidth(), getComponent().getHeight(), 8,
                    GradientTokens.PANEL, getComponent().hasFocus() ? ComponentPainter.SELECTED : ComponentPainter.BORDER);
            } else {
                paintSurface(g, 0, 0, getComponent().getWidth(), getComponent().getHeight(), 9, getComponent().hasFocus());
            }
            g.dispose();
        }
    }

    public static final class TabsUI extends BasicTabbedPaneUI {
        public static ComponentUI createUI(JComponent component) { return new TabsUI(); }
        @Override protected void paintTabBackground(Graphics graphics, int placement, int index, int x, int y, int w, int h, boolean selected) { Graphics2D g = (Graphics2D)graphics.create(); UiStyle.prepare(g); paintSurface(g, x, y, w, h, 10, selected); g.dispose(); }
        @Override protected void paintTabBorder(Graphics graphics, int placement, int index, int x, int y, int w, int h, boolean selected) { }
        @Override protected void paintFocusIndicator(Graphics graphics, int placement, Rectangle[] rects, int index, Rectangle icon, Rectangle text, boolean selected) { }
        @Override protected void paintContentBorder(Graphics graphics, int placement, int index) { }
    }
}
