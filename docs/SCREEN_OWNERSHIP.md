# 화면 모듈 담당 구분

- `org.kgu.ui.start`: ID 입력, 메인 메뉴, 게임 준비(분야·난이도·모드) 담당
- `org.kgu.ui.question`: 문제 풀이 화면 담당
- `org.kgu.ui.collection`: 카드 도감 화면 담당
- `org.kgu.ui.settings`: 설정 화면 담당
- `org.kgu.ui.result`: 게임 결과·획득 카드 화면 담당
- `org.kgu.ui.navigation`: 화면 전환만 담당. 화면 UI를 작성하지 않음
- `org.kgu.ui.common`: 모든 화면이 공유하는 버튼, 색상, 폰트, 그리기 도구

각 담당자는 자기 패키지 안의 화면 클래스만 수정한다. 다른 화면으로 이동이 필요하면 `GameNavigator`의 공개 메서드를 호출한다.
