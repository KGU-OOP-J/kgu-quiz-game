package org.kgu.integration.collection;

import java.util.List;

/**
 * 도감에 표시하는 인물 카드 한 장.
 *
 * @param name 카드 이름. 그림 파일 이름(images/cards/{name}.png)으로도 쓴다.
 * @param intro 이름 옆에 나오는 소개(직위·시대 등)
 * @param field 분야
 * @param stages 얻은 단계(하 1, 중 2, 상 3)를 오름차순으로. 비어 있으면 미획득
 * @param achievements 주요 업적
 */
public record Card(String name, String intro, String field, List<Integer> stages, List<String> achievements) {
    /** 모든 카드의 공격력. */
    public static final int ATTACK = 20;
    /** 인물마다 있는 단계 카드 수(하·중·상). 인물 80명이면 카드는 240장이다. */
    public static final int STAGE_COUNT = 3;

    /** 어느 난이도든 클리어했으면 획득한 카드다. */
    public boolean owned() {
        return !stages.isEmpty();
    }

    /** 얻은 가장 높은 단계. 도감의 별 개수와 정렬에 쓴다. 미획득이면 0이다. */
    public int stars() {
        return owned() ? stages.get(stages.size() - 1) : 0;
    }
}
