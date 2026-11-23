package org.kgu.setting.view;

import org.kgu.window.UiButton;
import org.kgu.window.UiStyle;
import org.kgu.window.View;
import org.kgu.window.ViewId;

import javax.swing.*;
import java.awt.*;

public final class SettingView extends JPanel implements View {
    private final JCheckBox sound = new JCheckBox("효과음 사용", true);
    private final JButton apply = new UiButton("저장", new Color(33, 127, 239));
    private final JButton back = new UiButton("메뉴로", new Color(112, 128, 144));
    private final JLabel status = new JLabel(" ", SwingConstants.CENTER);

    public SettingView() {
        setLayout(null);
        setBackground(new Color(245, 248, 252));
        JLabel title = new JLabel("설정", SwingConstants.CENTER);
        title.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 34f));
        title.setBounds(0, 130, 780, 70);
        add(title);
        sound.setBounds(260, 260, 260, 45);
        sound.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 20f));
        sound.setOpaque(false);
        add(sound);
        apply.setBounds(200, 380, 180, 55);
        back.setBounds(400, 380, 180, 55);
        for (JButton b : new JButton[]{apply, back}) {
            b.setForeground(Color.WHITE);
            b.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 20f));
            add(b);
        }
        status.setBounds(100, 450, 580, 35);
        status.setFont(UiStyle.FONT);
        add(status);
    }

    public void display(boolean enabled) {
        sound.setSelected(enabled);
        status.setText(" ");
    }

    public boolean isSoundEnabled() {
        return sound.isSelected();
    }

    public void showSaved() {
        status.setText("설정을 저장했습니다.");
    }

    public JButton getApplyButton() {
        return apply;
    }

    public JButton getBackButton() {
        return back;
    }

    @Override
    public ViewId getViewId() {
        return ViewId.SETTING;
    }

    @Override
    public JComponent getComponent() {
        return this;
    }

    @Override
    public Dimension getViewSize() {
        return new Dimension(UiStyle.WIDTH, UiStyle.HEIGHT);
    }
}
