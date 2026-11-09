
package org.kgu.ui.question;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

import org.kgu.game.PlayerSession;
import org.kgu.question.Question;
import org.kgu.question.QuestionService;
import org.kgu.ui.common.UiButton;
import org.kgu.ui.common.UiStyle;
import org.kgu.ui.navigation.GameNavigator;

// 문제 화면을 담당하는 클래스
public final class QuestionScreen extends JPanel {

    // 화면 이동과 문제 관련 기능에 필요한 객체
    private final GameNavigator navigator;
    private final QuestionService questionService;

    private final List<Question> questions;
    private Question currentQuestion;
    private int currentQuestionIndex = 0;

    // 보기 버튼과 선택한 정답 번호
    private final JRadioButton[] optionButtons = new JRadioButton[4];
    private ButtonGroup optionGroup = new ButtonGroup();
    private final JPanel[] optionRows = new JPanel[4];
    private final JPanel optionPanel = new JPanel();
    private JScrollPane optionScroll;
    private JTextArea questionTextArea;
    private int selectedAnswer = 0;

    // 화면에서 사용하는 색상
    private final Color NAVY = new Color(43, 63, 91), BLUE = new Color(75, 139, 224), LIGHT_BLUE = new Color(232, 242, 255), GREEN = new Color(22, 164, 119);

    // 생성자: 문제 화면의 기본 설정과 구성 요소 생성
    public QuestionScreen(GameNavigator navigator) {
        this.navigator = navigator;
        this.questionService = new QuestionService();

        // 현재 선택한 분야와 난이도에 맞는 문제 불러오기
        PlayerSession session = navigator.getSession();
        questions = questionService.getQuestions(session.getField(), session.getDifficulty());

        if (questions.size() < 10) {
        throw new IllegalStateException("연습 모드를 진행하려면 최소 10문제가 필요합니다.");
        }

        // 첫 번째 문제 표시
        currentQuestion = questions.get(currentQuestionIndex);

        setPreferredSize(new Dimension(UiStyle.WIDTH, UiStyle.HEIGHT));
        setLayout(null);
        setBackground(new Color(239, 246, 255));

        // 문제와 보기 화면 추가
        addQuestion();
        addOptions();

        // 하단 버튼 추가
        add(button("힌트 보기", 35, new Color(75, 139, 224), e -> showHint()));
        add(button("정답 제출", 285, GREEN, e -> submitAnswer()));
        add(button("연습 그만", 535, new Color(135, 148, 166), e -> giveUp()));
    }

