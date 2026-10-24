# FFLauncher — Android project

FFLauncher is a Kotlin + Jetpack Compose launcher for the Free Fire package `com.dts.freefireth`, with installation/version detection, manual launch, optional launch-on-start, settings, an in-app HUD lab, and an optional Android overlay showing artificial test targets.

## Important limits

The HUD lab/overlay is an independent visual simulation. It does not read game memory, hook Free Fire, extract real player coordinates, reveal real players through walls, automate aiming, or bypass anti-cheat. The simulated obstacle only demonstrates UI state. Android requires the user to explicitly grant “Display over other apps” before the overlay can appear.

## Build

Requirements: JDK 17, Android SDK platform 35 and build-tools 35.0.0, Gradle 8.11.1 or compatible.

From this directory:

```bash
gradle assembleDebug
```

APK output: `app/build/outputs/apk/debug/app-debug.apk`.

The project intentionally uses a Gradle installation in CI rather than a fabricated/empty wrapper. The GitHub Actions workflow included at `.github/workflows/android.yml` extracts this project from `FFLauncher-source.zip` in the repository root, then compiles it.
