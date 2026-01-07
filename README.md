# Running Tracker 🏃‍♂️

**Running Tracker**는 사용자의 러닝 활동을 기록하고 시각적으로 확인할 수 있는  
안드로이드 러닝 트래커 애플리케이션입니다.  
**Kotlin**과 **Jetpack Compose**를 기반으로 MVVM 아키텍처를 적용해 개발되었습니다.

러닝 중 경과 시간과 이동 경로를 실시간으로 확인할 수 있으며,  
단순하고 직관적인 UI를 목표로 설계되었습니다.

---

## ✨ 주요 기능 (Key Features)

- **러닝 타이머**
    - ViewModel에서 직접 관리되는 코루틴 기반 타이머
    - Start / Pause / Stop 동작 분리
    - Pause 후 Start 시 시간 유지, Stop 시에만 초기화

- **실시간 위치 경로 표시**
    - Google Maps Compose를 활용한 러닝 경로 시각화
    - 러닝 중 이동한 경로를 Polyline으로 지도에 표시

- **상태 기반 UI**
    - UI는 ViewModel의 `StateFlow`만 구독
    - 비즈니스 로직과 UI 로직을 명확히 분리

- **의존성 주입**
    - Koin을 활용한 ViewModel 및 위치 클라이언트 주입
    - 테스트 및 확장에 용이한 구조

---

## 🛠 기술 스택 (Tech Stack)

- **Language**: Kotlin
- **UI**: Jetpack Compose (Material3)
- **Architecture**: MVVM
- **State Management**: StateFlow
- **Asynchronous**: Kotlin Coroutines
- **Dependency Injection**: Koin
- **Maps**: Google Maps Compose
- **Location**: FusedLocationProviderClient

---

## 🧱 프로젝트 구조 (간략)
```file
app/
├─ presentation/
│ ├─ running/
│ │ ├─ RunningScreen.kt
│ │ ├─ RunningViewModel.kt
│ │ └─ RunningUiState.kt
│ ├─ ButtonControls.kt
│ └─ RunningMap.kt
├─ location/
│ └─ LocationClient.kt
├─ di/
│ └─ appModule.kt
└─ util/
└─ TimeFormatter.kt
``` 
---
## 📱 실행 화면

### 러닝 화면
| 초기 화면 | 러닝 시작 |
|----------|----------|
| <img src="https://github.com/user-attachments/assets/14016123-15b6-4ef0-989e-d2902865a961" width="300"/> | <img src="https://github.com/user-attachments/assets/576c72bd-7762-47b7-81b2-dbde36d55aa8" width="300"/> |

### 상태 변화 / 기록
| 일시정지 | 러닝 기록 |
|----------|----------|
| <img src="https://github.com/user-attachments/assets/8007cc65-f79c-4f74-93c9-93ef37fc3006" width="300"/> | <img src="https://github.com/user-attachments/assets/38a87645-9ceb-458e-8176-4723f2697494" width="300"/> |

---
## 🆕 업데이트 (2026.01.07)

###  1. 러닝 로직 분리 (리팩토링)

서비스 코드 복잡도를 줄이기 위해 핵심 로직을 역할 단위로 분리했습니다.

 - **RunTimer**

   - 시간 측정 전용 컴포넌트

   - 코루틴 기반, 순수 비즈니스 로직

- **RunLocationTracker**

  - 위치 수집 전용 컴포넌트

  - LocationClient를 통해 Flow 기반 위치 스트림 처리

```text
RunningService
├─ RunTimer (시간 측정)
└─ RunLocationTracker (위치 수집)
```
- 서비스는 오케스트레이션 역할만 담당하도록 정리
- 테스트 및 유지보수 용이성 향상

--- 
### 2. 배터리 상태 기반 러닝 제어

- BroadcastReceiver 기반 배터리 상태 감지

- Android 시스템 브로드캐스트(ACTION_BATTERY_CHANGED) 활용

동작 규칙:

**러닝 시작 시**

 - 배터리 30% 이하 → 경고 메시지 표시 (운동은 시작 가능)

**러닝 중**

- 배터리 20% 이하 → 자동으로 러닝 종료 + 기록 저장

사용자 경험을 해치지 않으면서 안정성 확보

---
### 3. 테스트 코드 추가 (JVM 단위 테스트)

- Robolectric + coroutines-test 기반 단위 테스트 작성

- Android 의존성이 없는 핵심 로직 위주로 테스트

테스트 대상: RunTimer

- start / pause / stop 동작 검증

- 시간 증가 및 초기화 로직 확인