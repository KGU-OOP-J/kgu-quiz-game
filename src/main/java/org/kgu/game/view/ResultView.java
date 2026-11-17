package org.kgu.game.view;

import org.kgu.collection.view.CardArt;
import org.kgu.collection.view.CardView;
import org.kgu.game.event.ResultGUIEvent;
import org.kgu.game.feature.BattleEngine;
import org.kgu.game.feature.GameSession;
import org.kgu.model.Person;
import org.kgu.window.UiButton;
import org.kgu.window.UiStyle;
import org.kgu.window.View;
import org.kgu.window.ViewId;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public final class ResultView extends JPanel implements View {
    private final ResultGUIEvent event;
    private final CardArt art;
    private final JLabel title = new JLabel("게임 결과", SwingConstants.CENTER);
    private final JLabel verdict = new JLabel("", SwingConstants.CENTER);
    private final JLabel score = new JLabel("", SwingConstants.CENTER);
    private final JPanel hp = new JPanel(new GridLayout(1, 2, 30, 0));
    private final JPanel rewards = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
    private final JLabel status = new JLabel("", SwingConstants.CENTER);
    private final JButton retry = new UiButton("보상 저장 재시도", new Color(239, 151, 43));

    public ResultView(ResultGUIEvent event) {
        this.event = event;
        art = new CardArt(event::image);
        setLayout(new BorderLayout(20, 15));
        setBackground(new Color(199, 229, 255));
        setBorder(BorderFactory.createEmptyBorder(25, 50, 25, 50));
        JPanel summary = new JPanel(new GridLayout(0, 1, 0, 10));
        summary.setOpaque(false);
        title.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 36f));
        verdict.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 36f));
        score.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 24f));
        summary.add(title);
        summary.add(verdict);
        hp.setOpaque(false);
        summary.add(hp);
        summary.add(score);
        add(summary, BorderLayout.NORTH);
        rewards.setBackground(new Color(255, 255, 255, 230));
        JScrollPane scroll = new JScrollPane(rewards);
        scroll.setBorder(BorderFactory.createTitledBorder("획득한 단계 카드"));
        add(scroll, BorderLayout.CENTER);
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        status.setFont(UiStyle.FONT.deriveFont(16f));
        bottom.add(status, BorderLayout.NORTH);
        JPanel actions = new JPanel();
        actions.setOpaque(false);
        JButton menu = new UiButton("메인으로", new Color(20, 125, 244));
        JButton collection = new UiButton("도감 확인", new Color(30, 172, 111));
        for (JButton button : new JButton[]{menu, collection, retry}) {
            button.setForeground(Color.WHITE);
            button.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 20f));
            button.setPreferredSize(new Dimension(220, 55));
            actions.add(button);
        }
        menu.addActionListener(e -> event.returnToMenu());
        collection.addActionListener(e -> event.openCollection());
        retry.addActionListener(e -> event.refresh(this));
        bottom.add(actions, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);
    }

    public void display(GameSession session, List<Person> savedRewards, String message, boolean failed) {
        hp.removeAll();
        if (session.getMode() == GameSession.Mode.BATTLE) {
            BattleEngine battle = session.getBattle();
            title.setText("최종 배틀 결과");
            String result = battle.result() == BattleEngine.Result.WIN ? "승리" : battle.result() == BattleEngine.Result.LOSE ? "패배" : "무승부";
            verdict.setText(result);
            hp.add(hpPanel("플레이어", battle.playerHp()));
            hp.add(hpPanel("상대", battle.computerHp()));
            score.setText("맞힌 문제: " + battle.correctCount() + " / 6");
        } else {
            title.setText("연습 결과");
            verdict.setText("10문제를 모두 풀었습니다!");
            score.setText("맞힌 문제: " + session.getCorrectCount() + " / " + session.getQuestionCount());
        }
        rewards.removeAll();
        for (Person person : savedRewards) {
            CardView card = new CardView(person, session.getDifficulty().getStage(), art);
            card.setPreferredSize(new Dimension(170, 250));
            rewards.add(card);
        }
        if (savedRewards.isEmpty()) {
            JLabel empty = new JLabel(failed ? "저장된 보상이 없습니다. 재시도해 주세요." : "이번 게임에서 획득한 보상이 없습니다.");
            empty.setFont(UiStyle.FONT);
            rewards.add(empty);
        }
        status.setText(message);
        status.setForeground(failed ? new Color(180, 45, 45) : UiStyle.NAVY);
        retry.setVisible(failed);
        revalidate();
        repaint();
    }

    private JPanel hpPanel(String name, int value) {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(Color.WHITE);
        JLabel label = new JLabel(name + " HP " + value + " / 60", SwingConstants.CENTER);
        label.setFont(UiStyle.FONT);
        panel.add(label, BorderLayout.NORTH);
        JProgressBar bar = new JProgressBar(0, 60);
        bar.setValue(value);
        bar.setForeground(new Color(73, 175, 102));
        panel.add(bar, BorderLayout.CENTER);
        return panel;
    }

    @Override
    public ViewId getViewId() {
        return ViewId.RESULT;
    }

    @Override
    public JComponent getComponent() {
        return this;
    }

    @Override
    public Dimension getViewSize() {
        return new Dimension(1280, 960);
    }
}
