package org.kgu.collection.view;

import org.kgu.collection.event.CollectionGUIEvent;
import org.kgu.model.Person;
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
    private static final Point[] LABELS = {new Point(120, 200), new Point(480, 200), new Point(110, 470), new Point(480, 470)};
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
            art.paintMap(g, getWidth(), getHeight(), (System.nanoTime() - startNanos) / 1e9, scales);
            g.dispose();
        }
    };
    private final long startNanos = System.nanoTime();
    private final double[] scales = {1, 1, 1, 1};
    private int hovered = -1;
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
    private int collectedPersons;
    private int totalPersons;
    private int collectedStages;
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
            UiButton button = CardArt.button(category + " 마을", Color.WHITE, 16f, () -> event.selectCategory(this, category));
            button.setBounds(LABELS[i].x, LABELS[i].y, 190, 44);
            int village = i;
            button.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hovered = village;
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
        JButton challengeMap = CardArt.button("도전 · 카드 장착", Color.WHITE, 14f, () -> adventure.accept(selected));
        challengeMap.setBounds(510, 510, 190, 38); map.add(challengeMap);
        // 이름표 말고 마을 섬 자체를 눌러도 들어간다(바다·가운데 광장은 안 눌림)
        MouseAdapter villageMouse = new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                hovered = art.villageAt(e.getX(), e.getY(), map.getWidth(), map.getHeight());
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
                    event.selectCategory(CollectionView.this, VILLAGES[village]);
                }
            }
        };
        map.addMouseListener(villageMouse);
        map.addMouseMotionListener(villageMouse);
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
        villageButtons.forEach((category, button) -> button.setText(
            category + " 마을 " + event.getCollectedCount(category) + "/" + event.getPersonCount(category)));
        slider.setPersons(persons);
        repaint();
    }

    /** 마을 지도를 보여 주고 바다 물결을 움직인다. */
    public void showMap() {
        hovered = -1;
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
            CardArt.gradientRound(g, 16, 22, 214, 54, 30, new Color(44, 80, 140), new Color(22, 46, 92));
            g.setColor(Color.WHITE);
            g.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 26f));
            UiStyle.center(g, selected + " 마을", 123, 49);
            CardArt.panel(g, 290, 18, 470, 62, 18, new Color(255, 255, 255, 235));
            g.setColor(UiStyle.NAVY);
            g.setFont(UiStyle.FONT.deriveFont(15f));
            UiStyle.center(g, "수집한 인물 " + collectedPersons + " / " + totalPersons + " · 단계 카드 " + collectedStages + " / " + totalPersons * 3, 525, 43);
            UiStyle.round(g, 320, 54, 410, 14, 14, new Color(222, 229, 240));
            UiStyle.round(g, 320, 54, totalPersons == 0 ? 0 : 410 * collectedPersons / totalPersons, 14, 14, CardArt.BLUE);
            CardArt.panel(g, 210, 90, 552, 432, 20, new Color(255, 255, 255, 200));
        }

        g.dispose();
    }
}
