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

public final class CollectionView extends JPanel implements View {
    // 지도 그림(images/ui/map.png)에서 마을이 있는 사분면 순서: 왼쪽 위, 오른쪽 위, 왼쪽 아래, 오른쪽 아래
    private static final String[] VILLAGES = {"과학자", "교수님", "한국사", "아이돌"};
    private static final Point[] LABELS = {new Point(120, 200), new Point(480, 200), new Point(110, 470), new Point(480, 470)};

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
        JButton menu = CardArt.button("메인으로", Color.WHITE, 16f, event::returnToMenu);
        menu.setBounds(16, 506, 180, 46);
        list.add(menu);

        detail = new CardDetailPanel(event, art, this::showList);
        detail.setBounds(0, 0, 780, 560);
        detail.setVisible(false);
        add(detail);
    }

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

    public void showMap() {
        hovered = -1;
        java.util.Arrays.fill(scales, 1);
        show(map);
        waves.start();
    }

    public void showList() {
        show(list);
    }

    public void showPerson(Person person, List<Person> owned) {
        detail.display(person, owned);
        show(detail);
    }

    private void show(JPanel page) {
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
        return new Dimension(UiStyle.WIDTH, UiStyle.HEIGHT);
    }

    @Override
    protected void paintComponent(Graphics source) {
        Graphics2D g = (Graphics2D) source.create();
        UiStyle.prepare(g);

        art.paintBackground(g, getWidth(), getHeight());

        if (list.isVisible()) {
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
