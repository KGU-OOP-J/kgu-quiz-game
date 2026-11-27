package org.kgu.domain.setting.view;

import org.kgu.domain.setting.view.layout.SettingLayout;

import org.kgu.core.window.*;
import org.kgu.core.ui.ComponentPainter;
import javax.swing.*;
import java.awt.*;

public final class SettingView extends JPanel implements View {
    private final JCheckBox sound = new JCheckBox("효과음 사용", true);
    private final JButton apply = new UiButton("설정 저장", GameTheme.TEAL), back = new UiButton("메뉴로", new Color(44, 55, 72));
    private final JLabel status = GameTheme.label(" ", 17, ComponentPainter.CORRECT);
    public SettingView() {
        super(new BorderLayout());
        setBackground(GameTheme.PAGE);
        back.putClientProperty("secondaryAction", true);
        add(new SettingLayout(sound, status, back, apply), BorderLayout.CENTER);
    }

    public void display(boolean enabled) { sound.setSelected(enabled); status.setText(" "); }
    public boolean isSoundEnabled() { return sound.isSelected(); }
    public void showSaved() { status.setText("설정이 저장되었습니다."); }
    public JButton getApplyButton() { return apply; }
    public JButton getBackButton() { return back; }
    @Override public ViewId getViewId() { return ViewId.SETTING; }
    @Override public JComponent getComponent() { return this; }
    @Override public Dimension getViewSize() { return new Dimension(1280, 960); }
    @Override protected void paintComponent(Graphics source) {
        GameTheme.paintBackdrop(source, this, "settings", 0.14f);
    }
}
