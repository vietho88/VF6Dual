# VF6 Dual v0.2

Experimental two-app virtual-display controller for Samsung/Android 16 + Android Auto.

## What changed in v0.2

The project now builds **two APKs**:

1. **VF6 Dual main** (`com.vf6.dual`)
   - phone configuration UI
   - Shizuku-powered virtual display
   - select left/right Android apps
   - 50/50, 60/40, 70/30 layouts
   - parking-only full SurfaceControl rendering and touch when the Android Auto host permits it
   - receives signed control commands from the driving companion

2. **VF6 Dual Driving** (`com.vf6.dual.driving`)
   - Android for Cars App Library 1.7.0 templated companion
   - remains a low-distraction session controller
   - Start / reconnect, Swap and Stop commands
   - intentionally does **not** mirror arbitrary app UIs or enable unrestricted app touch while driving

## Important safety / platform limitation

Android Auto supports arbitrary full-screen activities only in supported parked categories. While driving, template apps must follow driver-distraction restrictions. v0.2 does not bypass those restrictions.

The driving companion uses the IOT template category only as a local development prototype for controlling the VF6 Dual session. It is **not intended for Play Store distribution** in its current form.

## Build

GitHub Actions builds both modules:

```text
:app:assembleDebug
:driving:assembleDebug
```

Artifacts:

```text
VF6Dual-main-v0.2-debug-apk/app-debug.apk
VF6Dual-driving-v0.2-debug-apk/driving-debug.apk
```

## Install order

1. Install `app-debug.apk` on the phone.
2. Start Shizuku and grant VF6 Dual permission.
3. Choose the two apps and layout in VF6 Dual main.
4. Install `driving-debug.apk`.
5. In Android Auto Developer settings, enable Unknown sources if needed for local testing.
6. If the driving companion does not appear after direct install, reinstall only `driving-debug.apk` through your Android Auto sideload installer.

## Usage

Phone:

```text
VF6 Dual main
  Left app  -> Google Maps / Waze / any launchable app
  Right app -> YouTube Music / Spotify / any launchable app
  Layout    -> 50/50, 60/40, 70/30
  START VF6
```

Android Auto Driving companion:

```text
VF6 Dual Driving
  Start / reconnect
  Swap left / right
  Stop virtual session
```

Full dual-app visual rendering remains parking-only. The Driving companion is deliberately a controller, not a restriction bypass.
