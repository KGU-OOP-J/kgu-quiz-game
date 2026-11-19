package org.kgu.battle;

import static org.junit.jupiter.api.Assertions.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import java.util.UUID;
import javax.swing.*;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import org.kgu.integration.ui.navigation.GameNavigator;
import org.kgu.integration.collection.CardSave;
import org.kgu.ui.battle.*;

class ProfessorBattleIntegrationTest {
    @Test void eachCategoryAndRandomSelectionSaveExactlyOneCard() throws Exception {
        for (String category : new String[]{"과학자", "아이돌", "한국사", "무작위", "교수님"}) {
            playAndSave(category);
        }
    }

    private void playAndSave(String category) throws Exception {
        String id="battle-test-"+UUID.randomUUID();
        SwingUtilities.invokeAndWait(()->{
            JFrame frame=null;
            try{
                var nav=new GameNavigator();nav.getSession().setPlayerId(id);nav.getSession().setField(category);nav.getSession().setDifficulty("중");
                var field=GameNavigator.class.getDeclaredField("frame");field.setAccessible(true);frame=(JFrame)field.get(nav);
                nav.showBattle();assertNotNull(named(frame,"encounter",JLabel.class));
                named(frame,"startBattle",JButton.class).doClick();
                BattleScreen screen=component(frame,BattleScreen.class);
                var engineField=BattleScreen.class.getDeclaredField("engine");engineField.setAccessible(true);var engine=(BattleEngine)engineField.get(screen);
                if (!"무작위".equals(category)) assertEquals(category,engine.target().category());assertEquals(3,engine.questionCount());
                screen.setSize(1280,960);screen.doLayout();BufferedImage img=new BufferedImage(1280,960,BufferedImage.TYPE_INT_RGB);var g=img.createGraphics();screen.printAll(g);g.dispose();
                ImageIO.write(img,"png",Path.of(System.getProperty("battle.previewPath").replace(".png","-real.png")).toFile());
                while(engine.phase()!=BattleEngine.Phase.FINISHED){
                    screen.getActionMap().get("choice"+engine.current().answerIndex()).actionPerformed(null);
                    screen.getActionMap().get("confirm").actionPerformed(null);UiTestActions.completeFeedback(screen);
                }
                assertTrue(engine.won());assertNotNull(component(frame,BattleResultScreen.class));
                var saved=CardSave.load(id);assertEquals(1,saved.size());assertTrue(saved.containsKey(engine.target().category()+"\t"+engine.target().name()));
                assertTrue(named(frame,"rewardStatus",JLabel.class).getText().contains("등록"));
            }catch(Exception ex){throw new RuntimeException(ex);}finally{if(frame!=null)frame.dispose();try{Files.deleteIfExists(Path.of("saves",id+".txt"));}catch(Exception ignored){}}
        });
    }
    private <T> T component(Container p,Class<T> type){if(type.isInstance(p))return type.cast(p);for(Component c:p.getComponents())if(c instanceof Container child){T value=component(child,type);if(value!=null)return value;}return null;}
    private <T> T named(Container p,String name,Class<T> type){for(Component c:p.getComponents()){if(name.equals(c.getName()))return type.cast(c);if(c instanceof Container child){T value=named(child,name,type);if(value!=null)return value;}}return null;}
}
