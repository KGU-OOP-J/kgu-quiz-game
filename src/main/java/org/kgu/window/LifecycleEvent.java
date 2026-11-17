package org.kgu.window;

/**
 * 화면 진입·퇴장 처리. 타이머 등 외부 작업이 없는 화면은 별도 퇴장 처리가 필요 없다.
 */
public interface LifecycleEvent {
    void onEnter();

    default void onExit() {
    }
}
