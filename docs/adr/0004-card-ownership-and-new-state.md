# ADR-0004: 카드 획득과 단계별 NEW의 저장 단위

- Status: Proposed
- Date: 2026-11-27

## Context

기존 카드 소유는 `.txt`, 진행도는 `.progress`에 저장한다. 단계별 NEW는 재실행 후에도 유지해야 하며 획득 연출과 독립적이어야 한다. 서로 다른 두 파일에 소유와 NEW를 나눠 저장하면 중간 실패 시 알림이 누락될 수 있다.

## Decision

신규 카드의 소유와 NEW를 기존 `.progress`의 한 원자적 갱신으로 기록하는 방식을 제안하고 구현했다. 기존 `.txt`는 호환성을 위해 계속 읽고 내보낸다. 내보내기에 실패해도 소유·NEW는 `.progress`에서 복구한다. 기존 저장에는 새 키가 없어도 된다.

기존 파일에 알림을 덧붙이면 기존 3열 계약이 깨진다. 별도 알림 파일은 파일 간 정합성 처리가 늘어난다. 기존 `ProgressStore`를 확장하면 원자적 교체 방식을 재사용할 수 있다.

## Consequences

- Positive: 소유와 NEW를 함께 복구하며, View의 애니메이션이 저장을 결정하지 않는다.
- Negative: 기존 `.txt`와 신규 소유 키가 공존한다. 내보내기 실패 시 예전 버전만 실행하면 최신 카드를 보지 못할 수 있다.

## Compliance

`SaveFactory.record`에서 획득을 확정하고 `ProgressStore.readCard`에서 개별 단계의 NEW만 해제한다. 도감 진입·획득 레이어는 일괄 해제하지 않는다. 실패 시나리오와 실제 화면 확인은 [구현 계획](../ONE_STEP_MORE_PLAN.md)에 기록한다.

## Notes

[요구사항 FR-02·04·05](../one_step_more.md) 기반 구현 제안이다. 저장 방식 자체에 대한 별도 승인은 받지 않았으므로 Proposed로 기록한다.
