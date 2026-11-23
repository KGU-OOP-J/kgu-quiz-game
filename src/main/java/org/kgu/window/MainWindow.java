package org.kgu.window;

import javax.swing.*;
import java.awt.*;
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

    public MainWindow(Runnable shutdown) {
        requireEdt();
        Objects.requireNonNull(shutdown);
        frame = new JFrame("WHO’S NEXT? — 인물 카드 퀴즈");
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
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

    public void register(View view, LifecycleEvent lifecycleEvent) {
        requireEdt();
        Objects.requireNonNull(view);
        Objects.requireNonNull(lifecycleEvent);
        if (views.containsKey(view.getViewId())) throw new IllegalArgumentException("이미 등록한 화면: " + view.getViewId());
        views.put(view.getViewId(), view);
        lifecycleEvents.put(view.getViewId(), lifecycleEvent);
    }

    public void show(ViewId viewId) {
        requireEdt();
        if (closed) throw new IllegalStateException("종료된 창입니다.");
        View view = Objects.requireNonNull(views.get(viewId), "등록하지 않은 화면: " + viewId);
        current.ifPresent(id -> lifecycleEvents.get(id).onExit());
        frame.getContentPane().removeAll();
        frame.getContentPane().setPreferredSize(view.getViewSize());
        frame.pack();
        Rectangle bounds = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        frame.setSize(Math.min(frame.getWidth(), bounds.width), Math.min(frame.getHeight(), bounds.height));
        frame.setLocation(bounds.x + (bounds.width - frame.getWidth()) / 2, bounds.y + (bounds.height - frame.getHeight()) / 2);
        frame.getContentPane().add(view.getComponent(), BorderLayout.CENTER);
        current = Optional.of(viewId);
        lifecycleEvents.get(viewId).onEnter();
        frame.revalidate();
        frame.repaint();
        frame.setVisible(true);
    }

    public void close() {
        requireEdt();
        if (closed) return;
        current.ifPresent(id -> lifecycleEvents.get(id).onExit());
        frame.dispose();
        closed = true;
    }
}
