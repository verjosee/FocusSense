# FocusSense — Intelligent Focus & Hardware Environment Sensor Suite for Android

[![Platform](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com/)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.10-purple.svg)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-blue.svg)](https://developer.android.com/jetpack/compose)
[![Room](https://img.shields.io/badge/SQLite-Room%202.8.5-orange.svg)](https://developer.android.com/training/data-storage/room)
[![Sensors](https://img.shields.io/badge/Hardware-Accelerometer%20%7C%20Ambient%20Light-teal.svg)](https://developer.android.com/guide/topics/sensors/sensors_overview)

**FocusSense** is a modern, academic-grade Android productivity application engineered for students. It combines a distraction-free Pomodoro study timer with real-time physical environmental monitoring using Android hardware sensors.

During focus sessions, FocusSense monitors device movement via the **Accelerometer** to detect study interruptions or phone handling, and tracks ambient room lighting via the **Ambient Light Sensor** to ensure students study in ergonomically optimal lighting conditions.

---

## Table of Contents
1. [Primary Objective & Architecture](#primary-objective--architecture)
2. [Key Features](#key-features)
3. [Hardware Sensor Engineering](#hardware-sensor-engineering)
4. [Database & Storage Architecture](#database--storage-architecture)
5. [Domain & Timer Engine Logic](#domain--timer-engine-logic)
6. [Design System & UI Components](#design-system--ui-components)
7. [Screens & Navigation Flow](#screens--navigation-flow)
8. [Tech Stack & Gradle Configuration](#tech-stack--gradle-configuration)
9. [Project Directory Structure](#project-directory-structure)
10. [Permissions](#permissions)
11. [Setup, Build, and Run Instructions](#setup-build-and-run-instructions)
12. [Unit Testing & Verification](#unit-testing--verification)
13. [Limitations & Future Enhancements](#limitations--future-enhancements)

---

## 1. Primary Objective & Architecture

FocusSense was designed from the ground up following clean architecture principles and the Android **MVVM (Model-View-ViewModel)** architectural pattern.

### Architecture Layering

```
  ┌─────────────────────────────────────────────────────────────┐
  │                   Jetpack Compose UI Layer                  │
  │  (Screens, Custom Components, Material 3 Dark-First Theme)   │
  └──────────────────────────────┬──────────────────────────────┘
                                 │ Observes StateFlow / Sends Events
                                 ▼
  ┌─────────────────────────────────────────────────────────────┐
  │                     Presentation Layer                      │
  │    (HomeViewModel, FocusTimerViewModel, SensorsViewModel,   │
  │     HistoryViewModel, StatisticsViewModel, SettingsViewModel│
  └──────────────────────────────┬──────────────────────────────┘
                                 │ Executes Business Operations
                                 ▼
  ┌─────────────────────────────────────────────────────────────┐
  │                        Domain Layer                         │
  │    (FocusTimerEngine, Monotonic TimeProvider, Model Mappers, │
  │     StatisticsCalculator, SessionState & Aggregators)       │
  └──────────────────────────────┬──────────────────────────────┘
                                 │ Coordinates Repositories
                 ┌───────────────┴───────────────┐
                 ▼                               ▼
  ┌─────────────────────────────┐ ┌─────────────────────────────┐
  │      Data & Storage         │ │       Hardware Sensors      │
  │  (Room SQLite DB, DAOs,     │ │  (SensorManager, Listeners, │
  │   Entities, Repositories,   │ │   AccelerometerMonitor,     │
  │   DataStore Preferences)    │ │   AmbientLightMonitor)      │
  └─────────────────────────────┘ └─────────────────────────────┘
```

- **Separation of Concerns:** Zero UI code resides in business logic or data modules. ViewModels never import `android.content.Context` directly, relying on clean interfaces and repositories injected through a centralized `ViewModelFactory`.
- **Reactive Unidirectional Data Flow (UDF):** Every screen consumes immutable StateFlow streams (`timerState`, `allSessions`, `weeklyBreakdown`, `telemetryState`), reacting declaratively to state mutations.

---

## 2. Key Features

- **Monotonic Focus Timer:** Built on `SystemClock.elapsedRealtime()` to guarantee microsecond precision unaffected by device wall-clock adjustments, daylight savings, or sleep mode throttling.
- **Hardware Movement Detection:** Continuously calculates 3D Euclidean acceleration vector differentials ($\Delta = \sqrt{\Delta x^2 + \Delta y^2 + \Delta z^2}$), filtering out gravity to detect phone pickups and student fidgeting.
- **Motion Alert Banner & Haptics:** Prompts students when excess movement is detected during an active focus block with an interactive dismissal mechanism.
- **Ambient Lighting Ergonomics:** Samples lux levels throughout the session, automatically categorizing study lighting into `VERY_DIM` (<50 lx), `DIM` (50–200 lx), `MODERATE` (200–500 lx), and `BRIGHT` (>500 lx) with targeted ergonomic study recommendations.
- **Hardware-Missing Fallbacks:** Gracefully handles emulators or hardware lacking ambient light or accelerometer sensors without crashing or corrupting sessions.
- **Persistent SQLite Database:** Powered by Room 2.8.5 with indices on `session_start_time` and `session_status`, storing comprehensive study analytics, movement counts, and average lux.
- **Session Notes & Reflections:** Allows students to attach custom notes and key takeaways to every completed or interrupted session, editable at any time.
- **Interactive Analytics & 7-Day Chart:** Renders weekly distribution bar graphs, completion rate percentages, total focus time, and today's achievements.
- **User Preferences via DataStore:** Persists customizable default focus durations (15m, 25m, 45m, 60m), motion alert toggles, and UI theme preferences (Dark, Light, System).

---

## 3. Hardware Sensor Engineering

FocusSense integrates directly with physical Android hardware using `SensorManager` registered in `SensorManager.SENSOR_DELAY_NORMAL` to optimize CPU cycles and battery consumption.

### Accelerometer (`Sensor.TYPE_ACCELEROMETER`)
- **Formula:** 
  $$\Delta = \sqrt{(x_2 - x_1)^2 + (y_2 - y_1)^2 + (z_2 - z_1)^2}$$
- **Thresholding:** Normal stationary resting jitter ranges from $0.01$ to $0.05 \text{ m/s}^2$. The threshold is calibrated at $1.8 \text{ m/s}^2$ to differentiate intentional desk movements or device handling from background noise.
- **Throttling:** Movement events are debounced with a $1.5\text{-second}$ cooldown to prevent counting a single continuous movement as multiple distinct events.

### Ambient Light Sensor (`Sensor.TYPE_LIGHT`)
- **Photometric Reading:** Captures ambient illuminance in Lux ($\text{lx}$).
- **Categorization Logic:**
  - `< 50 lx`: `VERY_DIM` — High eye strain hazard; recommends turning on desk lamps.
  - `50 – 200 lx`: `DIM` — Suitable for casual screen reading, but insufficient for paper note-taking.
  - `200 – 500 lx`: `MODERATE` — Optimal ergonomic range recommended by ISO lighting standards for sustained study.
  - `> 500 lx`: `BRIGHT` — Well-lit daylight or intense lighting.
- **Session Averages:** During an active timer, illuminance readings are accumulated into a running average ($\frac{\sum \text{lux}}{N}$) stored alongside the session in SQLite.

---

## 4. Database & Storage Architecture

### Room SQLite Table Schema (`study_sessions`)

| Column Name | SQLite Type | Primary Key | Description |
| :--- | :--- | :--- | :--- |
| `id` | `INTEGER` | Yes (Auto) | Unique session ID |
| `session_start_time` | `INTEGER` | No (Indexed) | Epoch millisecond timestamp of session start |
| `session_end_time` | `INTEGER` | No | Epoch millisecond timestamp of session conclusion |
| `duration_seconds` | `INTEGER` | No | Net focused duration in seconds |
| `session_status` | `TEXT` | No (Indexed) | `COMPLETED`, `INTERRUPTED`, or `CANCELLED` |
| `notes` | `TEXT` | No (Nullable) | User-entered study reflection notes |
| `movement_count` | `INTEGER` | No | Number of significant motion events during session |
| `average_light_lux` | `REAL` | No (Nullable) | Mean ambient illuminance during session |

### DAO Queries (`StudySessionDao`)
- `getAllSessions(): Flow<List<StudySessionEntity>>` — Reactive stream ordered by `session_start_time DESC`.
- `getSessionsInRange(startMs, endMs): Flow<List<StudySessionEntity>>` — Range query for daily/weekly metrics.
- `getTodayCompletedCount(startOfDayMs): Flow<Int>` — Real-time badge counter.
- `getTodayTotalDuration(startOfDayMs): Flow<Long?>` — Net seconds studied today.
- `getTotalStudyDuration(): Flow<Long?>` — All-time student effort aggregator.
- `updateNotes(sessionId, notes): Int` — Updates notes asynchronously.
- `deleteSessionById(sessionId): Int` — Cascade deletion helper.

---

## 5. Domain & Timer Engine Logic

### FocusTimerEngine
- **Monotonic Precision:** Uses `TimeProvider.elapsedRealtime()` so pausing, backgrounding, and screen locks do not skew countdown accuracy.
- **State Machine:**
  - `IDLE`: Configured with duration, ready to start.
  - `RUNNING`: Actively counting down, listening to sensors, calculating progress ($0.0 \to 1.0$).
  - `PAUSED`: Elapsed duration accumulated, sensor listeners paused to conserve battery.
  - `COMPLETED`: Countdown reached zero, triggers celebration dialog and note prompt.
- **Duplicate Prevention:** Atomic `buildAndConsumeSession()` guarantees a session record is only committed to SQLite once.

---

## 6. Design System & UI Components

### Color Palette (Dark-First Default)
- **Primary Accent (`SoftViolet`):** `#8B7CFF` — Primary action buttons, active indicator pills, progress rings.
- **Secondary Accent (`SkyBlue`):** `#57C7FF` — Sensor gauges, secondary accents.
- **Background (`DeepNight`):** `#0B0D14` — Deep, distraction-free OLED background.
- **Surface (`DarkSlate`):** `#151925` — Card backgrounds and dialog containers.
- **Surface Variant (`SlateVariant`):** `#202638` — Filter chips, borders, and unselected tab states.
- **Success (`SuccessGreen`):** `#53D6A1` — Completed session badges and optimal light status.
- **Warning (`WarningAmber`):** `#FFCA78` — Motion alerts and dim light warnings.
- **Error (`ErrorRed`):** `#FF6B6B` — Interrupted session badges and reset actions.
- **Typography:** Modern, legible sans-serif hierarchy paired with monospace numerals (`00:00`) to prevent jittering during countdowns.

---

## 7. Screens & Navigation Flow

1. **Home Screen (`/home`):**
   - Quick greeting with daily progress overview (Today Focus, Completed Sessions, All-Time Hours).
   - "Quick Start" card jumping directly into a 25-minute Pomodoro session.
   - Live hardware status indicators (accelerometer status and room light indicator).
   - Recent 3 sessions preview with quick navigation to History.
2. **Focus Timer Screen (`/focus`):**
   - Elegant sweep gradient countdown ring with animated percentage progress.
   - Preset duration selector chips (15m, 25m, 45m, 60m).
   - Controls: Start, Pause, Resume, Stop.
   - Active motion warning banner displaying physical movements in real-time.
   - Completion modal allowing note recording before saving to Room SQLite.
3. **Hardware Sensors Screen (`/sensors`):**
   - Real-time Accelerometer vector card: 3-axis readings ($X, Y, Z$) and net magnitude.
   - Ambient Light telemetry card: Real-time Lux gauge, categorization badge, and ergonomic study tips.
   - Hardware detection banner identifying whether sensors are physical or missing.
4. **History Screen (`/history`):**
   - Full reverse-chronological list of past study blocks.
   - Status filters: `ALL`, `COMPLETED`, `INTERRUPTED`.
   - Card details: Duration, time range, movement count, average lux, and attached notes.
   - Note editing dialog and session deletion with confirmation.
5. **Statistics Screen (`/statistics`):**
   - 4 High-level metric cards: Total Study Time, Today Study Time, Completed Sessions, Completion Rate.
   - 7-Day interactive study bar chart with daily duration bars and day-of-week labels.
   - Completion rate progress bar.
6. **Settings Screen (`/settings`):**
   - Theme Mode Selection (Dark, Light, System Default).
   - Default Focus Length configuration (15, 25, 45, 60 minutes).
   - Motion Alert toggle during active focus.
   - Data management: "Clear All Study History" with security confirmation dialog.
   - Application metadata and version info.

---

## 8. Tech Stack & Gradle Configuration

| Component | Technology | Version |
| :--- | :--- | :--- |
| **Language** | Kotlin | `2.2.10` |
| **UI Toolkit** | Jetpack Compose (BOM) | `2026.02.01` |
| **Material Design** | Material 3 | `1.3.1` |
| **Database** | Room SQLite + KSP2 | `2.8.5` |
| **Symbol Processing** | Google KSP | `2.2.10-2.0.2` |
| **Navigation** | Navigation Compose | `2.8.8` |
| **Preferences** | AndroidX DataStore | `1.1.2` |
| **Concurrency** | Kotlin Coroutines & Flow | `1.10.1` |
| **Build System** | Gradle | `9.5.0` |
| **Android Plugin** | Android Gradle Plugin (AGP) | `9.3.3` |
| **Minimum SDK** | Android 8.0 (API 26) | `26` |
| **Target / Compile SDK**| Android 15 (API 35) | `35` |
| **Java Compatibility** | OpenJDK 21 | `JavaVersion.VERSION_21` |

---

## 9. Project Directory Structure

```
FocusSense/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/focussense/app/
│   │   │   │   ├── data/
│   │   │   │   │   ├── local/
│   │   │   │   │   │   ├── dao/StudySessionDao.kt
│   │   │   │   │   │   ├── entity/StudySessionEntity.kt
│   │   │   │   │   │   ├── preferences/FocusSettingsDataStore.kt
│   │   │   │   │   │   └── FocusSenseDatabase.kt
│   │   │   │   │   └── repository/
│   │   │   │   │       ├── SettingsRepository.kt & Impl.kt
│   │   │   │   │       └── StudySessionRepository.kt & Impl.kt
│   │   │   │   ├── domain/
│   │   │   │   │   ├── model/
│   │   │   │   │   │   ├── FocusSession.kt
│   │   │   │   │   │   ├── SessionStatus.kt
│   │   │   │   │   │   ├── SessionMappers.kt
│   │   │   │   │   │   ├── StatisticsSummary.kt
│   │   │   │   │   │   └── StatisticsCalculator.kt
│   │   │   │   │   └── timer/
│   │   │   │   │       ├── FocusTimerEngine.kt
│   │   │   │   │       ├── TimerState.kt
│   │   │   │   │       ├── TimerMode.kt
│   │   │   │   │       └── TimeProvider.kt
│   │   │   │   ├── presentation/
│   │   │   │   │   ├── components/
│   │   │   │   │   │   ├── CircularProgressTimer.kt
│   │   │   │   │   │   ├── FocusBottomBar.kt
│   │   │   │   │   │   ├── FocusIcons.kt
│   │   │   │   │   │   ├── FocusSensorsTopAppBar.kt
│   │   │   │   │   │   ├── SensorMeterCard.kt
│   │   │   │   │   │   ├── SessionItemCard.kt
│   │   │   │   │   │   ├── StatCard.kt
│   │   │   │   │   │   └── WeeklyBarChart.kt
│   │   │   │   │   ├── navigation/
│   │   │   │   │   │   ├── Screen.kt
│   │   │   │   │   │   └── AppNavigation.kt
│   │   │   │   │   ├── screens/
│   │   │   │   │   │   ├── HomeScreen.kt
│   │   │   │   │   │   ├── FocusScreen.kt
│   │   │   │   │   │   ├── SensorsScreen.kt
│   │   │   │   │   │   ├── HistoryScreen.kt
│   │   │   │   │   │   ├── StatisticsScreen.kt
│   │   │   │   │   │   └── SettingsScreen.kt
│   │   │   │   │   └── viewmodel/
│   │   │   │   │       ├── FocusTimerViewModel.kt
│   │   │   │   │       ├── HistoryViewModel.kt
│   │   │   │   │       ├── HomeViewModel.kt
│   │   │   │   │       ├── SensorsViewModel.kt
│   │   │   │   │       ├── SettingsViewModel.kt
│   │   │   │   │       ├── StatisticsViewModel.kt
│   │   │   │   │       └── ViewModelFactory.kt
│   │   │   │   ├── sensors/
│   │   │   │   │   ├── model/
│   │   │   │   │   │   ├── AccelerationData.kt
│   │   │   │   │   │   ├── LightCategory.kt
│   │   │   │   │   │   └── LightData.kt
│   │   │   │   │   ├── AccelerometerMonitor.kt
│   │   │   │   │   ├── AmbientLightMonitor.kt
│   │   │   │   │   ├── SensorRepository.kt
│   │   │   │   │   └── DefaultSensorRepository.kt
│   │   │   │   ├── ui/theme/
│   │   │   │   │   ├── Color.kt
│   │   │   │   │   ├── Shape.kt
│   │   │   │   │   ├── Theme.kt
│   │   │   │   │   └── Type.kt
│   │   │   │   └── MainActivity.kt
│   │   │   └── AndroidManifest.xml
│   │   └── test/java/com/focussense/app/
│   │       ├── domain/model/StatisticsCalculationTest.kt
│   │       ├── domain/timer/FocusTimerEngineTest.kt
│   │       └── sensors/SensorClassificationTest.kt
│   └── build.gradle.kts
├── gradle/
│   ├── libs.versions.toml
│   └── wrapper/
├── build.gradle.kts
├── gradle.properties
├── settings.gradle.kts
└── README.md
```

---

## 10. Permissions

FocusSense strictly follows the principle of least privilege:

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <!-- Required for feedback vibration when timer completes or motion warning triggers -->
    <uses-permission android:name="android.permission.VIBRATE" />
</manifest>
```

- **Note on Sensors:** Neither `Sensor.TYPE_ACCELEROMETER` nor `Sensor.TYPE_LIGHT` requires runtime permissions on Android 8.0–15 (`android.permission.HIGH_SAMPLING_RATE_SENSORS` is not required for `SENSOR_DELAY_NORMAL`).

---

## 11. Setup, Build, and Run Instructions

### Prerequisites
- **JDK:** OpenJDK 21 (e.g. Android Studio JBR: `C:\Program Files\Android\Android Studio\jbr`)
- **Android SDK:** Platforms `android-35` and Build Tools `35.0.0`
- **Gradle:** Wrapper included (`gradlew`)

### Build Commands

```bash
# 1. Clone repository
git clone <repo-url>
cd FocusSense

# 2. Build Debug APK
./gradlew assembleDebug

# Output APK path:
# app/build/outputs/apk/debug/app-debug.apk

# 3. Run all Unit Tests
./gradlew test

# 4. Install onto connected Android physical device or emulator
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

---

## 12. Unit Testing & Verification

Comprehensive unit tests cover business logic, math calculations, and state machines:

1. **`FocusTimerEngineTest`**:
   - Monotonic time tracking, pausing, resuming, stopping, and completing.
   - Protection against double-consumption of completed sessions.
   - Idle state restoration upon reset.
2. **`StatisticsCalculationTest`**:
   - 7-day breakdown aggregation logic and day name formatting.
   - Completion rate percentage calculations.
   - Edge case handling with zero sessions and multiple same-day sessions.
3. **`SensorClassificationTest`**:
   - Lux boundary mappings (`VERY_DIM`, `DIM`, `MODERATE`, `BRIGHT`) and negative sensor anomaly handling.
   - 3D Euclidean delta magnitude calculation.
   - Stationary resting jitter filtration vs. significant movement detection.

---

## 13. Limitations & Future Enhancements

- **Physical Device Required for Sensors:** While the app gracefully displays informational placeholders on emulators lacking physical sensors, testing real hardware readings requires a physical Android phone.
- **Background Foreground Service:** While the timer calculates elapsed time monotonically across app backgrounding upon return, a persistent Android Foreground Service with a sticky notification can be added to provide lock screen countdown controls.
- **Exporting Study Data:** Future releases can add CSV / JSON export of study sessions for academic productivity tracking.
