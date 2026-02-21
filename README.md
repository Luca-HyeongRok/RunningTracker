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

## 🚀 설치 및 실행 (Getting Started)

### 1) 사전 준비
- Android Studio (최신 안정 버전 권장)
- JDK 17 (Android Studio 내장 JDK 사용 가능)
- Google Maps API Key

### 2) 프로젝트 클론
```bash
git clone <repo-url>
cd RunningTracker
```

### 3) Google Maps API 키 설정
`app/src/main/res/values/google_maps_key.xml` 파일을 생성하고 아래 내용을 추가하세요.

```xml
<resources>
    <string name="google_maps_key" translatable="false">YOUR_API_KEY</string>
</resources>
```

출시 기준 권장 설정:
- 디버그/릴리스 API 키를 분리해서 사용
- Google Cloud Console에서 `Maps SDK for Android` 활성화
- API 키 제한(Application restrictions) 설정:
  - Android apps
  - 패키지명: `com.example.runningtracker`
  - SHA-1 인증서 지문: 디버그/릴리스 각각 등록
- API restrictions에서 Maps 관련 API만 허용

참고:
- `google_maps_key.xml`은 `.gitignore`에 포함되어 있으므로 저장소에 커밋되지 않습니다.
- 릴리스 빌드 전 실제 릴리스 keystore 기준 SHA-1로 동작 확인이 필요합니다.

### 4) 앱 실행
- Android Studio에서 프로젝트를 연 뒤 에뮬레이터 또는 실제 기기에서 실행
- 또는 터미널에서 디버그 빌드:

```bash
./gradlew assembleDebug
```

Windows:
```bash
gradlew.bat assembleDebug
```

---
## 🔐 권한 및 개인정보 처리 (출시 기준)

### 권한 사용 목적
- `ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION`: 러닝 중 위치 추적 및 경로 표시
- `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_LOCATION`: 러닝 중 백그라운드 위치 추적 유지
- `POST_NOTIFICATIONS` (Android 13+): 포그라운드 서비스 알림 표시

### 수집/저장 데이터 (현재 구현 기준)
- 러닝 기록: 시작 시각, 총 운동 시간, 평균 속도, 이동 거리
- 위치 데이터: 러닝 경로 표시를 위해 실시간 사용
- 저장 위치: 기기 내부 Room DB (`running_database`)

### 외부 전송 및 제3자 제공 (현재 구현 기준)
- 별도 백엔드 서버로 러닝 기록을 전송하지 않음
- Google Maps SDK 사용으로 지도 표시 관련 네트워크 통신이 발생할 수 있음

### 사용자 안내
- 위치 권한이 없으면 위치 기반 기능이 동작하지 않음
- 권한은 기기 설정에서 언제든 변경 가능

> 실제 스토어 출시 시에는 별도의 Privacy Policy 문서(URL)와 Play Console 데이터 보안 양식 내용을 본 섹션과 일치시키세요.

---
## 📄 Privacy Policy

- 정책 URL: `https://example.com/running-tracker/privacy`
- 출시 전 위 URL을 실제 공개 문서 주소로 교체하세요.
- 앱 내 설정 화면/온보딩, Play Store 등록 정보(개인정보처리방침)와 동일한 URL을 사용하세요.

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