        // 문제 내용을 표시하고 긴 문장은 자동 줄바꿈 및 스크롤 처리
    private void addQuestion() {
        questionTextArea = new JTextArea(currentQuestion.getQuestionText());
        questionTextArea.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 17f));
        questionTextArea.setForeground(NAVY);
        questionTextArea.setLineWrap(true);
        questionTextArea.setWrapStyleWord(true);
        questionTextArea.setEditable(false);
        questionTextArea.setFocusable(false);
        questionTextArea.setOpaque(false);

        JScrollPane questionScroll = new JScrollPane(questionTextArea);
        questionScroll.setBounds(320, 135, 420, 75);
        questionScroll.setBorder(null);
        questionScroll.setOpaque(false);
        questionScroll.getViewport().setOpaque(false);
        questionScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        questionScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        add(questionScroll);
    }

    
    // 객관식 보기 4개를 만들고 긴 문장은 자동 줄바꿈 처리
    private void addOptions() {
        String[] options = currentQuestion.getOptions();

        optionPanel.removeAll();
        optionGroup = new ButtonGroup();
        optionPanel.setLayout(new BoxLayout(optionPanel, BoxLayout.Y_AXIS));
        optionPanel.setBackground(Color.WHITE);

        for (int i = 0; i < 4; i++) {
            final int answerNumber = i + 1;

            // 보기 한 개를 담을 영역
            JPanel row = new JPanel(new BorderLayout(8, 0));
            row.setBackground(Color.WHITE);
            row.setBorder(new EmptyBorder(10, 10, 10, 10));
            row.setAlignmentX(Component.LEFT_ALIGNMENT);

            JRadioButton option = new JRadioButton();
            option.setBackground(Color.WHITE);
            option.setOpaque(false);
            option.setFocusPainted(false);

            JTextArea optionText = new JTextArea((i + 1) + ". " + options[i]);
            optionText.setFont(UiStyle.FONT.deriveFont(16f));
            optionText.setForeground(NAVY);
            optionText.setLineWrap(true);
            optionText.setWrapStyleWord(true);
            optionText.setEditable(false);
            optionText.setFocusable(false);
            optionText.setOpaque(false);

            optionText.setSize(340, Short.MAX_VALUE);
            int rowHeight = Math.max(65, optionText.getPreferredSize().height + 20);

            row.setPreferredSize(new Dimension(390, rowHeight));
            row.setMinimumSize(new Dimension(390, rowHeight));
            row.setMaximumSize(new Dimension(390, rowHeight));

            row.add(option, BorderLayout.WEST);
            row.add(optionText, BorderLayout.CENTER);

            option.addActionListener(e -> {
                selectedAnswer = answerNumber;
                updateOptionStyle();
            });

            optionText.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    option.doClick();
                }
            });

            optionButtons[i] = option;
            optionRows[i] = row;
            optionGroup.add(option);

            optionPanel.add(row);
            optionPanel.add(Box.createVerticalStrut(5));
        }

        if (optionScroll == null) {
            optionScroll = new JScrollPane(optionPanel);
            optionScroll.setBounds(320, 220, 420, 215);
            optionScroll.setBorder(null);
            optionScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            optionScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            add(optionScroll);
        }

        optionPanel.revalidate();
        optionPanel.repaint();
    }

    // 선택한 보기의 배경색을 파란색으로 변경
    private void updateOptionStyle() {
        for (int i = 0; i < 4; i++) {
            if (optionButtons[i].isSelected()) {
                optionRows[i].setBackground(LIGHT_BLUE);
                optionRows[i].setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BLUE, 2),
                    new EmptyBorder(8, 8, 8, 8)
                ));
            } else {
                optionRows[i].setBackground(Color.WHITE);
                optionRows[i].setBorder(new EmptyBorder(10, 10, 10, 10));
            }
        }
    }

    // 인물 소개와 정보를 힌트로 표시(회의 후 변경예정)
    private void showHint() {
        String hint = currentQuestion.getIntroduction() + "\n"
                    + currentQuestion.getInfo1() + "\n"
                    + currentQuestion.getInfo2() + "\n"
                    + currentQuestion.getInfo3();

        JOptionPane.showMessageDialog(this, hint, "힌트", JOptionPane.INFORMATION_MESSAGE);
    }

        // 정답을 확인하고 결과 메시지를 보여준 뒤 다음 문제로 이동
    private void submitAnswer() {
        if (selectedAnswer == 0) {
            JOptionPane.showMessageDialog(this, "정답을 선택해 주세요.");
            return;
        }

        boolean isCorrect = questionService.checkAnswer(currentQuestion, selectedAnswer);

        if (isCorrect) {
            JOptionPane.showMessageDialog(
                this, currentQuestion.getPersonName() + " 카드 획득!", "정답!", JOptionPane.INFORMATION_MESSAGE
            );
        } else {
            JOptionPane.showMessageDialog(
                this, "아쉬워요! 정답이 아닙니다.", "오답", JOptionPane.INFORMATION_MESSAGE
            );
        }
        nextQuestion();
    }

    // 다음 문제로 이동
    private void nextQuestion() {
        currentQuestionIndex++;

        // 10문제를 모두 풀었으면 메인 메뉴로 이동
        if (currentQuestionIndex >= 10) {
            JOptionPane.showMessageDialog(this, "10문제를 모두 풀었습니다!\n수고하셨습니다.");
            navigator.showMenu();
            return;
        }

        // 다음 문제 데이터 불러오기
        currentQuestion = questions.get(currentQuestionIndex);
        selectedAnswer = 0;
        optionGroup.clearSelection();

        questionTextArea.setText(currentQuestion.getQuestionText());
        addOptions();

        repaint();
    }

    // 연습을 그만두고 메인 메뉴로 이동
    private void giveUp() {
        int result = JOptionPane.showConfirmDialog(
            this, "연습을 그만두고 메인 화면으로 돌아갈까요?", "연습 종료", JOptionPane.YES_NO_OPTION
        );

        if (result == JOptionPane.YES_OPTION) {
            navigator.showMenu();
        }
    }

    // 하단 버튼을 공통으로 생성하는 메서드
    private JButton button(String text, int x, Color color, java.awt.event.ActionListener action) {
        UiButton button = new UiButton(text, color);
        button.setBounds(x, 465, 180, 48);
        button.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 17f));
        button.setForeground(Color.WHITE);
        button.addActionListener(action);
        return button;
    }

    // 화면 배경, 카드, 상단 정보 등 디자인을 그리는 메서드
    @Override
    protected void paintComponent(Graphics source) {
        super.paintComponent(source);

        Graphics2D g = (Graphics2D) source.create();
        UiStyle.prepare(g);

        // 전체 배경
        g.setColor(new Color(239, 246, 255));
        g.fillRect(0, 0, getWidth(), getHeight());

        // 상단에 선택한 분야, 난이도, 모드를 세 줄로 표시(회의 후 변경예정)
        PlayerSession session = navigator.getSession();

        String difficultyText = session.getDifficulty().equals("중")
                ? "보통" : session.getDifficulty();

        String modeText = session.getMode().equals("연습")
                ? "연습 모드" : "대진 모드";

        g.setColor(NAVY);
        g.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 14f));

        g.drawString("분야: " + session.getField(), 35, 28);
        g.drawString("난이도: " + difficultyText, 35, 48);
        g.drawString("모드: " + modeText, 35, 68);

        // 현재 문제 번호 표시
        g.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 19f));
        // 현재 문제 번호 표시
        g.drawString("문제 " + (currentQuestionIndex + 1) + " / 10", 635, 48);

        // 문제 진행도 막대
        g.setColor(new Color(216, 228, 244));
        g.fillRoundRect(35, 82, 690, 9, 9, 9);
        g.setColor(BLUE);
        int progressWidth = 690 * (currentQuestionIndex + 1) / 10;
        g.fillRoundRect(35, 82, progressWidth, 9, 9, 9);

        // 가운데 흰색 문제 카드
        UiStyle.round(g, 20, 100, 740, 415, 20, Color.WHITE);

        // 왼쪽 인물 사진 영역
        UiStyle.round(g, 40, 135, 250, 285, 16, new Color(218, 237, 224));
        g.setColor(new Color(75, 126, 91));
        g.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 18f));
        g.drawString("인물 사진 영역", 95, 280);

        // 오른쪽 문제 영역 제목
        g.setColor(BLUE);
        g.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 13f));
        g.drawString("QUESTION", 320, 125);

        g.dispose();
    }
}
