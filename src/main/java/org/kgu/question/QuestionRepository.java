
package org.kgu.question;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class QuestionRepository {

    // ===== TXT 파일에서 문제 데이터 불러오기 =====
    public List<Question> loadQuestions(String field) {
        List<Question> questions = new ArrayList<>();
        String fileName = "questions/" + field + ".txt";
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(fileName);

        if (inputStream == null) throw new IllegalArgumentException("문제 파일을 찾을 수 없습니다: " + fileName);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            List<String> lines = new ArrayList<>();
            String line;

            while ((line = reader.readLine()) != null) lines.add(line);

            // ===== 파일 줄 수 검사 =====
            if (lines.size() % 10 != 0) {
                throw new IllegalStateException("TXT 파일의 줄 수가 올바르지 않습니다. 한 사람당 10줄이어야 합니다. 현재 줄 수: " + lines.size());
            }

            // ===== 인물 정보 및 문제 데이터 처리 =====
            for (int i = 0; i < lines.size(); i += 10) {
                String[] personData = lines.get(i).split("\t", -1);
                if (personData.length != 5) throw new IllegalStateException((i + 1) + "번째 줄의 인물 정보 형식이 잘못되었습니다.");

                String personName = personData[0], introduction = personData[1];
                String info1 = personData[2], info2 = personData[3], info3 = personData[4];

                // ===== 한 사람의 문제 9개 처리 =====
                for (int j = 1; j <= 9; j++) {
                    int lineNumber = i + j + 1;
                    String[] data = lines.get(i + j).split("\t", -1);
                    if (data.length != 7) throw new IllegalStateException(lineNumber + "번째 줄의 문제 형식이 잘못되었습니다.");

                    String difficulty = data[0], questionText = data[1];
                    String[] options = {data[2], data[3], data[4], data[5]};
                    int answerIndex;

                    try {
                        answerIndex = Integer.parseInt(data[6]);
                    } catch (NumberFormatException e) {
                        throw new IllegalStateException(lineNumber + "번째 줄의 정답 번호가 숫자가 아닙니다.");
                    }

                    if (answerIndex < 1 || answerIndex > 4) throw new IllegalStateException(lineNumber + "번째 줄의 정답 번호는 1~4여야 합니다.");

                    questions.add(new Question(personName, introduction, info1, info2, info3, difficulty, questionText, options, answerIndex));
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("문제 파일을 읽을 수 없습니다: " + fileName, e);
        }

        return questions;
    }
}
