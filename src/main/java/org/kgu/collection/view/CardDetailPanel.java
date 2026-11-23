package org.kgu.collection.view;

import org.kgu.collection.event.CollectionGUIEvent;
import org.kgu.model.Person;
import org.kgu.window.*;
import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.*;

final class CardDetailPanel extends JPanel {
    private final CollectionGUIEvent event;
    private final CardArt art;
    private final JPanel portrait=new JPanel(new BorderLayout());
    private final JTextArea information=new JTextArea();
    private final JLabel name=new JLabel();
    private final JLabel category=new JLabel();
    private final JLabel stages=new JLabel();
    private int stageIndex;
    private final JButton previous=CardArt.button("◀",Color.WHITE,16f,()->step(-1));
    private final JButton next=CardArt.button("▶",Color.WHITE,16f,()->step(1));
    private final JButton lower=CardArt.button("‹",Color.WHITE,18f,()->showStage(stageIndex-1));
    private final JButton higher=CardArt.button("›",Color.WHITE,18f,()->showStage(stageIndex+1));
    private List<Person> persons=List.of();
    private List<Integer> ownedStages=List.of();
    private int personIndex;
    CardDetailPanel(CollectionGUIEvent event,CardArt art,Runnable back){
        this.event=event;this.art=art;setLayout(null);setOpaque(false);
        portrait.setOpaque(false);portrait.setBounds(74,50,248,364);add(portrait);
        name.setBounds(344,60,360,45);name.setFont(UiStyle.FONT.deriveFont(Font.BOLD,26f));name.setForeground(UiStyle.NAVY);add(name);
        category.setBounds(344,110,360,40);category.setFont(UiStyle.FONT.deriveFont(18f));add(category);
        stages.setBounds(344,175,360,40);stages.setFont(UiStyle.FONT.deriveFont(Font.BOLD,22f));add(stages);
        information.setBounds(344,235,360,175);information.setFont(UiStyle.FONT.deriveFont(16f));information.setEditable(false);
        information.setLineWrap(true);information.setWrapStyleWord(true);information.setOpaque(false);add(information);
        previous.setBounds(10,205,36,48);next.setBounds(734,205,36,48);lower.setBounds(44,210,30,44);higher.setBounds(314,210,30,44);
        add(previous);add(next);add(lower);add(higher);
        JButton returnButton=CardArt.button("도감으로 돌아가기",Color.WHITE,19f,back);returnButton.setBounds(240,462,300,50);add(returnButton);
    }
    void display(Person person,List<Person> persons){
        this.persons=List.copyOf(persons);personIndex=this.persons.indexOf(person);
        if(personIndex<0)throw new IllegalArgumentException("획득한 인물만 상세를 볼 수 있습니다.");
        previous.setVisible(persons.size()>1);next.setVisible(persons.size()>1);showPerson();
    }
    private void step(int offset){personIndex=Math.floorMod(personIndex+offset,persons.size());showPerson();}
    private void showPerson(){
        Person person=persons.get(personIndex);ownedStages=event.getStages(person);
        name.setText(person.getName());category.setText(person.getCategory()+" · "+person.getIntroduction());
        information.setText(person.getAchievements().stream().map(s->"• "+s).collect(Collectors.joining("\n\n")));
        showStage(ownedStages.size()-1);
    }
    private void showStage(int index){
        stageIndex=index;int stage=ownedStages.get(index);Person person=persons.get(personIndex);
        portrait.removeAll();CardView card=new CardView(person,stage,art);card.setGlow(true);portrait.add(card,BorderLayout.CENTER);
        stages.setText("공격력 20     "+"★".repeat(stage)+"☆".repeat(3-stage));
        lower.setVisible(index>0);higher.setVisible(index<ownedStages.size()-1);portrait.revalidate();repaint();
    }
    @Override protected void paintComponent(Graphics source){
        Graphics2D g=(Graphics2D)source.create();UiStyle.prepare(g);CardArt.panel(g,54,30,672,404,24,new Color(255,255,255,240));
        CardArt.gradientRound(g,598,24,136,34,10,new Color(52,196,124),new Color(28,160,98));
        g.setColor(Color.WHITE);g.setFont(UiStyle.FONT.deriveFont(Font.BOLD,15f));g.drawString("수집 완료",616,47);CardArt.drawCheck(g,694,35,14);g.dispose();
    }
}
