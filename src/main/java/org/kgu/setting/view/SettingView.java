package org.kgu.setting.view;

import org.kgu.window.*;
import javax.swing.*;
import java.awt.*;

public final class SettingView extends JPanel implements View {
    private final JCheckBox sound = new JCheckBox("효과음 사용", true);
    private final JButton apply = new UiButton("설정 저장", GameTheme.TEAL), back = new UiButton("메뉴로", new Color(44, 55, 72));
    private final JLabel status = GameTheme.label(" ", 17, GameTheme.GOLD);
    public SettingView() {
        setLayout(new GridBagLayout());
        JPanel frame = GameTheme.panel(new BorderLayout(0, 32)); frame.setPreferredSize(new Dimension(660, 440)); frame.setBorder(BorderFactory.createEmptyBorder(35, 40, 35, 40));
        JPanel heading = GameTheme.clear(new GridLayout(2, 1, 0, 13)); heading.add(GameTheme.label("PREFERENCES", 17, GameTheme.GOLD)); heading.add(GameTheme.label("여정의 설정", 34, GameTheme.PAPER)); frame.add(heading, BorderLayout.NORTH);
        JPanel options = GameTheme.clear(new GridLayout(2, 1, 0, 20)); sound.setFont(UiStyle.FONT.deriveFont(24f)); sound.setForeground(GameTheme.PAPER); sound.setOpaque(false); options.add(sound); options.add(status); frame.add(options);
        JPanel buttons = GameTheme.clear(new GridLayout(1, 2, 20, 0));
        for (JButton b : new JButton[]{back, apply}) { b.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 22f)); b.setPreferredSize(new Dimension(230, 56)); buttons.add(b); } frame.add(buttons, BorderLayout.SOUTH); add(frame);
    }
    public void display(boolean enabled) { sound.setSelected(enabled); status.setText(" "); }
    public boolean isSoundEnabled() { return sound.isSelected(); }
    public void showSaved() { status.setText("설정이 저장되었습니다."); }
    public JButton getApplyButton() { return apply; }
    public JButton getBackButton() { return back; }
    @Override protected void paintComponent(Graphics source) { Graphics2D g = (Graphics2D)source.create(); UiStyle.prepare(g); GameTheme.backdrop(g, getWidth(), getHeight(), "settings", 0.87f); g.dispose(); }
    @Override public ViewId getViewId() { return ViewId.SETTING; }
    @Override public JComponent getComponent() { return this; }
    @Override public Dimension getViewSize() { return new Dimension(1280, 960); }
}
