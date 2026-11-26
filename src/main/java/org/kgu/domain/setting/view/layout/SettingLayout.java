package org.kgu.domain.setting.view.layout;

import org.kgu.core.window.*;
import org.kgu.core.ui.ComponentPainter;
import javax.swing.*;
import java.awt.*;

public final class SettingLayout extends JPanel {
    public SettingLayout(JCheckBox sound, JLabel status, JButton back, JButton apply) {
        setLayout(new GridBagLayout());
        setOpaque(false);
        add(settingsContainer(sound, status, back, apply));
    }

    private JPanel settingsContainer(JCheckBox sound, JLabel status, JButton back, JButton apply) {
        JPanel frame = GameTheme.lightPanel(new BorderLayout(0, 32));
        frame.setPreferredSize(new Dimension(660, 440));
        frame.setBorder(BorderFactory.createEmptyBorder(35, 40, 35, 40));
        frame.add(headerSection(), BorderLayout.NORTH);
        frame.add(optionsSection(sound, status), BorderLayout.CENTER);
        frame.add(actionsSection(back, apply), BorderLayout.SOUTH);
        return frame;
    }

    private JPanel headerSection() {
        JPanel heading = GameTheme.clear(new GridLayout(2, 1, 0, 13));
        heading.add(GameTheme.label("PREFERENCES", 17, ComponentPainter.MUTED));
        heading.add(GameTheme.label("여정의 설정", 34, ComponentPainter.TEXT));
        return heading;
    }

    private JPanel optionsSection(JCheckBox sound, JLabel status) {
        JPanel options = GameTheme.clear(new GridLayout(2, 1, 0, 20));
        sound.setFont(UiStyle.FONT.deriveFont(24f));
        sound.setForeground(ComponentPainter.TEXT);
        sound.setOpaque(false);
        options.add(sound);
        options.add(status);
        return options;
    }

    private JPanel actionsSection(JButton back, JButton apply) {
        JPanel buttons = GameTheme.clear(new GridLayout(1, 2, 20, 0));
        for (JButton button : new JButton[]{back, apply}) {
            button.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 22f));
            button.setPreferredSize(new Dimension(230, 56));
            buttons.add(button);
        }
        return buttons;
    }
}
