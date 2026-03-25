# Git Conflict 정리

## 어떤 파일에서 충돌이 발생했는가

충돌이 발생한 파일은 conflic.md이다. 이 파일의 **첫 번째 줄**이 브랜치 C와 D에서 각각 다르게 수정되었기 때문에 충돌의 대상이 되었다.

---

## 왜 충돌이 발생했는가

Git은 두 브랜치가 **동일한 파일의 동일한 줄**을 서로 다른 내용으로 수정했을 때, 어떤 변경사항을 채택해야 할지 자동으로 판단할 수 없다. 이 실습에서의 흐름은 다음과 같다.

1. 원본 `conflict.md` 1번째 줄: `hello from B`
2. `branch-C`에서 → `hello from C`로 수정 후 commit
3. `branch-D`에서 → `hello from D`로 수정 후 commit
4. `branch-C`를 main에 먼저 merge → 성공 (main의 1번째 줄이 `hello from C`로 변경됨)
5. `branch-D`를 main에 merge 시도 → **충돌 발생**

`branch-D`가 분기되던 시점의 main은 `hello from B`였지만, 이미 main은 `hello from C`로 바뀐 상태다. Git 입장에서는 같은 1번째 줄에 `hello from C`(현재 main)와 `hello from D`(브랜치 D) 두 버전이 존재하므로 사람이 직접 선택해줘야 한다. 이때 브랜치가 분기되는 시점이 중요한 것 같다.

---

## 충돌 표시가 파일에 어떻게 나타났는가

실습 순서에 맞게 충돌이 발생하면 Git은 `conflict.md`를 다음과 같이 자동으로 마킹한다

`text<<<<<<< HEAD
hello from D
=======
hello from C
>>>>>>> main`

| 마커 | 의미 |
| --- | --- |
| `<<<<<<< HEAD` | **현재 브랜치(D)** 의 내용 시작 — C를 merge한 후의 main 상태 |
| `=======` | 두 버전의 구분선 |
| `>>>>>>> branch-D` | **병합하려는 브랜치(main)** 의 내용 |

이 세 마커 사이의 내용을 직접 편집하여 원하는 결과물로 만든 뒤 저장하는 것이 충돌 해결이다

---

## 최종적으로 어떤 방식으로 해결했는가

충돌 해결 방법은 **GitHub 웹 에디터** 또는 **로컬 터미널** 두 가지가 있다고 한다. 나는 로컬에서 해결했다.

**GitHub 웹 에디터로 해결하는 경우**

1. PR 페이지에서 `Resolve conflicts` 버튼 클릭
2. 충돌 마커(`<<<<<<<`, `=======`, `>>>>>>>`)를 포함한 모든 라인을 삭제하고, 남길 내용만 유지
3. `Mark as resolved` → `Commit merge` 클릭

**로컬에서 해결하는 경우**

`bashgit checkout branch-D
git merge main              # 최신 main을 D에 반영
# conflict.md 직접 편집 → 마커 제거 후 최종 내용만 남김
git add conflict.md
git commit -m "~~~"
git push origin branch-D`

---

## 해결 후 PR 상태가 어떻게 바뀌었는가

충돌 해결 전에는 PR 페이지에 **"This branch has conflicts that must be resolved"** 경고가 표시되며 merge 버튼이 비활성화된다. 충돌을 해결하고 commit하면 경고가 사라지고 **"Able to merge"** 상태로 바뀌면서 `Merge pull request` 버튼이 활성화된다.

---

## 추가 개념 정리

## Conflict의 정확한 정의

Conflict(충돌)란 **Git이 자동으로 병합할 수 없는 상황**을 의미한다. Git은 서로 다른 줄의 변경은 자동으로 합칠 수 있지만, **같은 줄에 대한 두 가지 다른 버전**이 존재하면 어떤 것이 "정답"인지 알 수 없어 사람에게 판단을 맡긴다.

## 같은 줄 수정 시 충돌이 잘 발생하는 이유

Git의 merge 알고리즘(3-way merge)은 **공통 조상(base)**, **브랜치 A의 변경**, **브랜치 B의 변경** 세 가지를 비교한다. 서로 다른 줄이면 "A는 3번째 줄을 바꿨고 B는 5번째 줄을 바꿨다"고 구분할 수 있다. 하지만 **같은 줄**이면 base 대비 두 버전 모두 변경된 것이므로 어느 쪽을 선택할지 Git이 결정할 수 없다.

## git pull vs git merge의 차이

`git pull`은 사실상 `git fetch` + `git merge`를 합친 명령어다.

| 명령어 | 동작 | 대상 |
| --- | --- | --- |
| `git merge <branch>` | 두 **로컬 브랜치**를 병합 | 로컬 → 로컬 |
| `git pull origin main` | 원격 저장소에서 가져오고(fetch) **자동으로 병합**까지 수행 | 원격 → 로컬 |

## GitHub에서 해결 vs 로컬에서 해결의 차이

| 구분 | GitHub 웹 에디터 | 로컬 터미널 |
| --- | --- | --- |
| 편의성 | 간단한 충돌에 빠름 | 복잡한 충돌에 유리 |
| 도구 | 웹 텍스트 편집기만 사용 | VSCode, IDE 등 활용 가능 |
| 확인 | 바로 PR에 반영 | push 후 PR에 반영 |
| 권장 상황 | 1~2줄 단순 충돌 | 여러 파일, 복잡한 로직 충돌 |

## 왜 최신 main을 먼저 반영해야 하는가

내 브랜치를 PR하기 전에 `git merge main`(또는 `git pull`)으로 **최신 main을 내 브랜치에 먼저 반영**하면 충돌을 로컬에서 미리 해결할 수 있다. 이렇게 하면 PR 올린 후 GitHub에서 충돌이 발생하는 상황을 예방하고, main 브랜치를 깨끗하게 유지할 수 있다. 이 실습 기준으로는 `branch-D`에서 작업하기 전에 `git merge main`을 했다면, C가 merge된 최신 main을 미리 반영하여 충돌을 로컬에서 처리할 수 있었다.