
package org.kgu.question;

import java.util.ArrayList;
import java.util.List;

// 분야와 난이도에 맞는 문제를 제공하고 정답을 확인하는 클래스 
public class QuestionService {

    private final QuestionRepository repository;

    public QuestionService() {
        repository = new QuestionRepository();
    }

    
     //선택한 분야와 난이도에 해당하는 문제만 반환
     
    public List<Question> getQuestions(String field, String difficulty) {
    List<Question> allQuestions = repository.loadQuestions(field);
    List<Question> selectedQuestions = new ArrayList<>();

    // 이미 문제를 출제한 인물 이름 저장
    List<String> usedPeople = new ArrayList<>();

    String selectedDifficulty = convertDifficulty(difficulty);

    for (Question question : allQuestions) {
        // 선택한 난이도와 일치하고 아직 출제하지 않은 인물인지 확인
        if (question.getDifficulty().equals(selectedDifficulty)
                && !usedPeople.contains(question.getPersonName())) {

            selectedQuestions.add(question);
            usedPeople.add(question.getPersonName());
        }
    }

    return selectedQuestions;
    }

    
     //게임 화면의 난이도와 TXT 파일의 난이도 이름을 연결
     //(회의)이름 맞추면 제거할 코드
    private String convertDifficulty(String difficulty) {
        switch (difficulty) {
            case "하": return "쉬움";
            case "중": return "보통";
            case "상": return "어려움";
            default: return difficulty;
        }
    }

    //사용자가 제출한 답이 정답인지 확인, answer와 정답 번호는 모두 1~4를 사용
    public boolean checkAnswer(Question question, int answer) {

        if (question == null) {
            return false;
        }

        return question.getAnswerIndex() == answer;
    }
}
