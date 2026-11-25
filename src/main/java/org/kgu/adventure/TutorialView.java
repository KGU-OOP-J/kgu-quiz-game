package org.kgu.adventure;

import org.kgu.game.feature.GameFeature;
import org.kgu.window.*;
import javax.swing.*;
import java.awt.*;

public final class TutorialView extends JPanel implements View {
    private static final String[][] PAGES = {
        {"WHO’S NEXT? 탐험을 시작해요", "인물 퀴즈를 풀고 카드를 모아 네 마을을 탐험하는 게임입니다.\n\n연습 → 카드 획득 → 카드 장착 → 인물 도전 → 마을 보스 순서로 진행해 보세요.\n\n사용자 ID별로 진행 상황이 이 컴퓨터에 저장됩니다. 온라인 계정이나 결제 기능은 없습니다."},
        {"정답뿐 아니라 행동도 중요해요", "일반 공격: 정답일 때 기본 피해, 반격 일부 회피\n강공격: 정답일 때 큰 피해, 오답이면 더 큰 반격\n방어·집중: 피해를 줄이고 정답 시 스킬 자원을 더 획득\n\n상대는 견제 → 힘 모으기 → 강공격 패턴을 반복합니다. 다음 행동 예고를 보고 선택하세요. 아래 버튼으로 연습해 보세요."},
        {"스킬과 연속 정답", "스킬 자원은 정답과 콤보로 충전됩니다. 최대 10까지 보관합니다.\n\n분석: 오답 보기 제거 (자원 2, 분석 카드 장착 시 1)\n보호: 다음 반격 피해 15 감소 (자원 2)\n집중: 다음 정답 공격 피해 15 증가 (자원 2)\n\n분석과 분석 렌즈를 사용하면 힌트 미사용 별은 받을 수 없습니다."},
        {"모은 인물 카드를 사용하세요", "연습 정답이나 배틀 승리로 인물 카드를 얻습니다. 탐험가 길드 → 카드 장착에서 지원 카드 1장을 선택하세요.\n\n인물에 따라 분석·돌파·수호·집중 버프가 적용됩니다. 예: 뉴턴은 공격 강화, 이순신은 수호.\n높은 난이도의 단계 카드와 3회 단위 숙련도 성장이 버프를 강화합니다. 장착한 카드가 없으면 견습 탐험가로 시작합니다."},
        {"상점 · 성장 · 도전과제", "전투와 연습을 마치면 XP와 코인을 얻고 레벨이 오릅니다. 레벨 업으로 전투 최대 HP가 조금 증가합니다.\n\n시작 코인 120으로 분석 렌즈·보호 부적·회복 물약을 구매할 수 있습니다. 각 종류는 전투당 1개 사용 가능하며 사용한 아이템은 중단해도 반환되지 않습니다.\n\n도전과제 달성 후 보상 받기를 눌러 추가 코인을 받으세요. 튜토리얼 완료 보상도 준비되어 있습니다!"},
        {"마지막 목표는 교수님 구출", "과학자·한국사·아이돌 분야의 서로 다른 문제 30개를 맞히면 이은정 교수님의 최종 구출 경로가 열립니다. 같은 문제를 다시 맞혀도 중복 집계되지 않습니다.\n\n일반 마을은 서로 다른 인물 3명 승리 시 보스가 열립니다.\n\n별 1: 승리 · 별 2: 정답률 80% 이상 · 별 3: 분석·힌트 미사용\n일반 도전과 교수님 구출은 최대 5문제, 일반 마을 보스는 혼합 8문제입니다. 보스는 힘 모으기 턴에 보호막을 사용하며 HP 절반 이하에서는 격노합니다. 기회를 다 쓰기 전에 상대 HP를 0으로 만드세요.\n\n숫자 1~4로 보기 선택, Enter로 제출·다음 턴. 오답 노트로 복습할 수 있으며 메인에서 이 안내를 다시 볼 수 있습니다."}
    };
    private final GameFeature game;
    private final JLabel title = new JLabel(), indicator = new JLabel(), demoResult = new JLabel(" ");
    private final JTextArea body = new JTextArea();
    private final JPanel demo = new JPanel(new GridLayout(1, 3, 12, 0));
    private final JButton previous = new UiButton("이전", GameTheme.TEAL), next = new UiButton("다음", new Color(133, 101, 52));
    private int page;
    public TutorialView(GameFeature game) {
        this.game = game; setLayout(new BorderLayout(20, 25)); setBackground(GameTheme.INK);
        setBorder(BorderFactory.createEmptyBorder(55, 70, 45, 70));
        JPanel header = new JPanel(new GridLayout(2, 1, 0, 15)); header.setOpaque(false);
        title.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 35f)); title.setForeground(GameTheme.PAPER); indicator.setForeground(GameTheme.GOLD); indicator.setFont(UiStyle.FONT.deriveFont(20f)); header.add(indicator); header.add(title); add(header, BorderLayout.NORTH);
        body.setFont(UiStyle.FONT.deriveFont(22f)); body.setEditable(false); body.setLineWrap(true); body.setWrapStyleWord(true);
        body.setOpaque(false); body.setForeground(GameTheme.PAPER); body.setMargin(new Insets(28, 30, 28, 30));
        JPanel pagePanel = GameTheme.panel(new BorderLayout()); pagePanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        pagePanel.add(GameTheme.scroll(body)); add(pagePanel, BorderLayout.CENTER);
        JPanel bottom = new JPanel(new GridLayout(3, 1, 0, 12)); bottom.setOpaque(false);
        demo.setOpaque(false);
        String[] actions = {"일반 공격", "강공격", "방어·집중"};
        String[] results = {"정답! 피해 18 · 반격 6 회피 → 받은 피해 6", "정답! 피해 32 · 오답이었다면 반격 20", "정답! 피해 10 · 받은 피해 0 · 추가 자원 +2"};
        for (int i = 0; i < 3; i++) { int index = i; JButton b = new UiButton(actions[i], GameTheme.TEAL); b.setName("tutorialDemo" + i); b.setFont(UiStyle.FONT.deriveFont(20f)); b.addActionListener(e -> demoResult.setText(results[index])); demo.add(b); }
        bottom.add(demo); demoResult.setFont(UiStyle.FONT.deriveFont(19f)); demoResult.setForeground(GameTheme.GOLD); bottom.add(demoResult);
        JPanel navigation = new JPanel(new GridLayout(1, 3, 18, 0)); navigation.setOpaque(false);
        JButton later = new UiButton("다음 로그인에 다시 보기", new Color(39, 53, 72)); later.setName("tutorialLater"); later.addActionListener(e -> game.returnToMenu());
        previous.setName("tutorialPrevious"); previous.addActionListener(e -> { page--; render(); });
        next.setName("tutorialNext"); next.addActionListener(e -> advance());
        for (JButton b : new JButton[]{later, previous, next}) { b.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 20f)); navigation.add(b); }
        bottom.add(navigation); add(bottom, BorderLayout.SOUTH); reset();
    }
    public void reset() { page = 0; render(); }
    public int page() { return page; }
    private void render() {
        title.setText(PAGES[page][0]); body.setText(PAGES[page][1]); body.setCaretPosition(0);
        indicator.setText("처음 탐험 가이드 " + (page + 1) + "/" + PAGES.length);
        previous.setEnabled(page > 0); next.setText(page == PAGES.length - 1 ? "튜토리얼 완료 · 시작" : "다음");
        demo.setVisible(page == 1); demoResult.setText(" ");
    }
    private void advance() {
        if (page < PAGES.length - 1) { page++; render(); return; }
        try { game.progress().completeTutorial(); game.returnToMenu(); }
        catch (java.io.IOException e) { JOptionPane.showMessageDialog(this, "안내 완료를 저장하지 못했습니다. 다시 눌러 주세요.\n" + e.getMessage(), "저장 실패", JOptionPane.ERROR_MESSAGE); }
    }
    @Override protected void paintComponent(Graphics source) { Graphics2D g = (Graphics2D)source.create(); UiStyle.prepare(g); GameTheme.backdrop(g, getWidth(), getHeight(), "tutorial", 0.86f); g.dispose(); }
    @Override public ViewId getViewId() { return ViewId.TUTORIAL; }
    @Override public JComponent getComponent() { return this; }
    @Override public Dimension getViewSize() { return new Dimension(1280, 960); }
}
