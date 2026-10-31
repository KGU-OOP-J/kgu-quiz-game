package org.kgu;

import javax.swing.SwingUtilities;
import org.kgu.ui.navigation.GameNavigator;

/** 프로그램 시작점: 화면 생성은 GameNavigator에게 위임한다. */
public class Main {
    public static void main(String[] args) {
        // 780x576 기준 레이아웃을 약 1400px 너비로 균일하게 확대한다.
        System.setProperty("sun.java2d.uiScale", "1.8");
        SwingUtilities.invokeLater(() -> new GameNavigator().open());
    }
}
