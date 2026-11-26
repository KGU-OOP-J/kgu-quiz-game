package org.kgu.collection.view;

import org.kgu.collection.event.CollectionGUIEvent;
import org.kgu.model.Person;
import org.kgu.window.GameTheme;
import org.kgu.window.UiButton;
import org.kgu.window.UiStyle;
import org.kgu.window.View;
import org.kgu.window.ViewId;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 인물 도감 화면. 마을 지도 → 마을(분야)별 카드 목록 → 인물 상세 순서로 보여 준다.
 */
public final class CollectionView extends JPanel implements View {
    // WaveMap.ISLANDS와 같은 순서(지도의 왼쪽 위, 오른쪽 위, 왼쪽 아래, 오른쪽 아래 섬)
    private static final String[] VILLAGES = {"과학자", "교수님", "한국사", "아이돌"};
    private static final int LOCKED = java.util.Arrays.asList(VILLAGES).indexOf(CollectionGUIEvent.LOCKED_VILLAGE);
    private static final double UNLOCK_SECONDS = LockArt.DURATION;
    private static final Point[] LABELS = {new Point(120, 200), new Point(480, 200), new Point(110, 452), new Point(480, 452)};  // 아래 버튼(y 510)과 안 겹치게
    // 배치는 처음 만든 780x560 좌표로 하고, 로그인·메뉴·대전과 같은 1280x960 창에 맞게 한 번에 늘린다.
    private static final int WIDTH = 1280, HEIGHT = 960;
    static final double SX = WIDTH / (double) UiStyle.WIDTH, SY = HEIGHT / (double) UiStyle.HEIGHT;

    private final CollectionGUIEvent event;
    private final CardArt art;
    private final JPanel map = new JPanel(null) {
        @Override
        protected void paintComponent(Graphics source) {
            Graphics2D g = (Graphics2D) source.create();
            UiStyle.prepare(g);
            for (int v = 0; v < scales.length; v++) {
                scales[v] += ((v == hovered ? 1.12 : 1.0) - scales[v]) * 0.3;  // 부드럽게 커지고 줄어든다
            }
            long now = System.nanoTime();
            double unlock = locked ? 0 : unlockStart < 0 ? 1 : Math.min(1, (now - unlockStart) / 1e9 / UNLOCK_SECONDS);
            double[] reveal = new double[VILLAGES.length];
            for (int v = 0; v < reveal.length; v++) {
                reveal[v] = !restored[v] ? 0 : restoreStart[v] < 0 ? 1 : progress(now, restoreStart[v], 0, RESTORE_SECONDS);
            }
            // 봉인이 깨진 뒤(약 1초) 금빛이 지도 전체로 퍼진다
            double world = locked ? 0 : unlockStart < 0 ? 1 : progress(now, unlockStart, UNLOCK_SECONDS * 0.31, WORLD_SECONDS);
            art.paintMap(g, getWidth(), getHeight(), (now - startNanos) / 1e9, scales, LOCKED, unlock, lockHint, reveal, world);
            g.dispose();
        }
    };
    private static final double RESTORE_SECONDS = 2.0, WORLD_SECONDS = 3.0;
    private final boolean[] restored = new boolean[VILLAGES.length];
    private final long[] restoreStart = {-1, -1, -1, -1};
    private final long startNanos = System.nanoTime();

