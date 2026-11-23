
package org.kgu.question;

// 인물 정보와 문제 데이터를 저장하는 클래스. 
public class Question {

    private String personName;
    private String introduction;
    private String info1;
    private String info2;
    private String info3;

    private String difficulty;
    private String questionText;
    private String[] options;
    private int answerIndex;

    public Question(String personName, String introduction,
                    String info1, String info2, String info3,
                    String difficulty, String questionText,
                    String[] options, int answerIndex) {

        this.personName = personName; this.introduction = introduction;
        this.info1 = info1; this.info2 = info2; this.info3 = info3;
        this.difficulty = difficulty; this.questionText = questionText;
        this.options = options; this.answerIndex = answerIndex;
    }
    public String getPersonName() {
        return personName;
    }
    public String getIntroduction() {
        return introduction;
    }
    public String getInfo1() {
        return info1;
    }
    public String getInfo2() {
        return info2;
    }
    public String getInfo3() {
        return info3;
    }
    public String getDifficulty() {
        return difficulty;
    }
    public String getQuestionText() {
        return questionText;
    }
    public String[] getOptions() {
        return options;
    }
    public int getAnswerIndex() {
        return answerIndex;
    }
}
