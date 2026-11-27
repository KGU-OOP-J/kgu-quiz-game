package org.kgu.domain.adventure.view.component;

import org.kgu.core.progression.PlayerProgress;
import org.kgu.core.progression.ShopItem;
import org.kgu.core.window.GameTheme;
import org.kgu.core.window.UiButton;
import org.kgu.core.window.UiStyle;
import javax.swing.*;
import java.awt.*;

/** Read-only help pages. Inventory is supplied by the feature as a snapshot. */
public final class ChallengeHelpPanel extends JPanel {
    private final CardLayout layout = new CardLayout();
    private final JPanel pages = GameTheme.clear(layout);
    private final JButton rulesTab, itemsTab;

    public ChallengeHelpPanel(PlayerProgress profile, Runnable back) {
        super(new BorderLayout(0, 22)); setOpaque(false);
        JPanel header = GameTheme.clear(new BorderLayout(0, 18));
        JPanel title = GameTheme.clear(new GridLayout(2, 1, 0, 8));
        title.add(label("도전 안내", 30, GameTheme.PAPER));
        title.add(label("규칙을 확인하고, 필요한 아이템을 준비하세요.", 17, GameTheme.MUTED));
        header.add(title, BorderLayout.NORTH);
        JPanel tabs = GameTheme.clear(new GridLayout(1, 2, 12, 0));
        rulesTab = button("전투 규칙", "helpRules", () -> showPage("rules"));
        itemsTab = button("보유 아이템", "helpItems", () -> showPage("items"));
        tabs.add(rulesTab); tabs.add(itemsTab); header.add(tabs); add(header, BorderLayout.NORTH);
        pages.add(rules(), "rules"); pages.add(items(profile), "items"); add(pages);
        JButton close = button("← 선택한 인물로 돌아가기", "helpBack", back);
        close.setPreferredSize(new Dimension(400, 54)); add(close, BorderLayout.SOUTH);
        showPage("rules");
    }
    private void showPage(String page) {
        layout.show(pages, page);
        rulesTab.putClientProperty("selectionTint", page.equals("rules") ? new Color(43, 104, 165) : new Color(39, 56, 74));
        itemsTab.putClientProperty("selectionTint", page.equals("items") ? new Color(43, 104, 165) : new Color(39, 56, 74));
        rulesTab.putClientProperty("selected", page.equals("rules")); itemsTab.putClientProperty("selected", page.equals("items"));
        rulesTab.repaint(); itemsTab.repaint();
    }
    private static JPanel rules() {
        JPanel page = GameTheme.clear(new BorderLayout(0, 18));
        JPanel facts = GameTheme.clear(new GridLayout(1, 3, 14, 0));
        facts.add(fact("5", "문제", "페이즈당 · 일반 1 / 보스 2 / 교수님 3"));
        facts.add(fact("3", "일반 통과 기준", "보스 3→4 · 교수님 3→3→4"));
        facts.add(fact("1", "사용 가능한 아이템", "종류와 관계없이 한 판에 1개"));
        facts.setPreferredSize(new Dimension(800, 170)); page.add(facts, BorderLayout.NORTH);
        JPanel detail = GameTheme.panel(new BorderLayout(0, 18)); detail.setBorder(BorderFactory.createEmptyBorder(24, 26, 24, 26));
        detail.add(label("별 3개에 도전해 보세요", 24, GameTheme.GOLD), BorderLayout.NORTH);
        JPanel stars = GameTheme.clear(new GridLayout(3, 1, 0, 12));
        stars.add(label("★  전투에서 승리", 22, GameTheme.PAPER));
        stars.add(label("★  최초 풀이 정답률 80% 이상", 22, GameTheme.PAPER));
        stars.add(label("★  아이템을 사용하지 않고 승리", 22, GameTheme.PAPER)); detail.add(stars);
        detail.add(text("목표를 먼저 채워도 해당 페이즈의 남은 문제를 계속 풀어요.\n남은 문제와 부적·지원 능력으로도 역전할 수 없으면 전투가 끝나요.", 18), BorderLayout.SOUTH);
        page.add(detail); return page;
    }
    private static JPanel items(PlayerProgress profile) {
        JPanel page = GameTheme.clear(new BorderLayout(0, 16));
        page.add(label("답안을 제출하기 전에 사용하세요. 사용한 아이템은 반환되지 않아요.", 17, GameTheme.MUTED), BorderLayout.NORTH);
        JPanel rows = GameTheme.clear(new GridLayout(3, 1, 0, 14));
        for (ShopItem item : ShopItem.values()) {
            JPanel row = GameTheme.panel(new BorderLayout(20, 10));
            row.setBorder(BorderFactory.createEmptyBorder(20, 22, 20, 22));
            JPanel heading = GameTheme.clear(new BorderLayout());
            heading.add(label(item.label(), 23, GameTheme.PAPER));
            int count = profile.inventory(item);
            JLabel stock = label("보유 " + count + "개", 21, count == 0 ? GameTheme.MUTED : GameTheme.GOLD);
            stock.setName("helpStock" + item.name()); heading.add(stock, BorderLayout.EAST); row.add(heading, BorderLayout.NORTH);
            String explanation = switch (item) {
                case INSIGHT -> "현재 문제의 오답 보기 1개를 제거해요.";
                case SHIELD -> "5문제 뒤 첫 오답 1개를 다시 풀어요. 오답이 없으면 발동하지 않아요.\n재도전 정답은 승리 조건에만 +1. 최초 정답률·별·보상은 늘어나지 않아요.";
                case TONIC -> "이번 전투가 끝날 때까지 해당 인물의 정보를 열어볼 수 있어요.";
            };
            row.add(text(explanation, 18)); rows.add(row);
        }
        page.add(rows); page.add(label("아이템은 왼쪽 ‘상점’에서 구매할 수 있어요.", 17, GameTheme.MUTED), BorderLayout.SOUTH);
        return page;
    }
    private static JPanel fact(String number, String title, String caption) {
        JPanel panel = GameTheme.panel(new BorderLayout(0, 8)); panel.setBorder(BorderFactory.createEmptyBorder(16, 14, 16, 14));
        JLabel value = label(number, 46, GameTheme.GOLD); value.setHorizontalAlignment(SwingConstants.CENTER); panel.add(value, BorderLayout.NORTH);
        JLabel name = label(title, 20, GameTheme.PAPER); name.setHorizontalAlignment(SwingConstants.CENTER); panel.add(name);
        JLabel sub = label(caption, 14, GameTheme.MUTED); sub.setHorizontalAlignment(SwingConstants.CENTER); panel.add(sub, BorderLayout.SOUTH); return panel;
    }
    private static JTextArea text(String value, int size) {
        JTextArea area = new JTextArea(value); area.setEditable(false); area.setLineWrap(true); area.setWrapStyleWord(true);
        area.setColumns(40); area.setRows(2); area.setPreferredSize(new Dimension(600, 54)); area.setOpaque(false); area.setForeground(GameTheme.MUTED);
        area.setFont(UiStyle.FONT.deriveFont((float) size)); return area;
    }
    private static JLabel label(String text, int size, Color color) {
        JLabel label = GameTheme.label(text, size, color); label.setFont(label.getFont().deriveFont(Font.BOLD)); return label;
    }
    private static JButton button(String text, String name, Runnable action) {
        JButton button = new UiButton(text, GameTheme.TEAL); button.setName(name);
        button.putClientProperty("selectionTint", new Color(39, 56, 74));
        button.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 20f)); button.setPreferredSize(new Dimension(300, 52));
        button.addActionListener(e -> action.run()); return button;
    }
}
