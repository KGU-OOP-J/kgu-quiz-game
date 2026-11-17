package org.kgu.game.event;

import org.kgu.game.feature.GameFeature;
import org.kgu.game.view.ResultView;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Optional;

public final class ResultGUIEvent {
    private final GameFeature gameFeature;
    public ResultGUIEvent(GameFeature gameFeature){this.gameFeature=gameFeature;}
    public void refresh(ResultView view){
        try{
            gameFeature.saveRewards();
            view.display(gameFeature.getSession(),gameFeature.getSavedRewards(),"보상 처리 완료 (중복 단계 제외)",false);
        }catch(IOException e){
            view.display(gameFeature.getSession(),gameFeature.getSavedRewards(),"보상 저장 실패: "+e.getMessage(),true);
        }
    }
    public Optional<BufferedImage> image(String key){return gameFeature.image(key);}
    public void returnToMenu(){gameFeature.returnToMenu();}
    public void openCollection(){gameFeature.openCollection();}
}
