package org.kgu.game;

import java.util.Scanner;
import org.kgu.mgr.Manageable;

/** Manager가 관리하는 메인 메뉴 항목. */
public class GameMenuItem implements Manageable {
    private final String key;
    private final String title;

    public GameMenuItem(String key, String title) { this.key = key; this.title = title; }
    public String getTitle() { return title; }
    @Override public void read(Scanner scan) { }
    @Override public void print() { System.out.println(title); }
    @Override public boolean matches(String keyword) { return key.equals(keyword); }
}
