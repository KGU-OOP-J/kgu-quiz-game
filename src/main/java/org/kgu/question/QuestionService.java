
package org.kgu.question;

import java.util.ArrayList;
import java.util.List;

public class QuestionService {

    // ===== 문제 저장소 및 초기화 =====
    private final QuestionRepository repository;

    public QuestionService() {
        repository = new QuestionRepository();
    }

    // ===== 분야와 난이도에 맞는 문제 제공 =====
    public List<Question> getQuestions(String field, String difficulty) {
        List<Question> allQuestions = repository.loadQuestions(field);
        List<Question> selectedQuestions = new ArrayList<>();
        List<String> usedPeople = new ArrayList<>();
        String selectedDifficulty = convertDifficulty(difficulty);

        for (Question question : allQuestions) {
            if (question.getDifficulty().equals(selectedDifficulty) && !usedPeople.contains(question.getPersonName())) {
                selectedQuestions.add(question);
                usedPeople.add(question.getPersonName());
            }
        }

        return selectedQuestions;
    }

    // ===== 화면 난이도와 TXT 난이도 연결 =====
    private String convertDifficulty(String difficulty) {
        switch (difficulty) {
            case "하": return "쉬움";
            case "중": return "보통";
            case "상": return "어려움";
            default: return difficulty;
        }
    }

    // ===== 정답 확인 =====
    public boolean checkAnswer(Question question, int answer) {
        if (question == null) return false;
        return question.getAnswerIndex() == answer;
    }
}
