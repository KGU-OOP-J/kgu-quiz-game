package org.kgu.domain.battle.feature;

import org.kgu.battle.UiTestActions;

import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.awt.Container;
import java.awt.Component;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import javax.swing.*;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import org.kgu.domain.battle.view.component.*;

class BattleTest {
    private BattleEngine mission() {
        var professor=new BattleCharacter("professor","권기현","교수님","images/cards/권기현_2.png");
        var questions=new ArrayList<BattleQuestion>();
        for(int i=0;i<3;i++)questions.add(new BattleQuestion("q"+i,professor.id(),"중","교수님의 기억을 되찾을 수 있는 답은 무엇일까요?",new String[]{"기억의 단서","보기 2","보기 3","보기 4"},0,"",""));
        return new BattleEngine(professor,questions,2);
    }
    private void answer(BattleEngine e,int choice){e.select(choice);e.submit();e.advance();}
    @Test void supportsMoreQuestionsAndCallerDefinedSuccessThreshold() {
        var professor = mission().target();
        var questions = new ArrayList<BattleQuestion>();
        for (int i = 0; i < 8; i++) {
            questions.add(new BattleQuestion("expanded-" + i, professor.id(), "중", "추가 문제 " + i,
                    new String[]{"A", "B", "C", "D"}, 0, "", ""));
        }
        var engine = new BattleEngine(professor, questions, 5);
        for (int i = 0; i < 8; i++) answer(engine, i < 3 ? 1 : 0);
        assertEquals(8, engine.questionCount());
        assertEquals(5, engine.correctCount());
        assertTrue(engine.won());
        assertEquals(0, engine.remainingHp());
        assertEquals(4, BattleEngine.defaultRequiredCorrect(6));
        assertEquals(6, BattleEngine.defaultRequiredCorrect(8));
    }
    @Test void knockoutFinishesEarlyAfterFeedbackAndAwardsOnlyProfessor() {
        var engine = mission();
        assertEquals(60, engine.remainingHp());
        answer(engine, 0);
        assertEquals(30, engine.remainingHp());
        assertEquals(30, engine.lastDamage());
        engine.select(0);
        engine.submit();
        assertEquals(0, engine.remainingHp());
        assertTrue(engine.defeated());
        assertTrue(engine.rewardIds().isEmpty());
        assertThrows(IllegalStateException.class, engine::won);
        String lastQuestionId = engine.current().id();
        engine.advance();
        assertTrue(engine.won());
        assertEquals(2, engine.questionIndex());
        assertEquals(lastQuestionId, engine.current().id());
        assertEquals(Set.of("professor"), engine.rewardIds());
        assertThrows(IllegalStateException.class, engine::submit);
    }
    @Test void missDoesNotReduceHpAndExhaustedQuestionsFail(){var e=mission();answer(e,0);answer(e,1);assertEquals(30,e.remainingHp());answer(e,1);assertFalse(e.won());assertTrue(e.rewardIds().isEmpty());}
    @Test void submissionAndTransitionGuards(){var e=mission();assertThrows(IllegalStateException.class,e::submit);assertThrows(IllegalStateException.class,e::advance);e.select(8);assertEquals(-1,e.selected());e.select(0);e.submit();assertThrows(IllegalStateException.class,e::submit);e.select(1);assertEquals(0,e.selected());e.advance();assertEquals(-1,e.selected());}
    @Test void rejectsOtherPeopleDuplicatesAndInvalidThreshold(){var e=mission();assertThrows(IllegalArgumentException.class,()->new BattleEngine(e.target(),List.of(e.current(),e.current()),1));assertThrows(IllegalArgumentException.class,()->new BattleEngine(e.target(),List.of(e.current()),2));assertThrows(IllegalArgumentException.class,()->new BattleEngine(new BattleCharacter("other","다른 교수","교수님",""),List.of(e.current()),1));}
    @Test void onePortraitNoVersusAndOneFinishCallback() throws Exception {
        SwingUtilities.invokeAndWait(()->{
            var e=mission();AtomicInteger calls=new AtomicInteger();var screen=new BattleScreen(e,result->calls.incrementAndGet(),()->{});
            assertEquals(1,Arrays.stream(screen.getComponents()).filter(c->"professorCard".equals(c.getName())).count());
            assertFalse(Arrays.stream(screen.getComponents()).anyMatch(c->c instanceof JLabel l && "VS".equals(l.getText())));
            for(int i=0;i<3;i++){screen.getActionMap().get("choice0").actionPerformed(null);screen.getActionMap().get("confirm").actionPerformed(null);UiTestActions.completeFeedback(screen);}
            screen.getActionMap().get("confirm").actionPerformed(null);assertEquals(1,calls.get());assertEquals(0,e.remainingHp());
        });
    }
    @Test void earlyExitDoesNotCompleteOrReward() throws Exception {
        SwingUtilities.invokeAndWait(()->{var e=mission();AtomicInteger exits=new AtomicInteger();var screen=new BattleScreen(e,r->fail("unexpected result"),exits::incrementAndGet);named(screen,"menu",JButton.class).doClick();screen.getActionMap().get("choice0").actionPerformed(null);screen.getActionMap().get("confirm").actionPerformed(null);assertEquals(1,exits.get());assertEquals(0,e.correctCount());assertTrue(e.rewardIds().isEmpty());});
    }
    @Test void resultRetriesSaveButFailureNeverSaves() throws Exception {
        SwingUtilities.invokeAndWait(()->{var e=mission();answer(e,0);answer(e,0);AtomicInteger saves=new AtomicInteger();var result=new BattleResultScreen(e,()->{if(saves.incrementAndGet()==1)throw new IllegalStateException("disk");return true;},()->{},()->{},()->{});JButton retry=named(result,"retrySave",JButton.class);assertTrue(retry.isVisible());retry.doClick();assertFalse(retry.isVisible());assertEquals(2,saves.get());var failed=mission();answer(failed,1);answer(failed,1);answer(failed,1);new BattleResultScreen(failed,()->{fail("failure must not save");return false;},()->{},()->{},()->{});});
    }
    @Test void rendersBattlePreview() throws Exception {
        SwingUtilities.invokeAndWait(()->{try{var screen=new BattleScreen(mission(),r->{},()->{});screen.setSize(1280,960);screen.doLayout();BufferedImage image=new BufferedImage(1280,960,BufferedImage.TYPE_INT_RGB);var g=image.createGraphics();screen.printAll(g);g.dispose();Path file=Path.of(System.getProperty("battle.previewPath","../../battle-preview.png"));Files.createDirectories(file.toAbsolutePath().getParent());ImageIO.write(image,"png",file.toFile());}catch(Exception ex){throw new RuntimeException(ex);}});
    }
    private <T> T named(Container parent,String name,Class<T> type){for(Component c:parent.getComponents()){if(name.equals(c.getName()))return type.cast(c);if(c instanceof Container container){T found=find(container,name,type);if(found!=null)return found;}}throw new AssertionError(name);}
    private <T> T find(Container p,String n,Class<T> t){for(Component c:p.getComponents()){if(n.equals(c.getName()))return t.cast(c);if(c instanceof Container child){T result=find(child,n,t);if(result!=null)return result;}}return null;}
}
