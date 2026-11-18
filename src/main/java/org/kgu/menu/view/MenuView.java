package org.kgu.menu.view;

import org.kgu.application.Difficulty;
import org.kgu.game.feature.GameSession;
import org.kgu.model.Person;
import org.kgu.window.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/** Design-only main hub; its events and feature calls are unchanged. */
public final class MenuView extends JPanel implements View {
    private static final int WIDTH=1280, HEIGHT=960;
    private final CardLayout layout=new CardLayout(); private final JPanel home=new Background(), setup=new SetupBackground();
    private final List<MenuCard> menuCards = new ArrayList<>();
    private final JButton game=button("게임 시작",new Color(35,133,246)), collection=button("카드 도감",new Color(239,151,43)), setting=button("설정",new Color(30,172,111));
    private final JButton login=button("로그인으로",new Color(112,128,144));
    private final JButton start=button("게임 시작",new Color(33,127,239)), back=button("메뉴로",new Color(112,128,144));
    private final JComboBox<String> category=new JComboBox<>(new String[]{"과학자","교수님","한국사","아이돌"});
    private final JComboBox<Difficulty> difficulty=new JComboBox<>(Difficulty.values()); private final JComboBox<GameSession.Mode> mode=new JComboBox<>(GameSession.Mode.values());
    private final JComboBox<Person> person=new JComboBox<>(); private final JLabel personLabel=new JLabel("배틀 상대 선택");
    private final JLabel welcome=new JLabel("무엇을 해볼까요?",SwingConstants.CENTER);
    public MenuView(){ setLayout(layout); buildHome(); buildSetup(); add(home,"home");add(setup,"setup");showHome(); }
    private void buildHome(){
        welcome.setBounds(120,160,1040,60);welcome.setFont(UiStyle.FONT.deriveFont(Font.BOLD,43f));welcome.setForeground(UiStyle.NAVY);home.add(welcome);
        JLabel guide=new JLabel("원하는 메뉴를 선택해서 나만의 인물 카드를 완성해 보세요.",SwingConstants.CENTER);guide.setBounds(150,222,980,35);guide.setFont(UiStyle.FONT.deriveFont(Font.BOLD,20f));guide.setForeground(new Color(35,76,124));home.add(guide);
        addTile(game,130,"게임 시작","분야와 난이도를 고르고\n인물 퀴즈에 도전하세요.","⚔");addTile(collection,470,"카드 도감","획득한 인물 카드를\n한눈에 확인하세요.","★");addTile(setting,810,"설정","소리와 게임 환경을\n내 취향에 맞춰보세요.","⚙");
        login.setBounds(92,760,170,46);login.setFont(UiStyle.FONT.deriveFont(Font.BOLD,17f));login.setForeground(Color.WHITE);home.add(login);
    }
    private void addTile(JButton b,int x,String title,String desc,String icon){
        MenuCard tile=new MenuCard(title,desc,icon,b);
        tile.setBounds(x,315,300,412);b.setBounds(34,330,232,58);b.setFont(UiStyle.FONT.deriveFont(Font.BOLD,22f));b.setForeground(Color.WHITE);home.add(tile);menuCards.add(tile);
    }
    private void buildSetup(){
        JLabel title=new JLabel("게임 준비",SwingConstants.CENTER);title.setBounds(0,245,WIDTH,60);title.setFont(UiStyle.FONT.deriveFont(Font.BOLD,38f));title.setForeground(UiStyle.NAVY);setup.add(title);
        addChoice("분야 선택",category,365);addChoice("난이도 선택",difficulty,430);addChoice("게임 모드",mode,495);personLabel.setBounds(395,560,185,38);personLabel.setFont(UiStyle.FONT.deriveFont(Font.BOLD,17f));personLabel.setForeground(Color.BLACK);setup.add(personLabel);person.setBounds(585,560,300,38);person.setForeground(Color.BLACK);setup.add(person);start.setBounds(650,660,235,55);back.setBounds(395,660,235,55);for(JButton b:new JButton[]{start,back}){b.setForeground(Color.WHITE);b.setFont(UiStyle.FONT.deriveFont(Font.BOLD,20f));setup.add(b);}
    }
    private void addChoice(String text,JComboBox<?> box,int y){JLabel l=new JLabel(text);l.setBounds(395,y,185,38);l.setFont(UiStyle.FONT.deriveFont(Font.BOLD,17f));l.setForeground(Color.BLACK);setup.add(l);box.setBounds(585,y,300,38);box.setFont(UiStyle.FONT.deriveFont(17f));box.setForeground(Color.BLACK);box.setBackground(Color.WHITE);setup.add(box);}
    private static JButton button(String t,Color c){return new UiButton(t,c);}
    public void setPlayerId(String id){welcome.setText(id+"님, 무엇을 해볼까요?");} public void showHome(){layout.show(this,"home");SwingUtilities.invokeLater(()->{for(int i=0;i<menuCards.size();i++)menuCards.get(i).appear(i*100);});} public void showSetup(){layout.show(this,"setup");updateMode();}
    public void updateMode(){boolean battle=getMode()==GameSession.Mode.BATTLE;person.setVisible(battle);personLabel.setVisible(battle);} public void setPersons(List<Person> p){person.setModel(new DefaultComboBoxModel<>(p.toArray(new Person[0])));}
    public String getCategory(){return (String)category.getSelectedItem();} public Difficulty getDifficulty(){return (Difficulty)difficulty.getSelectedItem();} public GameSession.Mode getMode(){return (GameSession.Mode)mode.getSelectedItem();} public Person getSelectedPerson(){return (Person)person.getSelectedItem();}
    public JButton getGameButton(){return game;}public JButton getCollectionButton(){return collection;}public JButton getSettingButton(){return setting;}public JButton getLoginButton(){return login;}public JButton getStartButton(){return start;}public JButton getBackButton(){return back;}public JComboBox<String> getCategoryBox(){return category;}public JComboBox<GameSession.Mode> getModeBox(){return mode;}
    @Override public ViewId getViewId(){return ViewId.MENU;}@Override public JComponent getComponent(){return this;}@Override public Dimension getViewSize(){return new Dimension(WIDTH,HEIGHT);}
    private static class Background extends JPanel{private final Image image=new ImageIcon(Background.class.getResource("/org/kgu/battle/campus_background.jpg")).getImage();Background(){setLayout(null);setOpaque(false);}@Override protected void paintComponent(Graphics g){g.drawImage(image,0,0,getWidth(),getHeight(),null);}}
    private static final class MenuCard extends JPanel {
        private final String title, description, icon; private final JButton button; private float alpha; private boolean hover; private Timer timer;
        MenuCard(String title,String description,String icon,JButton button){this.title=title;this.description=description;this.icon=icon;this.button=button;setLayout(null);setOpaque(false);add(button);MouseAdapter listener=new MouseAdapter(){@Override public void mouseEntered(MouseEvent e){hover=true;repaint();}@Override public void mouseExited(MouseEvent e){hover=false;repaint();}};addMouseListener(listener);button.addMouseListener(listener);}
        void appear(int delay){if(timer!=null)timer.stop();alpha=0;button.setVisible(false);timer=new Timer(16,null);timer.setInitialDelay(delay);timer.addActionListener(e->{alpha=Math.min(1f,alpha+.09f);if(alpha>.55f)button.setVisible(true);repaint();if(alpha>=1f)((Timer)e.getSource()).stop();});timer.start();}
        @Override protected void paintComponent(Graphics source){Graphics2D g=(Graphics2D)source.create();UiStyle.prepare(g);g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,alpha));g.setColor(hover?new Color(255,255,255,255):new Color(255,255,255,242));g.fillRoundRect(0,0,getWidth(),getHeight(),36,36);g.setColor(hover?new Color(93,185,255):new Color(255,255,255,235));g.setStroke(new BasicStroke(hover?5f:2f));g.drawRoundRect(2,2,getWidth()-5,getHeight()-5,36,36);if(hover){g.setColor(new Color(95,190,255,55));g.fillRoundRect(8,8,getWidth()-16,getHeight()-16,30,30);}g.setColor(UiStyle.NAVY);g.setFont(UiStyle.FONT.deriveFont(Font.BOLD,56f));UiStyle.center(g,icon,150,100);g.setFont(UiStyle.FONT.deriveFont(Font.BOLD,30f));UiStyle.center(g,title,150,174);g.setFont(UiStyle.FONT.deriveFont(Font.BOLD,16f));String[] lines=description.split("\\n");UiStyle.center(g,lines[0],150,222);UiStyle.center(g,lines[1],150,248);g.dispose();}
    }
    private static final class SetupBackground extends Background {@Override protected void paintComponent(Graphics source){super.paintComponent(source);Graphics2D g=(Graphics2D)source.create();UiStyle.prepare(g);g.setPaint(new GradientPaint(320,185,new Color(255,255,255,246),320,785,new Color(219,238,255,238)));g.fillRoundRect(320,185,640,600,38,38);g.setColor(new Color(255,255,255,235));g.setStroke(new BasicStroke(2));g.drawRoundRect(320,185,640,600,38,38);g.dispose();}}
}
