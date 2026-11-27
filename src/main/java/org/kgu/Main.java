package org.kgu;

import org.kgu.core.application.Application;
import org.kgu.domain.showcase.feature.ShowcaseScene;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        Application[] loaded = new Application[1];
        try {
            SwingUtilities.invokeAndWait(() -> loaded[0] = new Application());
            Application application = loaded[0];
            application.init(); application.load();
            SwingUtilities.invokeLater(() -> {
                try {
                    application.start();
                    for (String argument : args) if (argument.startsWith("--showcase="))
                        application.startShowcase(ShowcaseScene.valueOf(argument.substring("--showcase=".length())));
                } catch (Exception e) { showFailure(application, e); }
            });
        } catch (Exception e) {
            if (e instanceof InterruptedException) Thread.currentThread().interrupt();
            Throwable cause = e.getCause() == null ? e : e.getCause();
            SwingUtilities.invokeLater(() -> showFailure(loaded[0], cause));
        }
    }

    private static void showFailure(Application application, Throwable error) {
        if (application != null) application.shutdown();
        JOptionPane.showMessageDialog(null, error.getMessage(), "시작 실패", JOptionPane.ERROR_MESSAGE);
    }
}
