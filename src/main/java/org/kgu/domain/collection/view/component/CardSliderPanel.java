package org.kgu.domain.collection.view.component;

import org.kgu.domain.collection.event.CollectionGUIEvent;
import org.kgu.core.model.Person;
import org.kgu.core.window.UiButton;
import java.awt.*;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.*;

/** 페이지당 인물 10명, 5열 2행. 페이지 선택은 표현 상태로만 보유한다. */
public final class CardSliderPanel extends JPanel {
    private final CollectionGUIEvent event;
    private final CardArt art;
    private final Consumer<Person> onOpen;
    private final JPanel grid=new JPanel(null);
    private final JPanel pages=new JPanel(new FlowLayout(FlowLayout.CENTER,6,0));
    private List<Person> persons=List.of();
    private int page;
    public CardSliderPanel(CollectionGUIEvent event,CardArt art,Consumer<Person> onOpen){
        this.event=event;this.art=art;this.onOpen=onOpen;setLayout(null);setOpaque(false);
        grid.setOpaque(false);grid.setBounds(0,0,530,336);add(grid);
        pages.setOpaque(false);pages.setBounds(0,352,530,44);add(pages);
    }
    public void setPersons(List<Person> persons){this.persons=List.copyOf(persons);showPage(0);}
    public static int pageCount(int count){return Math.max(1,(count+9)/10);}
    private void showPage(int target){
        page=Math.floorMod(target,pageCount(persons.size()));grid.removeAll();
        for(int i=0;i<10&&page*10+i<persons.size();i++){
            Person person=persons.get(page*10+i);int stage=event.getHighestStage(person);
            CardView card=new CardView(person,stage,art);
            JButton tile=new JButton(){
                @Override protected void paintComponent(Graphics g){card.setGlow(getModel().isRollover()||hasFocus());card.setSize(getSize());card.paint(g);}
            };
            tile.setContentAreaFilled(false);tile.setBorderPainted(false);
            tile.setEnabled(stage>0);tile.setRolloverEnabled(true);tile.setToolTipText(person.getName()+(stage==0?" (잠김)":""));
            tile.getAccessibleContext().setAccessibleName(tile.getToolTipText());
            tile.addActionListener(e->onOpen.accept(person));grid.add(tile);
        }
        pages.removeAll();
        if(pageCount(persons.size())>1){
            UiButton prev=CardArt.button("이전",Color.WHITE,14f,()->showPage(page-1));prev.setPreferredSize(new Dimension(48,44));pages.add(prev);
        }
        for(int i=0;i<pageCount(persons.size());i++){
            int index=i;UiButton number=CardArt.button(String.valueOf(i+1),i==page?CardArt.BLUE:Color.WHITE,16f,()->showPage(index));
            number.setPreferredSize(new Dimension(44,44));pages.add(number);
        }
        if(pageCount(persons.size())>1){UiButton next=CardArt.button("다음",Color.WHITE,14f,()->showPage(page+1));next.setPreferredSize(new Dimension(48,44));pages.add(next);}
        doLayout();
        grid.revalidate();pages.revalidate();repaint();
    }

    @Override public void doLayout() {
        int footer = Math.min(50, getHeight() / 4);
        int width = getWidth(), height = getHeight() - footer;
        grid.setBounds(0, 0, width, height);
        pages.setBounds(0, height, width, footer);
        for (int i = 0; i < grid.getComponentCount(); i++) {
            int x = (i % 5) * width / 5, y = (i / 5) * height / 2;
            grid.getComponent(i).setBounds(x, y, width / 5, height / 2);
        }
    }
}
