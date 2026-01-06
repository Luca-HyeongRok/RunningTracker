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

<img width="311" height="666" alt="img" src="https://github.com/user-attachments/assets/14016123-15b6-4ef0-989e-d2902865a961" />
<img width="309" height="658" alt="img_1" src="https://github.com/user-attachments/assets/576c72bd-7762-47b7-81b2-dbde36d55aa8" />
<img width="321" height="665" alt="img_2" src="https://github.com/user-attachments/assets/8007cc65-f79c-4f74-93c9-93ef37fc3006" />
<img width="321" height="673" alt="img_3" src="https://github.com/user-attachments/assets/38a87645-9ceb-458e-8176-4723f2697494" />



