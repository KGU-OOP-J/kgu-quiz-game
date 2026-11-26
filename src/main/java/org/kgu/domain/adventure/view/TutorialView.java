package org.kgu.domain.adventure.view;

import org.kgu.domain.adventure.feature.AdventureFeature;
import org.kgu.core.window.*;
import org.kgu.domain.adventure.view.layout.TutorialLayout;
import javax.swing.*;
import java.awt.*;

public final class TutorialView extends JPanel implements View {
    private static final String[][] PAGES = {
        {"WHO’S NEXT? 탐험을 시작해요", "인물 퀴즈를 풀고 카드를 모아 네 마을을 탐험합니다.\n\n연습 → 카드 획득 → 인물 도전 → 마을 보스 → 교수님 구출 순서로 진행해 보세요.\n사용자 ID별로 진행 상황을 이 컴퓨터에 저장합니다."},
        {"다섯 문제를 끝까지 풀어요", "한 인물의 같은 난이도 문제 5개에 도전합니다.\n정답 3개면 성공! 먼저 3개를 맞혀도 남은 문제를 계속 풉니다.\n\n정답이면 상대 HP가 20 감소합니다. 내 HP나 공격 방식 선택은 없습니다.\n효과와 정답 설명 후 자동 진행합니다.\n남은 문제와 보호 부적으로도 역전이 불가능하면 상대의 마지막 일격으로 끝납니다."},
        {"한 판에 아이템 하나", "분석 렌즈: 현재 문제에서 오답 보기 하나 제거.\n보호 부적: 5문제 뒤 첫 오답 한 문제 재도전. 보기는 다시 섞입니다.\n정보 물약: 이번 전투 동안 인물 정보 열람.\n\n재도전 정답은 성공 조건에만 +1입니다. 최초 정답률·별 평가·XP·코인·해금 정답 수에는 중복 반영하지 않습니다."},
        {"모은 인물 카드와 함께해요", "연습 정답이나 배틀 승리로 인물 카드를 얻습니다. 탐험가 길드에서 획득한 지원 카드 한 장을 장착할 수 있습니다.\n\n함께 승리하면 숙련도가 증가합니다. 집중 성향의 기존 XP 보너스는 유지됩니다.\n카드나 레벨이 정답 피해와 성공에 필요한 정답 수를 바꾸지는 않습니다."},
        {"상점 · 성장 · 도전과제", "연습과 전투를 완료하면 XP와 코인을 얻습니다.\n시작 코인 120으로 분석 렌즈·보호 부적·정보 물약을 구매할 수 있습니다.\n\n전투 중 답안을 제출하기 전에 한 개만 사용하세요. 사용한 아이템은 중단해도 반환되지 않습니다. 보호 부적은 오답이 없으면 발동하지 않습니다.\n도전과제 달성 후 보상 받기로 추가 코인을 받으세요."},
        {"마지막 목표는 교수님 구출", "다른 분야의 서로 다른 문제 30개를 맞히면 이은정 교수님 구출 경로가 열립니다. 일반 마을은 서로 다른 인물 3명에게 승리하면 보스가 열립니다.\n\n일반·보스·교수님 전투 모두 해당 인물 문제 5개입니다.\n별: 승리 1개 + 최초 정답 4개 이상 1개 + 아이템 미사용 1개.\n\n숫자 1~4로 보기 선택, Enter로 제출합니다. 피드백은 건너뛰지 않습니다. 오답 노트에서 틀린 내용을 복습하세요."}
    };
    private final AdventureFeature feature;
    private final JLabel title = new JLabel(), indicator = new JLabel(), demoResult = new JLabel(" ");
    private final JTextArea body = new JTextArea();
    private final JPanel demo = new JPanel(new GridLayout(1, 3, 12, 0));
    private final JButton previous = new UiButton("이전", GameTheme.TEAL), next = new UiButton("다음", new Color(133, 101, 52));
    private int page;
    public TutorialView(AdventureFeature feature) {
        super(new BorderLayout());
        setBackground(GameTheme.PAGE);
        this.feature = feature;
        add(new TutorialLayout(title, indicator, body, demo, demoResult, previous, next,
            () -> { page--; render(); }, this::advance, feature::returnToMenu));
        reset();
    }
    public void reset() { page = 0; render(); }
    public int page() { return page; }
    private void render() {
        title.setText(PAGES[page][0]); body.setText(PAGES[page][1]); body.setCaretPosition(0);
        indicator.setText("게임 방법 " + (page + 1) + "/" + PAGES.length);
        previous.setEnabled(page > 0); next.setText(page == PAGES.length - 1 ? "튜토리얼 완료 · 시작" : "다음");
        demo.setVisible(page == 1); demoResult.setText(" ");
    }
    private void advance() {
        if (page < PAGES.length - 1) { page++; render(); return; }
        try { feature.completeTutorial(); }
        catch (java.io.IOException e) { JOptionPane.showMessageDialog(this, "안내 완료를 저장하지 못했습니다. 다시 눌러 주세요.\n" + e.getMessage(), "저장 실패", JOptionPane.ERROR_MESSAGE); }
    }
    @Override public ViewId getViewId() { return ViewId.TUTORIAL; }
    @Override public JComponent getComponent() { return this; }
    @Override public Dimension getViewSize() { return new Dimension(1280, 960); }
    @Override protected void paintComponent(Graphics source) {
        GameTheme.paintBackdrop(source, this, "tutorial", 0.33f);
    }
}
