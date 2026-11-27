package org.kgu.domain.collection.view.component;

import org.kgu.core.model.Person;
import org.kgu.core.progression.CardPower;
import org.kgu.core.ui.ComponentPainter;
import org.kgu.core.window.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public final class CardAcquisitionPanel extends JPanel {
    public interface Actions {
        CardPower power(Person person);
        String equippedDescription();
        int mastery(Person person);
        void equip(Person person) throws java.io.IOException;
        void openCollection();
    }
    private final List<Person> cards;
    private final int stage;
    private final CardArt art;
    private final Actions actions;
    private final JLabel status = GameTheme.label("", 16, ComponentPainter.TEXT);
    private final JTextArea information = new JTextArea();
    private final JButton equip = new UiButton("즉시 장착", GameTheme.BLUE_TEXT);
    private final JButton next = new UiButton("계속 진행", GameTheme.BLUE_TEXT);
    private final JPanel portrait;
    private final Timer timer;
    private int index;
    private long started;
    private double progress;
    private JLayeredPane host;
    private final KeyEventDispatcher input = event -> {
        if (host == null || KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusedWindow() != SwingUtilities.getWindowAncestor(host)) return false;
        if (event.getKeyCode() == KeyEvent.VK_F1) return false;
        if (event.getKeyCode() == KeyEvent.VK_TAB || event.getKeyCode() == KeyEvent.VK_SPACE) {
            Component focused = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
            if (focused != null && SwingUtilities.isDescendingFrom(focused, this)) return false;
            if (event.getID() == KeyEvent.KEY_PRESSED) next.requestFocusInWindow();
            return true;
        }
        if (event.getID() == KeyEvent.KEY_PRESSED) {
            if (event.getKeyCode() == KeyEvent.VK_ENTER) advance();
            else if (event.getKeyCode() == KeyEvent.VK_ESCAPE) { if (progress < 1) reveal(); else close(); }
        }
        return true;
    };
    private final ComponentAdapter resize = new ComponentAdapter() {
        @Override public void componentResized(ComponentEvent event) { fit(); }
    };

    public CardAcquisitionPanel(List<Person> cards, int stage, CardArt art, Actions actions) {
        super(new GridBagLayout()); this.cards = List.copyOf(cards); this.stage = stage; this.art = art; this.actions = actions;
        setOpaque(false); setFocusCycleRoot(true); setFocusable(true);
        JPanel content = GameTheme.lightPanel(new BorderLayout(24, 20));
        content.setPreferredSize(new Dimension(820, 590)); content.setBorder(BorderFactory.createEmptyBorder(28, 28, 28, 28));
        JLabel title = GameTheme.label("새로운 동료를 만났습니다", 30, ComponentPainter.TEXT);
        title.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 30f)); content.add(title, BorderLayout.NORTH);
        portrait = new JPanel() {
            @Override protected void paintComponent(Graphics source) { paintCard((Graphics2D)source); }
        };
        portrait.setOpaque(false); portrait.setPreferredSize(new Dimension(280, 350));
        JPanel body = GameTheme.clear(new BorderLayout(24, 16)); body.add(portrait, BorderLayout.WEST);
        information.setEditable(false); information.setLineWrap(true); information.setWrapStyleWord(true);
        information.setOpaque(false); information.setForeground(ComponentPainter.TEXT); information.setFont(UiStyle.FONT.deriveFont(17f));
        body.add(GameTheme.scroll(information), BorderLayout.CENTER); content.add(body, BorderLayout.CENTER);
        JPanel bottom = GameTheme.clear(new BorderLayout(0, 12)); bottom.add(status, BorderLayout.NORTH);
        JPanel buttons = GameTheme.clear(new GridLayout(2, 2, 12, 10));
        JButton collection = new UiButton("도감 확인", GameTheme.BLUE_TEXT);
        JButton skip = new UiButton("연출 건너뛰기 / 모두 확인", GameTheme.BLUE_TEXT);
        collection.putClientProperty("secondaryAction", true); skip.putClientProperty("secondaryAction", true);
        equip.addActionListener(e -> {
            if (progress < 1) return;
            try { actions.equip(current()); status.setText(current().getName() + " 장착 완료 · 다음 전투부터 적용"); equip.setEnabled(false); GameResources.play(GameResources.Sound.UI_CONFIRM); }
            catch (java.io.IOException ex) { status.setText("장착 저장 실패 · 다시 시도하세요"); }
        });
        next.addActionListener(e -> advance());
        skip.addActionListener(e -> { if (progress < 1) reveal(); else close(); });
        collection.addActionListener(e -> { close(); actions.openCollection(); });
        buttons.add(equip); buttons.add(next); buttons.add(collection); buttons.add(skip); bottom.add(buttons); content.add(bottom, BorderLayout.SOUTH);
        add(content);
        timer = new Timer(16, e -> tick());
        bind("ENTER", "next", this::advance); bind("ESCAPE", "skip", () -> { if (progress < 1) reveal(); else close(); });
        addMouseListener(new MouseAdapter() { }); addMouseMotionListener(new MouseMotionAdapter() { });
        addMouseWheelListener(e -> e.consume());
    }

    public static void show(JComponent owner, List<Person> cards, int stage, CardArt art, Actions actions) {
        if (cards.isEmpty() || !owner.isShowing()) return;
        JRootPane root = SwingUtilities.getRootPane(owner);
        if (root == null) return;
        CardAcquisitionPanel panel = new CardAcquisitionPanel(cards, stage, art, actions);
        panel.host = root.getLayeredPane(); panel.host.add(panel, JLayeredPane.DRAG_LAYER);
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(panel.input);
        panel.host.addComponentListener(panel.resize); panel.fit(); panel.begin(); panel.requestFocusInWindow();
    }
    private void fit() { if (host != null) setBounds(0, 0, host.getWidth(), host.getHeight()); }
    private Person current() { return cards.get(index); }
    private void begin() {
        started = System.nanoTime(); progress = 0; information.setText("카드를 공개하는 중입니다.");
        status.setText((index + 1) + " / " + cards.size() + " · NEW 알림은 단계별 상세에서 확인할 때 해제됩니다.");
        equip.setEnabled(false); next.setEnabled(false); timer.start(); GameResources.play(GameResources.Sound.CARD_FLIP);
    }
    private void tick() {
        progress = Math.min(1, (System.nanoTime() - started) / ((400 + stage * 400) * 1_000_000.0));
        if (progress >= 1) reveal(); repaint();
    }
    private void reveal() {
        boolean first = timer.isRunning(); timer.stop(); progress = 1;
        CardPower power = actions.power(current());
        information.setText(current().getName() + "  ·  NEW\n" + current().getCategory() + " · 카드 " + stage + "단계\n\n"
            + "장착 시 지원 · 보유 최고 단계 기준\n" + power.title() + "\n" + power.description() + "\n\n숙련도 " + actions.mastery(current())
            + "\n\n현재 장착\n" + actions.equippedDescription());
        information.setCaretPosition(0); equip.setEnabled(true); next.setEnabled(true);
        if (first) GameResources.play(stage == 1 ? GameResources.Sound.CARD_REVEAL_1 : stage == 2 ? GameResources.Sound.CARD_REVEAL_2 : GameResources.Sound.CARD_REVEAL_3);
        repaint();
    }
    private void advance() { if (progress < 1) return; if (++index == cards.size()) close(); else begin(); }
    private void close() {
        timer.stop();
        KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(input);
        if (host != null) {
            host.removeComponentListener(resize); host.remove(this); host.revalidate(); host.repaint(); host = null;
        }
    }
    private void bind(String key, String name, Runnable command) {
        getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(key), name);
        getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke(key), name);
        getActionMap().put(name, new AbstractAction() { @Override public void actionPerformed(ActionEvent e) { command.run(); } });
    }
    @Override public void removeNotify() { timer.stop(); KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(input); if (host != null) host.removeComponentListener(resize); super.removeNotify(); }
    @Override protected void paintComponent(Graphics source) {
        source.setColor(new Color(25, 48, 78, 110)); source.fillRect(0, 0, getWidth(), getHeight());
    }
    private void paintCard(Graphics2D source) {
        Graphics2D g = (Graphics2D)source.create(); UiStyle.prepare(g);
        int w = portrait.getWidth(), h = portrait.getHeight();
        int cardW = Math.min(220, w - 48), cardH = Math.min(310, h - 40);
        Color glow = stage == 3 ? new Color(224, 178, 68) : stage == 2 ? ComponentPainter.SELECTED : new Color(174, 191, 207);
        double reveal = Math.clamp((progress - .2) / .45, 0, 1);
        g.translate(w / 2.0, h / 2.0);
        if (stage == 3 && progress > .6 && progress < .8) g.translate(Math.sin(progress * 160) * 3, 0);
        if (progress > .5) {
            g.setColor(glow); g.setStroke(new BasicStroke(2));
            for (int i = 0; i < stage * 12; i++) {
                double angle = i * Math.PI * 2 / (stage * 12), radius = 70 + progress * 100;
                int x = (int)(Math.cos(angle) * radius), y = (int)(Math.sin(angle) * radius);
                g.fillOval(x, y, 3, 3);
                if (stage > 1 && i % 3 == 0) g.drawLine(x / 2, y / 2, x, y);
            }
        }
        g.scale(Math.max(.03, Math.abs(Math.cos(reveal * Math.PI))), .85 + .15 * Math.min(1, progress * 4));
        if (reveal < .5) {
            g.setColor(GameTheme.BLUE_TEXT); g.fillRoundRect(-cardW / 2, -cardH / 2, cardW, cardH, 16, 16);
            g.setColor(Color.WHITE); g.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 54f)); g.drawString("?", -18, 18);
        } else art.drawCard(g, current(), stage, -cardW / 2, -cardH / 2, cardW, cardH, true, 1.16f);
        g.dispose();
    }
}