    /** start에서 delay초 뒤부터 seconds초 동안 0→1. */
    private static double progress(long now, long start, double delay, double seconds) {
        return Math.min(1, Math.max(0, ((now - start) / 1e9 - delay) / seconds));
    }
    private final double[] scales = {1, 1, 1, 1};
    private int hovered = -1;
    private boolean locked;
    private long unlockStart = -1;
    private String lockHint = "";
    // 지도가 화면에 보일 때만 바다를 다시 그린다(약 30fps)
    private final Timer waves = new Timer(33, e -> {
        if (map.isShowing()) map.repaint();
        else ((Timer) e.getSource()).stop();
    });
    private final Map<String, UiButton> villageButtons = new LinkedHashMap<>();
    private final JPanel list = new JPanel(null);
    private final CardSliderPanel slider;
    private final CardDetailPanel detail;
    private String selected = "";
    private String pendingVillage;
    private final JPanel villageActions = org.kgu.window.GameTheme.panel(new BorderLayout(0, 14));
    private final JLabel villageTitle = new JLabel("", SwingConstants.CENTER);
    private int collectedPersons;
    private int totalPersons;
    private int collectedStages;
    public void showGateNotice(int solved) { showMap(); }
    private java.util.function.Consumer<String> adventure = category -> { };
    public void configureAdventure(java.util.function.Consumer<String> open) { adventure = open; }
    public void stopAnimations() { waves.stop(); }
    @Override public void removeNotify() { stopAnimations(); super.removeNotify(); }

