
package org.kgu.ui.question;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.*;
import org.kgu.game.PlayerSession;
import org.kgu.question.Question;
import org.kgu.question.QuestionService;
import org.kgu.ui.common.UiStyle;
import org.kgu.ui.navigation.GameNavigator;

public final class QuestionScreen extends JPanel {

    // ===== 기본 게임 정보 및 화면 변수 =====
    private final GameNavigator navigator;
    private final QuestionService questionService;
    private final List<Question> questions;
    private Question currentQuestion;
    private int currentQuestionIndex = 0;
    private int selectedAnswer = 0;
    private final QuestionStyle.OptionRow[] optionRows = new QuestionStyle.OptionRow[4];
    private final JPanel optionPanel = new JPanel();
    private JTextPane questionTextPane;
    private BufferedImage backgroundImage;
    private BufferedImage personImage;

    // ===== 인물 사진 프레임 디자인 =====
    private static final Color FRAME_COLOR = new Color(37, 43, 53);
    private static final Color FRAME_BORDER = new Color(70, 81, 95);

    // ===== 화면 초기화 및 버튼 구성 =====
    public QuestionScreen(GameNavigator navigator) {
        this.navigator = navigator;
        questionService = new QuestionService();
        PlayerSession session = navigator.getSession();
        questions = questionService.getQuestions(session.getField(), session.getDifficulty());

        if (questions.size() < 10) throw new IllegalStateException("연습 모드를 진행하려면 최소 10문제가 필요합니다.");

        currentQuestion = questions.get(0);
        setPreferredSize(new Dimension(UiStyle.WIDTH, UiStyle.HEIGHT));
        setLayout(null);
        setBackground(QuestionStyle.BACKGROUND);

        loadBackgroundImage();
        loadPersonImage();
        addQuestion();
        addOptions();

        add(QuestionStyle.createBottomButton("💡  힌트 보기", 75, QuestionStyle.BLUE, e -> showHint()));
        add(QuestionStyle.createBottomButton("✔  정답 제출", 510, QuestionStyle.GREEN, e -> submitAnswer()));
        add(QuestionStyle.createBottomButton("↪  연습 그만", 945, new Color(112, 135, 164), e -> giveUp()));
    }

    // ===== 배경 및 인물 사진 불러오기 =====
    private void loadBackgroundImage() {
        try {
            URL url = getClass().getResource("/images/question-background.png");
            if (url != null) backgroundImage = ImageIO.read(url);
            else System.err.println("배경 이미지를 찾을 수 없습니다.");
        } catch (IOException e) {
            System.err.println("배경 이미지 로드 실패: " + e.getMessage());
        }
    }

    private void loadPersonImage() {
        personImage = PersonImageLoader.load(currentQuestion.getPersonName(), currentQuestion.getDifficulty());
        repaint();
    }

    // ===== 문제 내용 및 가운데 정렬 =====
    private void addQuestion() {
        questionTextPane = new JTextPane();
        questionTextPane.setText(currentQuestion.getQuestionText());
        questionTextPane.setFont(QuestionStyle.font(25f, true));
        questionTextPane.setForeground(QuestionStyle.NAVY);
        questionTextPane.setEditable(false);
        questionTextPane.setFocusable(false);
        questionTextPane.setOpaque(false);
        questionTextPane.setMargin(new Insets(5, 12, 5, 12));
        questionTextPane.setBounds(505, 255, 695, 105);
        centerQuestionText();
        add(questionTextPane);
    }

    private void centerQuestionText() {
        StyledDocument doc = questionTextPane.getStyledDocument();
        SimpleAttributeSet attributes = new SimpleAttributeSet();
        StyleConstants.setAlignment(attributes, StyleConstants.ALIGN_CENTER);
        doc.setParagraphAttributes(0, doc.getLength(), attributes, false);

        FontMetrics fm = questionTextPane.getFontMetrics(questionTextPane.getFont());
        int availableWidth = 665, lineCount = 1, currentWidth = 0;

        for (String word : questionTextPane.getText().split("\\s+")) {
            int wordWidth = fm.stringWidth(word + " ");
            if (currentWidth + wordWidth > availableWidth) {
                lineCount++;
                currentWidth = wordWidth;
            } else currentWidth += wordWidth;
        }

        int textHeight = lineCount * fm.getHeight();
        int topMargin = Math.max(5, (105 - textHeight) / 2);
        questionTextPane.setMargin(new Insets(topMargin, 12, topMargin, 12));
    }

    // ===== 선택지 생성 및 선택 처리 =====
    private void addOptions() {
        optionPanel.removeAll();
        optionPanel.setLayout(new BoxLayout(optionPanel, BoxLayout.Y_AXIS));
        optionPanel.setOpaque(false);
        optionPanel.setBounds(500, 410, 705, 390);
        String[] options = currentQuestion.getOptions();

        for (int i = 0; i < 4; i++) {
            final int answerNumber = i + 1;
            String value = options != null && i < options.length ? options[i] : "";
            QuestionStyle.OptionRow row = new QuestionStyle.OptionRow();
            row.setLayout(new GridBagLayout());
            row.setOpaque(false);
            row.setBorder(new EmptyBorder(5, 20, 5, 20));
            row.setPreferredSize(new Dimension(700, 82));
            row.setMinimumSize(new Dimension(700, 82));
            row.setMaximumSize(new Dimension(700, 82));
            row.setAlignmentX(Component.LEFT_ALIGNMENT);

            JTextArea text = new JTextArea((i + 1) + ". " + value);
            text.setFont(QuestionStyle.font(27f, true));
            text.setForeground(QuestionStyle.NAVY);
            text.setLineWrap(true);
            text.setWrapStyleWord(true);
            text.setEditable(false);
            text.setFocusable(false);
            text.setOpaque(false);
            text.setBorder(new EmptyBorder(0, 0, 0, 0));
            text.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.weightx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.anchor = GridBagConstraints.CENTER;
            row.add(text, gbc);

            MouseAdapter selectAction = new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    selectAnswer(answerNumber);
                }
            };

