package org.kgu.domain.battle.view.component;

import java.awt.*;
import java.util.function.Supplier;
import javax.swing.*;
import org.kgu.domain.battle.feature.BattleEngine;

/** Displays a completed battle; the caller supplies the real card-saving function. */
public final class BattleResultScreen extends JPanel {
    private final Supplier<Boolean> saveCard;
    private final JLabel status = new JLabel("", SwingConstants.CENTER);
    private final JButton saveAgain = new JButton("카드 저장 재시도");

    public BattleResultScreen(BattleEngine engine, Supplier<Boolean> saveCard,
                              Runnable onRetry, Runnable onMenu, Runnable onCollection) {
        this.saveCard = saveCard;
        boolean won = engine.won();
        setPreferredSize(new Dimension(1280, 960));
        setLayout(new BorderLayout(25, 25));
        setBackground(new Color(226, 243, 255));
        setBorder(BorderFactory.createEmptyBorder(65, 90, 65, 90));

        JLabel title = new JLabel(won ? "도전 성공!" : "다시 도전해 보세요", SwingConstants.CENTER);
        title.setFont(new Font("Malgun Gothic", Font.BOLD, 38));
        add(title, BorderLayout.NORTH);

        JPanel body = new JPanel(new GridLayout(0, 1, 12, 20));
        body.setOpaque(false);
        JTextArea story = new JTextArea(resultStory(engine));
        story.setEditable(false);
        story.setOpaque(false);
        story.setLineWrap(true);
        story.setWrapStyleWord(true);
        story.setFont(new Font("Malgun Gothic", Font.PLAIN, 25));
        body.add(story);

        JLabel score = new JLabel("남은 HP " + engine.remainingHp() + " / " + BattleEngine.MAX_HP + " · 정답 "
                + engine.correctCount() + " / " + engine.questionIndex(), SwingConstants.CENTER);
        score.setFont(new Font("Malgun Gothic", Font.BOLD, 26));
        body.add(score);
        status.setName("rewardStatus");
        body.add(status);
        saveAgain.setName("retrySave");
        saveAgain.setVisible(false);
        saveAgain.addActionListener(e -> saveReward());
        body.add(saveAgain);
        add(body, BorderLayout.CENTER);

        JPanel actions = new JPanel();
        actions.setOpaque(false);
        actions.add(button("다른 상대 만나기", onRetry));
        actions.add(button("메인으로", onMenu));
        actions.add(button("도감 확인", onCollection));
        add(actions, BorderLayout.SOUTH);

        if (won) saveReward();
    }

    private String resultStory(BattleEngine engine) {
        String name = engine.target().name();
        if (!engine.won()) {
            return name + "에게 아직 승리하지 못했습니다.\n"
                    + "정답을 익힌 뒤 다시 도전해 보세요.\n\n이번 시도에는 카드가 지급되지 않습니다.";
        }
        if ("교수님".equals(engine.target().category())) {
            return name + " 교수님이 기억을 되찾았습니다.\n“찾아줘서 고맙네.”\n\n"
                    + "졸업을 앞둔 당신의 여정은 계속됩니다. 다른 교수님들도 찾아주세요.";
        }
        return name + "에게 승리했습니다.\n이 인물의 카드를 얻었습니다.\n\n"
                + "여러 마을에서 동료를 모으며 실종된 교수님들의 흔적을 찾아보세요.";
    }

    private void saveReward() {
        try {
            boolean fresh = saveCard.get();
            status.setText(fresh ? "획득한 카드 한 장이 도감에 등록되었습니다." : "이미 보유한 단계의 카드입니다.");
            status.setToolTipText(null);
            saveAgain.setVisible(false);
        } catch (RuntimeException ex) {
            status.setText("카드 저장에 실패했습니다. 재시도해 주세요.");
            status.setToolTipText(ex.getMessage());
            saveAgain.setVisible(true);
        }
    }

    private JButton button(String text, Runnable action) {
        JButton button = new JButton(text);
        button.addActionListener(e -> action.run());
        return button;
    }
}