    /**
     * @param event 마을 고르기, 인물 열기, 메인으로 가기를 처리하는 이벤트
     */
    public CollectionView(CollectionGUIEvent event) {
        this.event = event;
        art = new CardArt(event::image);
        setLayout(null);

        map.setBounds(0, 0, 780, 560);
        for (int i = 0; i < VILLAGES.length; i++) {
            String category = VILLAGES[i];
            UiButton button = CardArt.button(villageName(category) + " 마을", Color.WHITE, 16f, () -> chooseVillage(category));
            button.setName("mapVillage" + category);
            button.putClientProperty("selectionTint", new Color(39, 61, 84));
            button.setBounds(LABELS[i].x, LABELS[i].y, 190, 44);
            int village = i;
            button.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hovered = event.categoryAvailable(category) ? village : -1;
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hovered = -1;
                }
            });
            villageButtons.put(category, button);
            map.add(button);
        }
        JButton mapMenu = CardArt.button("메인으로", Color.WHITE, 14f, event::returnToMenu);
        mapMenu.setBounds(330, 510, 120, 38);
        map.add(mapMenu);
        JButton challengeMap = CardArt.button("준비 · 상점", Color.WHITE, 14f, () -> adventure.accept(selected));
        challengeMap.setBounds(510, 510, 190, 38); map.add(challengeMap);
        // 이름표 말고 마을 섬 자체를 눌러도 들어간다(바다·가운데 광장은 안 눌림)
        MouseAdapter villageMouse = new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                hovered = art.villageAt(e.getX(), e.getY(), map.getWidth(), map.getHeight());
                if (hovered >= 0 && !event.categoryAvailable(VILLAGES[hovered])) hovered = -1;
                map.setCursor(Cursor.getPredefinedCursor(hovered < 0 ? Cursor.DEFAULT_CURSOR : Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hovered = -1;
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                int village = art.villageAt(e.getX(), e.getY(), map.getWidth(), map.getHeight());
                if (village >= 0) {
                    chooseVillage(VILLAGES[village]);
                }
            }
        };
        map.addMouseListener(villageMouse);
        map.addMouseMotionListener(villageMouse);
        villageActions.setBounds(205, 175, 370, 220);
        villageActions.setBorder(BorderFactory.createEmptyBorder(22, 24, 22, 24));
        villageTitle.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 22f));
        villageTitle.setForeground(org.kgu.window.GameTheme.PAPER); villageActions.add(villageTitle, BorderLayout.NORTH);
        JPanel routes = new JPanel(new GridLayout(2, 1, 0, 12)); routes.setOpaque(false);
        JButton enter = CardArt.button("도전하기  →", CardArt.BLUE, 18f, () -> {
            if (pendingVillage != null && event.categoryAvailable(pendingVillage)) {
                villageActions.setVisible(false); adventure.accept(pendingVillage);
            }
        });
        enter.setName("villageChallenge"); enter.putClientProperty("selectionTint", new Color(43, 104, 165)); routes.add(enter);
        JButton cards = CardArt.button("수집 카드 보기", Color.WHITE, 17f, () -> {
            if (pendingVillage != null) { villageActions.setVisible(false); event.selectCategory(this, pendingVillage); }
        });
        cards.setName("villageCollection"); cards.putClientProperty("selectionTint", new Color(39, 61, 84)); routes.add(cards);
        villageActions.add(routes);
        JButton close = CardArt.button("지도로 돌아가기", Color.WHITE, 14f, () -> villageActions.setVisible(false));
        close.setName("villageCancel"); close.putClientProperty("selectionTint", new Color(39, 61, 84));
        villageActions.add(close, BorderLayout.SOUTH); villageActions.setVisible(false);
        map.add(villageActions); map.setComponentZOrder(villageActions, 0);
        mapMenu.putClientProperty("selectionTint", new Color(39, 61, 84));
        challengeMap.putClientProperty("selectionTint", new Color(39, 61, 84));
        add(map);

        list.setOpaque(false);
        list.setBounds(0, 0, 780, 560);
        list.setVisible(false);
        add(list);
        slider = new CardSliderPanel(event, art, person -> event.openPerson(this, person));
        slider.setBounds(221, 106, 530, 396);
        list.add(slider);
        JButton toMap = CardArt.button("지도로", CardArt.BLUE, 16f, this::showMap);
        toMap.setBounds(16, 92, 180, 46);
        list.add(toMap);
        JButton challenge = CardArt.button("마을 도전 · 장착", CardArt.BLUE, 16f, () -> adventure.accept(selected));
        challenge.setBounds(16, 150, 180, 46); list.add(challenge);
        JButton menu = CardArt.button("메인으로", Color.WHITE, 16f, event::returnToMenu);
        menu.setBounds(16, 506, 180, 46);
        list.add(menu);

        detail = new CardDetailPanel(event, art, this::showList);
        detail.setBounds(0, 0, 780, 560);
        detail.setVisible(false);
        add(detail);
        for (Component child : getComponents()) scale(child);
    }

    private void chooseVillage(String category) {
        if (!event.categoryAvailable(category)) { showGateNotice(event.rescueProgress()); return; }
        pendingVillage = category;
        villageTitle.setText(villageName(category) + " 마을");
        villageActions.setVisible(true); map.repaint();
    }

    /** 780x560 기준으로 놓은 부품의 위치·크기·글자 크기를 창 크기에 맞게 늘린다(자식까지). */
    static void scale(Component c) {
        Rectangle b = c.getBounds();
        c.setBounds((int) Math.round(b.x * SX), (int) Math.round(b.y * SY), (int) Math.round(b.width * SX), (int) Math.round(b.height * SY));
        if (c.isPreferredSizeSet()) {
            Dimension d = c.getPreferredSize();
            c.setPreferredSize(new Dimension((int) Math.round(d.width * SX), (int) Math.round(d.height * SY)));
        }
        Font f = c.getFont();
        if (f != null) c.setFont(f.deriveFont((float) (f.getSize2D() * SX)));
        // CardView는 그림을 자기 크기에 맞춰 그리므로 안쪽은 건드리지 않는다
        if (c instanceof Container container && !(c instanceof CardView)) {
            for (Component child : container.getComponents()) scale(child);
        }
    }

    /**
     * 고른 마을의 카드 목록과 진행도, 지도 이름표의 수집 수를 새로 채운다. 보이는 페이지는 바꾸지 않는다.
     *
     * @param selected 고른 분야(마을) 이름
     * @param persons 목록에 보여 줄 인물
     * @param collectedPersons 그 분야에서 모은 인물 수
     * @param totalPersons 그 분야의 전체 인물 수
     * @param collectedStages 그 분야에서 모은 단계 카드 수
     */
    public void display(String selected, List<Person> persons, int collectedPersons, int totalPersons, int collectedStages) {
        this.selected = selected;
        this.collectedPersons = collectedPersons;
        this.totalPersons = totalPersons;
        this.collectedStages = collectedStages;
        villageButtons.forEach((category, button) -> {
            boolean available = event.categoryAvailable(category); button.setEnabled(available);
            button.setText(available ? villageName(category) + " 마을 " + event.getCollectedCount(category) + "/" + event.getPersonCount(category) : "교수님 잠김 " + event.rescueProgress() + "/30");
        });
        slider.setPersons(persons);
        repaint();
    }

    /** 마을 지도를 보여 주고 바다 물결을 움직인다. */
    public void showMap() {
        villageActions.setVisible(false); pendingVillage = null;
        hovered = -1;
        locked = !event.isVillageOpen(CollectionGUIEvent.LOCKED_VILLAGE);
        // 열린 뒤 처음 지도를 볼 때 계정마다 한 번만 풀리는 모션을 튼다(본 기록은 세이브 옆 파일에 남는다)
        if (event.takeUnlockMotion(CollectionGUIEvent.LOCKED_VILLAGE)) unlockStart = System.nanoTime();
        // 교수님 섬은 따로 복구되지 않고, 교수님 마을이 열릴 때 지도 전체와 함께 돌아온다
        for (int v = 0; v < VILLAGES.length; v++) {
            restored[v] = v != LOCKED && event.isVillageRestored(VILLAGES[v]);
            if (restored[v] && event.takeRestoreMotion(VILLAGES[v])) restoreStart[v] = System.nanoTime();
        }
        lockHint = "다른 분야 문제 " + event.rescueProgress() + "/"
            + org.kgu.progression.PlayerProgress.PROFESSOR_REQUIRED + " 정답이면 열려요";
        java.util.Arrays.fill(scales, 1);
        show(map);
        waves.start();
    }

    /** 고른 마을의 카드 목록을 보여 준다. */
    public void showList() {
        show(list);
    }

    /**
     * 인물 상세 화면을 보여 준다.
     *
     * @param person 보여 줄 인물
     * @param owned 상세 화면에서 넘겨 볼 수 있는, 모은 인물 목록
     */
    public void showPerson(Person person, List<Person> owned) {
        detail.display(person, owned);
        show(detail);
    }

    /** 도감에서는 교수님 분야를 "경기대"로 부른다(분야 데이터 이름은 그대로). */
    private static String villageName(String category) {
        return CollectionGUIEvent.LOCKED_VILLAGE.equals(category) ? "경기대" : category;
    }

    private int openOrNone(int village) {
        return village >= 0 && event.isVillageOpen(VILLAGES[village]) ? village : -1;
    }

    private void show(JPanel page) {
        if (page != map) waves.stop();
        map.setVisible(page == map);
        list.setVisible(page == list);
        detail.setVisible(page == detail);
        repaint();
    }

    @Override
    public ViewId getViewId() {
        return ViewId.COLLECTION;
    }

    @Override
    public JComponent getComponent() {
        return this;
    }

    @Override
    public Dimension getViewSize() {
        return new Dimension(WIDTH, HEIGHT);
    }

    @Override
    protected void paintComponent(Graphics source) {
        Graphics2D g = (Graphics2D) source.create();
        UiStyle.prepare(g);

        art.paintBackground(g, getWidth(), getHeight());

        if (list.isVisible()) {
            g.scale(SX, SY);
            // 도전 준비 화면처럼: 왼쪽 위 제목, 어두운 패널, 금색 포인트
            g.setColor(GameTheme.MUTED);
            g.setFont(UiStyle.FONT.deriveFont(12f));
            g.drawString("카드 도감", 18, 34);
            g.setColor(GameTheme.PAPER);
            g.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 24f));
            g.drawString(villageName(selected) + " 마을", 16, 66);
            GameTheme.paintSurface(g, 290, 18, 470, 62, 12, false);
            g.setColor(GameTheme.PAPER);
            g.setFont(UiStyle.FONT.deriveFont(15f));
            UiStyle.center(g, "수집한 인물 " + collectedPersons + " / " + totalPersons + " · 단계 카드 " + collectedStages + " / " + totalPersons * 3, 525, 43);
            UiStyle.round(g, 320, 54, 410, 10, 10, GameTheme.INK);
            UiStyle.round(g, 320, 54, totalPersons == 0 ? 0 : 410 * collectedPersons / totalPersons, 10, 10, GameTheme.TEAL);
            GameTheme.paintSurface(g, 210, 90, 552, 432, 12, false);
        }

        g.dispose();
    }
}
