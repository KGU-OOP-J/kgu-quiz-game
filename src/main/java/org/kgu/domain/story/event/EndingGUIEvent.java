package org.kgu.domain.story.event;

import org.kgu.domain.story.feature.EndingFeature;
import org.kgu.domain.story.view.EndingView;

public final class EndingGUIEvent {
    private final EndingFeature feature;
    public EndingGUIEvent(EndingFeature feature) { this.feature = feature; }
    public void display(EndingView view) { view.display(feature.summary(), feature.seen()); }
    public void finish(EndingView view) {
        try { feature.confirm(); feature.continuePlaying(); }
        catch (java.io.IOException e) { view.showError("엔딩 기록 저장 실패 · 계속하기를 다시 눌러 주세요."); }
    }
}
