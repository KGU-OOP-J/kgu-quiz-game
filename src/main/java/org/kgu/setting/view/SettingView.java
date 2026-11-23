package org.kgu.setting.view;

import org.kgu.window.*;
import javax.swing.*;
import java.awt.*;

/** Design-only setting view. Setting events and persistence remain unchanged. */
public final class SettingView extends JPanel implements View {
    private static final int WIDTH=1280, HEIGHT=960;
    private final Image campus=new ImageIcon(SettingView.class.getResource("/org/kgu/battle/campus_background.jpg")).getImage();
    private final JCheckBox sound=new JCheckBox("효과음 사용",true);
    private final JButton apply=new UiButton("저장",new Color(33,127,239)); private final JButton back=new UiButton("메뉴로",new Color(112,128,144));
    private final JLabel status=new JLabel(" ",SwingConstants.CENTER);
    public SettingView(){setLayout(null);setOpaque(false);sound.setBounds(464,460,360,54);sound.setFont(UiStyle.FONT.deriveFont(Font.BOLD,24f));sound.setForeground(UiStyle.NAVY);sound.setOpaque(false);add(sound);apply.setBounds(390,610,235,62);back.setBounds(655,610,235,62);for(JButton b:new JButton[]{apply,back}){b.setForeground(Color.WHITE);b.setFont(UiStyle.FONT.deriveFont(Font.BOLD,23f));add(b);}status.setBounds(320,700,640,35);status.setFont(UiStyle.FONT.deriveFont(Font.BOLD,16f));status.setForeground(UiStyle.NAVY);add(status);}
    public void display(boolean enabled){sound.setSelected(enabled);status.setText(" ");}public boolean isSoundEnabled(){return sound.isSelected();}public void showSaved(){status.setText("설정이 저장되었습니다.");}public JButton getApplyButton(){return apply;}public JButton getBackButton(){return back;}
    @Override public ViewId getViewId(){return ViewId.SETTING;}@Override public JComponent getComponent(){return this;}@Override public Dimension getViewSize(){return new Dimension(WIDTH,HEIGHT);}
    @Override protected void paintComponent(Graphics source){super.paintComponent(source);Graphics2D g=(Graphics2D)source.create();UiStyle.prepare(g);g.drawImage(campus,0,0,getWidth(),getHeight(),null);g.setColor(new Color(7,77,154,22));g.fillRect(0,0,getWidth(),getHeight());glass(g,340,245,600,510,40);g.setColor(UiStyle.NAVY);g.setFont(UiStyle.FONT.deriveFont(Font.BOLD,43f));UiStyle.center(g,"설정",640,375);g.setFont(UiStyle.FONT.deriveFont(Font.BOLD,19f));g.setColor(new Color(46,84,125));UiStyle.center(g,"게임 환경을 내 취향에 맞춰보세요.",640,420);g.dispose();}
    private static void glass(Graphics2D g,int x,int y,int w,int h,int a){g.setPaint(new GradientPaint(x,y,new Color(255,255,255,244),x,y+h,new Color(220,239,255,232)));g.fillRoundRect(x,y,w,h,a,a);g.setColor(new Color(255,255,255,230));g.setStroke(new BasicStroke(2));g.drawRoundRect(x,y,w,h,a,a);}
}
