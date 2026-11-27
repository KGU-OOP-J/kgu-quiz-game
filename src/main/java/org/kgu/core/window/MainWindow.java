package org.kgu.core.window;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * 프로그램의 유일한 JFrame. View 교체와 실제 창 크기만 관리한다.
 */
public final class MainWindow {
    private final JFrame frame;
    private final Map<ViewId, View> views = new EnumMap<>(ViewId.class);
    private final Map<ViewId, LifecycleEvent> lifecycleEvents = new EnumMap<>(ViewId.class);
    private Optional<ViewId> current = Optional.empty();
    private boolean closed;
    private Timer transition;
    private float transitionAlpha;
    private java.awt.image.BufferedImage previousFrame;
    private ViewId queuedView;
    private final KeyEventDispatcher transitionInput = this::blockTransitionInput;
    private final JComponent curtain = new JComponent() {
        @Override protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D)graphics.create();
            if (previousFrame != null) {
                g.setComposite(AlphaComposite.SrcOver.derive(transitionAlpha));
                g.drawImage(previousFrame, 0, 0, getWidth(), getHeight(), null);
            }
            g.dispose();
        }
    };

    public MainWindow(Runnable shutdown) {
        requireEdt();
        Objects.requireNonNull(shutdown);
        GameTheme.install();
        frame = new JFrame("WHO’S NEXT? — 인물 카드 퀴즈");
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        curtain.addMouseListener(new java.awt.event.MouseAdapter() { });
        curtain.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() { });
        curtain.addMouseWheelListener(event -> event.consume());
        curtain.setFocusTraversalKeysEnabled(false); frame.setGlassPane(curtain);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                shutdown.run();
            }
        });
    }

    private static void requireEdt() {
        if (!SwingUtilities.isEventDispatchThread()) throw new IllegalStateException("창 조작은 Swing EDT에서 실행해야 합니다.");
    }

    public void bindShowcase(Runnable action) {
        JRootPane root = frame.getRootPane();
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("released F1"), "showcase");
        root.getActionMap().put("showcase", new AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent event) {
                action.run();
            }
        });
    }

    public void setTitle(String title) { frame.setTitle(title); }

    public Component dialogOwner() { return frame; }

    public void register(View view, LifecycleEvent lifecycleEvent) {
        requireEdt();
        Objects.requireNonNull(view);
        Objects.requireNonNull(lifecycleEvent);
        if (views.containsKey(view.getViewId())) throw new IllegalArgumentException("이미 등록한 화면: " + view.getViewId());
        views.put(view.getViewId(), view);
        lifecycleEvents.put(view.getViewId(), lifecycleEvent);
    }

    /** A new local login owns a fresh set of views and feature instances. */
    public void clearPlayerViews() {
        requireEdt();
        if (current.isPresent() && current.get() != ViewId.LOGIN) throw new IllegalStateException("로그인 화면에서만 사용자를 바꿀 수 있습니다.");
        for (ViewId id : ViewId.values()) if (id != ViewId.LOGIN && id != ViewId.PROLOGUE) {
            LifecycleEvent event = lifecycleEvents.remove(id);
            if (event != null) event.onExit();
            views.remove(id);
        }
    }

    public void show(ViewId viewId) {
        requireEdt();
        if (closed) throw new IllegalStateException("종료된 창입니다.");
        if (transition != null) { queuedView = viewId; return; }
        if (current.isPresent() && current.get() != viewId) {
            fadeTo(viewId); return;
        }
        showNow(viewId);
    }

    private void fadeTo(ViewId viewId) {
        Container content = frame.getContentPane();
        if (content.getWidth() <= 0 || content.getHeight() <= 0) { showNow(viewId); return; }
        previousFrame = new java.awt.image.BufferedImage(content.getWidth(), content.getHeight(), java.awt.image.BufferedImage.TYPE_INT_RGB);
        Graphics2D capture = previousFrame.createGraphics(); content.printAll(capture); capture.dispose();
        long start = System.nanoTime();
        transitionAlpha = 1;
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(transitionInput);
        transition = new Timer(16, event -> {
            double progress = Math.min(1, (System.nanoTime() - start) / 300_000_000.0);
            transitionAlpha = (float)(1 - progress); curtain.repaint();
            if (progress >= 1) {
                stopTransition();
                views.get(viewId).getComponent().requestFocusInWindow();
                ViewId next = queuedView; queuedView = null;
                if (next != null && next != current.orElse(null)) show(next);
            }
        });
        try { showNow(viewId); }
        catch (RuntimeException e) { stopTransition(); throw e; }
        curtain.setVisible(true); transition.start();
        GameResources.play(GameResources.Sound.UI_TRANSITION);
    }

    private boolean blockTransitionInput(KeyEvent event) {
        return event.getKeyCode() != KeyEvent.VK_F1
            && KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusedWindow() == frame;
    }

    private void stopTransition() {
        if (transition != null) transition.stop(); transition = null;
        KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(transitionInput);
        curtain.setVisible(false); transitionAlpha = 0; previousFrame = null;
    }

    private void showNow(ViewId viewId) {
        View view = Objects.requireNonNull(views.get(viewId), "등록하지 않은 화면: " + viewId);
        current.ifPresent(id -> lifecycleEvents.get(id).onExit());
        Container content = frame.getContentPane();
        content.removeAll();
        content.add(view.getComponent(), BorderLayout.CENTER);
        if (!frame.isShowing()) {
            content.setPreferredSize(view.getViewSize());
            frame.pack();
            Rectangle bounds = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
            frame.setSize(Math.min(frame.getWidth(), bounds.width), Math.min(frame.getHeight(), bounds.height));
            frame.setLocation(bounds.x + (bounds.width - frame.getWidth()) / 2,
                bounds.y + (bounds.height - frame.getHeight()) / 2);
        }
        current = Optional.of(viewId);
        lifecycleEvents.get(viewId).onEnter();
        frame.revalidate();
        frame.repaint();
        frame.setVisible(true);
    }

    public void close() {
        requireEdt();
        if (closed) return;
        stopTransition(); queuedView = null;
        current.ifPresent(id -> lifecycleEvents.get(id).onExit());
        frame.dispose();
        closed = true;
    }
}
