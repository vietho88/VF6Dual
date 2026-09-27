# VF6 Dual v0.1

Experimental Android Auto dual-app controller optimized for Samsung + VF6.

## Goal

One-tap flow:

1. Select App Left + App Right on the phone.
2. VF6 Dual creates a secondary Android display through Shizuku/ADB-shell privileges.
3. It launches both real Android apps onto that display and attempts a 50/50, 60/40 or 70/30 freeform layout.
4. Android Auto opens `VF6 Dual` as a parked `CAR_LAUNCHER` activity.
5. The app mirrors the secondary display at the SurfaceControl/compositor layer (not MediaProjection, not bitmap capture).
6. Basic head-unit tap/swipe is forwarded to the secondary display with `input -d`.

## Important status

This is **v0.1 / physical-device prototype**. It has not been verified on the user's S24 Ultra + VF6 yet.

Expected limitations:

- Android Auto `CAR_LAUNCHER` parked activities are intended for use while parked. Android Auto may exit the activity once vehicle motion is detected.
- Touch v0.1 supports tap + one-finger swipe. Pinch/multi-touch is not implemented yet.
- The virtual-display backend uses Android's `overlay_display_devices` shell setting. A small preview of the secondary display can appear on the phone.
- Freeform task resize is best-effort because Samsung firmware can change ActivityTaskManager behavior.
- Surface mirroring uses the hidden WindowManager `mirrorDisplay` call from a Shizuku UserService. This is deliberately experimental and OEM/firmware dependent.

## Requirements

- Android 11+ for convenient Shizuku Wireless Debugging start; app minSdk is 29.
- Shizuku running with ADB/shell privilege (non-root is OK).
- Android Auto host that surfaces sideloaded parked apps. If the car launcher does not show VF6 Dual, AAEnabler/installer identity may be needed just like other sideloaded Android Auto apps.

## First test

1. Install Shizuku and start it through Wireless Debugging.
2. Install `app-debug.apk`.
3. Open **VF6 Dual** on the phone and tap **Cấp quyền Shizuku**.
4. Choose two apps.
5. Keep `1280×720 / 220 dpi` enabled for the first test.
6. Tap **START VF6**. Confirm the log contains a virtual display ID and both apps launch.
7. Connect Android Auto and open **VF6 Dual** from the car launcher while parked.
8. Test tap and swipe.
9. Use **Stop** to restore the previous `overlay_display_devices` value.

## Build with GitHub Actions

Push this folder to a GitHub repository. `Build VF6 Dual APK` runs automatically.
The successful workflow exposes artifact `VF6Dual-v0.1-debug-apk` containing `app-debug.apk`.

## Safety

Keep complex interaction to parked use. While driving, use normal Android Auto navigation/media controls and voice interaction.