            row.addMouseListener(selectAction);
            text.addMouseListener(selectAction);
            optionRows[i] = row;
            optionPanel.add(row);
            if (i < 3) optionPanel.add(Box.createVerticalStrut(8));
        }

        add(optionPanel);
        optionPanel.revalidate();
        optionPanel.repaint();
    }

    private void selectAnswer(int answerNumber) {
        selectedAnswer = answerNumber;
        updateOptionStyle();
    }

    private void updateOptionStyle() {
        for (int i = 0; i < 4; i++) optionRows[i].setSelected(selectedAnswer == i + 1);
        optionPanel.repaint();
    }

    // ===== 힌트 표시 =====
    private void showHint() {
        String hint = currentQuestion.getIntroduction() + "\n" + currentQuestion.getInfo1() + "\n"
                + currentQuestion.getInfo2() + "\n" + currentQuestion.getInfo3();
        JTextArea hintArea = new JTextArea(hint, 8, 35);
        hintArea.setFont(QuestionStyle.font(19f, true));
        hintArea.setLineWrap(true);
        hintArea.setWrapStyleWord(true);
        hintArea.setEditable(false);
        hintArea.setBackground(new Color(245, 249, 255));
        hintArea.setBorder(new EmptyBorder(15, 15, 15, 15));
        JOptionPane.showMessageDialog(this, new JScrollPane(hintArea), "힌트", JOptionPane.INFORMATION_MESSAGE);
    }

    // ===== 정답 확인 및 결과 표시 =====
    private void submitAnswer() {
        if (selectedAnswer == 0) {
            JOptionPane.showMessageDialog(this, "정답을 선택해 주세요.");
            return;
        }

        boolean correct = questionService.checkAnswer(currentQuestion, selectedAnswer);
        if (correct) {
            JOptionPane.showMessageDialog(this, currentQuestion.getPersonName() + " 카드 획득!", "정답!", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "아쉬워요! 정답이 아닙니다.", "오답", JOptionPane.INFORMATION_MESSAGE);
        }
        nextQuestion();
    }

    // ===== 다음 문제 이동 및 10문제 완료 처리 =====
    private void nextQuestion() {
        if (currentQuestionIndex >= 9) {
            JOptionPane.showMessageDialog(this, "10문제를 모두 풀었습니다!\n수고하셨습니다.");
            navigator.showMenu();
            return;
        }

        currentQuestionIndex++;
        currentQuestion = questions.get(currentQuestionIndex);
        selectedAnswer = 0;
        loadPersonImage();
        questionTextPane.setText(currentQuestion.getQuestionText());
        centerQuestionText();
        remove(optionPanel);
        addOptions();
        revalidate();
        repaint();
    }

    // ===== 연습 종료 =====
    private void giveUp() {
        int result = JOptionPane.showConfirmDialog(this, "연습을 그만두고 메인 화면으로 돌아갈까요?", "연습 종료", JOptionPane.YES_NO_OPTION);
        if (result == JOptionPane.YES_OPTION) navigator.showMenu();
    }

    // ===== 전체 화면 및 인물 사진 그리기 =====
    @Override
    protected void paintComponent(Graphics source) {
        super.paintComponent(source);
        int displayQuestionIndex = Math.min(currentQuestionIndex, 9);
        QuestionStyle.paintScreen(source, this, backgroundImage, navigator.getSession(), displayQuestionIndex);

        Graphics2D g2 = (Graphics2D) source.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 사진 뒤 차콜 프레임
        int frameX = 73, frameY = 263, frameWidth = 404, frameHeight = 474;

        // 그림자 및 프레임
        g2.setColor(new Color(0, 0, 0, 45));
        g2.fillRoundRect(frameX + 4, frameY + 5, frameWidth, frameHeight, 22, 22);
        g2.setColor(FRAME_COLOR);
        g2.fillRoundRect(frameX, frameY, frameWidth, frameHeight, 22, 22);
        g2.setColor(FRAME_BORDER);
        g2.setStroke(new BasicStroke(2f));
        g2.drawRoundRect(frameX + 1, frameY + 1, frameWidth - 2, frameHeight - 2, 22, 22);

        // 인물 사진 표시
        if (personImage != null) {
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            double scale = Math.min(380.0 / personImage.getWidth(), 450.0 / personImage.getHeight());
            int drawWidth = (int) (personImage.getWidth() * scale);
            int drawHeight = (int) (personImage.getHeight() * scale);
            int x = 85 + (380 - drawWidth) / 2;
            int y = 275 + (450 - drawHeight) / 2;
            g2.drawImage(personImage, x, y, drawWidth, drawHeight, null);
        }

        g2.dispose();
    }
}
