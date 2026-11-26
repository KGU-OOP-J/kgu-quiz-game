package org.kgu.domain.adventure.event;

import javax.swing.JButton;
import javax.swing.JLabel;
import java.awt.event.ActionListener;
import java.io.IOException;

public final class AdventureGUIEvent {
    @FunctionalInterface
    public interface Command {
        void run() throws IOException;
    }

    public ActionListener action(JLabel notice, Command command) {
        return event -> {
            try {
                command.run();
            } catch (IOException | IllegalArgumentException | IllegalStateException ex) {
                notice.setText(ex.getMessage());
            }
        };
    }
}
