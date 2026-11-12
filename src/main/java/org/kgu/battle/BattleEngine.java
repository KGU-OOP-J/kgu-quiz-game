package org.kgu.battle;
import java.util.*;

/** No Swing dependency: independently testable 6-turn game. */
public final class BattleEngine {
    public enum Phase { QUESTION, FEEDBACK, FINISHED }
    public enum Result { WIN, LOSE, DRAW }
    public record TurnFeedback(boolean correct, boolean attackTurn, int damage, String explanation){}
    private final BattleCharacter player,computer;
    private final List<BattleQuestion> questions;
    private int turn=0,playerHp=60,computerHp=60,selected=-1,correctCount=0;
    private Phase phase=Phase.QUESTION;
    private TurnFeedback feedback;
    public BattleEngine(BattleCharacter player,BattleCharacter computer,List<BattleQuestion> playerQuestions,List<BattleQuestion> computerQuestions){
        if(player.id().equals(computer.id()))throw new IllegalArgumentException("Opponent must differ from player");
        if(playerQuestions.size()<3||computerQuestions.size()<3)throw new IllegalArgumentException("At least 3 questions per character are required");
        this.player=player;this.computer=computer;
        List<BattleQuestion> sequence = new ArrayList<>();
        for(int i=0;i<3;i++){sequence.add(playerQuestions.get(i));sequence.add(computerQuestions.get(i));}
        this.questions=List.copyOf(sequence);
    }
    public BattleCharacter player(){return player;}
    public BattleCharacter computer(){return computer;}
    public Phase phase(){return phase;}
    public int turn(){return turn;}
    public int playerHp(){return playerHp;}
    public int computerHp(){return computerHp;}
    public int selected(){return selected;}
    public int correctCount(){return correctCount;}
    public boolean attackTurn(){return turn%2==0;}
    public BattleQuestion current(){return questions.get(Math.min(turn,5));}
    public TurnFeedback feedback(){return feedback;}
    public void select(int choice){if(phase==Phase.QUESTION && choice>=0 && choice<4)selected=choice;}
    public TurnFeedback submit(){
        if(phase!=Phase.QUESTION||selected<0)throw new IllegalStateException("Choose an answer first");
        boolean correct=selected==current().answerIndex();
        if(correct)correctCount++;
        int damage=0;
        if(attackTurn()&&correct){computerHp=Math.max(0,computerHp-20);damage=20;}
        if(!attackTurn()&&!correct){playerHp=Math.max(0,playerHp-20);damage=20;}
        feedback=new TurnFeedback(correct,attackTurn(),damage,current().explanation());
        phase=Phase.FEEDBACK;return feedback;
    }
    public void advance(){
        if(phase!=Phase.FEEDBACK)throw new IllegalStateException("Submit answer before next turn");
        turn++;selected=-1;feedback=null;
        phase=turn>=6?Phase.FINISHED:Phase.QUESTION;
    }
    public Result result(){
        if(phase!=Phase.FINISHED)throw new IllegalStateException("Battle is still running");
        return playerHp>computerHp?Result.WIN:playerHp<computerHp?Result.LOSE:Result.DRAW;
    }
    public Set<String> rewardIds(){
        return phase==Phase.FINISHED && result()==Result.WIN?Set.of(player.id(),computer.id()):Set.of();
    }
}
