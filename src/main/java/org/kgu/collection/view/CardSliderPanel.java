package org.kgu.collection.view;

import org.kgu.collection.event.CollectionGUIEvent;
import org.kgu.model.Person;
import org.kgu.window.UiButton;
import java.awt.*;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.*;

/** 페이지당 인물 10명, 5열 2행. 페이지 선택은 표현 상태로만 보유한다. */
final class CardSliderPanel extends JPanel {
    private final CollectionGUIEvent event;
    private final CardArt art;
    private final Consumer<Person> onOpen;
    private final JPanel grid=new JPanel(null);
    private final JPanel pages=new JPanel(new FlowLayout(FlowLayout.CENTER,6,0));
    private List<Person> persons=List.of();
    private int page;
    CardSliderPanel(CollectionGUIEvent event,CardArt art,Consumer<Person> onOpen){
        this.event=event;this.art=art;this.onOpen=onOpen;setLayout(null);setOpaque(false);
        grid.setOpaque(false);grid.setBounds(0,0,530,336);add(grid);
        pages.setOpaque(false);pages.setBounds(0,352,530,44);add(pages);
    }
    void setPersons(List<Person> persons){this.persons=List.copyOf(persons);showPage(0);}
    static int pageCount(int count){return Math.max(1,(count+9)/10);}
    private void showPage(int target){
        page=Math.floorMod(target,pageCount(persons.size()));grid.removeAll();
        for(int i=0;i<10&&page*10+i<persons.size();i++){
            Person person=persons.get(page*10+i);int stage=event.getHighestStage(person);
            CardView card=new CardView(person,stage,art);
            JButton tile=new JButton(){
                @Override protected void paintComponent(Graphics g){card.setGlow(getModel().isRollover()||hasFocus());card.setSize(getSize());card.paint(g);}
            };
            tile.setBounds(i%5*106,i/5*168,106,164);tile.setContentAreaFilled(false);tile.setBorderPainted(false);
            tile.setEnabled(stage>0);tile.setRolloverEnabled(true);tile.setToolTipText(person.getName()+(stage==0?" (잠김)":""));
            tile.getAccessibleContext().setAccessibleName(tile.getToolTipText());
            tile.addActionListener(e->onOpen.accept(person));grid.add(tile);
        }
        pages.removeAll();
        if(pageCount(persons.size())>1){
            UiButton prev=CardArt.button("◀",Color.WHITE,14f,()->showPage(page-1));prev.setPreferredSize(new Dimension(40,36));pages.add(prev);
        }
        for(int i=0;i<pageCount(persons.size());i++){
            int index=i;UiButton number=CardArt.button(String.valueOf(i+1),i==page?CardArt.BLUE:Color.WHITE,16f,()->showPage(index));
            number.setPreferredSize(new Dimension(36,36));pages.add(number);
        }
        if(pageCount(persons.size())>1){UiButton next=CardArt.button("▶",Color.WHITE,14f,()->showPage(page+1));next.setPreferredSize(new Dimension(40,36));pages.add(next);}
        grid.revalidate();pages.revalidate();repaint();
    }
}
