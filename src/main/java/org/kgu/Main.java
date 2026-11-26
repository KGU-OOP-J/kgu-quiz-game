package org.kgu;

import org.kgu.core.application.Application;

import javax.swing.*;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Application application = new Application();

            try {
                application.init();
                application.load();
                application.start();
            }
            catch (IOException e) {
                application.shutdown();
                JOptionPane.showMessageDialog(null,
                    e.getMessage(),
                    "시작 실패", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
