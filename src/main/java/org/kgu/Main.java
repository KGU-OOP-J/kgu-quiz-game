package org.kgu;

import org.kgu.application.Application;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // 프로그램의 진입점을 정의합니다.
        // Swing이 초기화 된 이후 프로그램이 실행되어야 합니다.
        SwingUtilities.invokeLater(() -> {
            Application app = new Application();
            app.run();
        });
    }
}
