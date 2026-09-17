# Sensify  -  Android Sensor Telemetry & Visualizer

A modern, high-performance, open-source Android application designed for real-time hardware sensor monitoring, physical telemetry visualization, and signal analysis.

Sensify turns any Android device into an interactive sensor lab, catering to casual users who want to inspect hardware functionality, as well as engineers, students, and researchers who require precise physical telemetry and live waveform plots.

---

## Features

### Real-Time Physical Visualizers
- **Bubble Spirit Level**: 2D tilt bubble visualizer with real-time pitch and roll readouts in degrees, responsive spring physics, and level-calibration indication. Powered by Accelerometer and Gravity sensors.
- **True Cardinal Compass**: 3D rotation-matrix azimuth heading dial (0 deg to 360 deg) with cardinal ticks (N/E/S/W) and top red lubber line. Uses device Rotation Vector / Geomagnetic flux calculation instead of uncalibrated raw values.
- **Dynamic Radial Gauges**: Smooth sweep arc gauges with live numeric metrics for Ambient Light (lx), Atmospheric Pressure (hPa), and custom environmental sensors.
- **Proximity Radar**: Distance radar with reactive sonar ripple animation indicating NEAR (< 3 cm) vs FAR object state.

### Interactive Telemetry & Advanced Signal Analysis
- **Live Waveform Charts**: Multi-axis real-time line charts rendered with high-throughput MPAndroidChart integration. Supports X, Y, Z dynamic streams with bounded memory windows.
- **Signal Modulus & Frequency Tracking**: Real-time vector magnitude computation alongside real-time sampling rate calculation (Hz) with exponential moving average smoothing.
- **Physical Sensor Fusion Explanations**: Deep technical documentation for each sensor:
  - Difference between *Game Rotation Vector* (Gyro + Accel, no magnetic drift) vs *Geomagnetic Rotation Vector* (Accel + Compass, low power).
  - Physical measurement mechanisms (MEMS capacitive combs, Lorentz magnetic force, piezoresistive diaphragms, photodiodes).
  - Axis definitions, measurement units (m/s^2, rad/s, uT, lx, hPa), and full hardware manufacturer specifications (vendor, version, power draw, resolution).

### Performance & Modern Architecture
- **Dedicated Background Sensor Looper**: Sensor events are dispatched onto a dedicated daemon HandlerThread (THREAD_PRIORITY_MORE_FAVORABLE), eliminating UI frame drops and ANR freezes even at >100 Hz hardware interrupt rates.
- **Zero Memory Leaks (Bounded Buffers)**: Chart data pipelines enforce strict circular capacity bounds (N=40 frame window) to avoid garbage collection thrashing and out-of-memory (OOM) exits.
- **Adaptive Multi-Device Layout**: Fully responsive across compact phones, tall aspect ratios, and foldables/tablets (>600 dp) via Jetpack Compose adaptive constraints.
- **100% Offline & FOSS**: Zero analytics, zero ads, zero external tracker libraries, and zero runtime permission requirements for standard motion/environmental sensors.

---

## Tech Stack & Architecture

- **Language**: Kotlin 2.0+ (100% Kotlin)
- **UI Framework**: Jetpack Compose with Material Design 3 (M3)
- **Design Paradigm**: Clean Architecture with unidirectional state flow (StateFlow, SharedFlow, Coroutines)
- **Hardware Integration**: Android Sensors Framework (android.hardware.SensorManager, SensorEventListener)
- **Graphics & Charting**: MPAndroidChart + Compose Canvas vector math
- **Build System**: Gradle 8.9 with Android Gradle Plugin 8.9.0 (Java 17 target)

---

## Getting Started

### Prerequisites
- Android Studio Ladybug / Meerkat or newer
- JDK 17
- Android SDK with Platform 35 (compileSdk 35, minSdk 24)

### Build & Run
1. Clone the repository:
```bash
git clone https://github.com/DreamyClamp/Sensify.git
cd Sensify
```

2. Build the debug APK via Gradle:
```bash
./gradlew assembleDebug
```

3. Install onto a connected Android device:
```bash
./gradlew installDebug
```

Or locate the generated APK at:
```
app/build/outputs/apk/debug/app-debug.apk
```

---

## Project Structure

```
io.sensify.sensor/
|-- domains/
|   |-- chart/             # Chart data pipelines, streaming accumulators, and MPChart bridges
|   \-- sensors/           # Hardware sensor management, background threading, and physics math
|       |-- metadata/      # SensorMath (quaternion transformations, headings) and physics info
|       |-- packets/       # Background HandlerThread provider and event buffering
|       \-- provider/      # Device sensor enumeration and registry
\-- ui/
    |-- components/
    |   |-- cards/         # Modern interactive sensor cards with spring bounce
    |   \-- visualizers/   # Custom 2D Bubble Level, Compass, Radial Gauge, Proximity radar
    |-- navigation/        # Jetpack Compose Navigation graph
    |-- pages/
    |   |-- home/          # Home dashboard, category filters, and active sensor carousel
    |   |-- sensor/        # Detail screen: hero visualizer, waveform chart, hardware specs
    |   \-- about/         # Open-source info and project overview
    \-- resource/          # Themes, Material 3 palettes, and dynamic typography
```

---

## Open-Source References & Credits

This project is built using open-source libraries and standards:

- [Android Jetpack Compose](https://developer.android.com/jetpack/compose)  -  Modern toolkit for native Android UI.
- [Kotlin Coroutines & Flow](https://github.com/Kotlin/kotlinx.coroutines)  -  Asynchronous and reactive programming for Kotlin.
- [MPAndroidChart](https://github.com/PhilJay/MPAndroidChart) by Philipp Jahoda  -  Powerful chart engine for Android.
- [Android Open Source Project (AOSP) Sensor API](https://source.android.com/docs/core/interaction/sensors)  -  Official Android hardware sensor documentation and physical coordinate system standards.
- [Material Design 3](https://m3.material.io/)  -  Google's Material 3 design guidelines and color system.
