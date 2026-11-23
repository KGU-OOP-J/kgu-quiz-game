package org.kgu.collection.view;

import org.kgu.collection.event.CollectionGUIEvent;
import org.kgu.model.Person;
import org.kgu.window.UiStyle;
import org.kgu.window.View;
import org.kgu.window.ViewId;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public final class CollectionView extends JPanel implements View {
    private final CollectionGUIEvent event;
    private final CardArt art;
    private final JPanel list = new JPanel(null);
    private final JPanel fields = new JPanel(null);
    private final CardSliderPanel slider;
    private final CardDetailPanel detail;
    private int collectedPersons;
    private int totalPersons;
    private int collectedStages;

    public CollectionView(CollectionGUIEvent event) {
        this.event = event;
        art = new CardArt(event::image);
        setLayout(null);
        list.setOpaque(false);
        list.setBounds(0, 0, 780, 560);
        add(list);
        fields.setOpaque(false);
        fields.setBounds(16, 92, 184, 300);
        list.add(fields);
        slider = new CardSliderPanel(event, art, person -> event.openPerson(this, person));
        slider.setBounds(221, 106, 530, 396);
        list.add(slider);
        detail = new CardDetailPanel(event, art, this::showList);
        detail.setBounds(0, 0, 780, 560);
        detail.setVisible(false);
        add(detail);
        JButton menu = CardArt.button("메인으로", Color.WHITE, 16f, event::returnToMenu);
        menu.setBounds(16, 506, 180, 46);
        list.add(menu);
    }

    public void display(String selected, List<String> categories, List<Person> persons, int collectedPersons, int totalPersons, int collectedStages) {
        this.collectedPersons = collectedPersons;
        this.totalPersons = totalPersons;
        this.collectedStages = collectedStages;
        fields.removeAll();
        for (int i = 0; i < categories.size(); i++) {
            String category = categories.get(i);
            JButton button = CardArt.button(category + " " + event.getCollectedCount(category) + "/" + event.getPersonCount(category),
                category.equals(selected) ? CardArt.BLUE : Color.WHITE, 14f, () -> event.selectCategory(this, category));
            button.setBounds(0, i * 54, 184, 44);
            fields.add(button);
        }
        slider.setPersons(persons);
        fields.revalidate();
        showList();
    }

    public void showPerson(Person person, List<Person> owned) {
        detail.display(person, owned);
        list.setVisible(false);
        detail.setVisible(true);
        repaint();
    }

    public void showList() {
        detail.setVisible(false);
        list.setVisible(true);
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
            CardArt.gradientRound(g, 30, 22, 214, 54, 30, new Color(44, 80, 140), new Color(22, 46, 92));
            g.setColor(Color.WHITE);
            g.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 28f));
            g.drawString("인물 도감", 65, 60);
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
