# Kuro (黒)

Kuro is an isolated app vault for modern Android. It keeps a private set of apps
you choose — added from apps already installed on the device, or imported
directly from an APK file — and launches each one in its own isolated task
stack with per-app vault storage. 64-bit first: APKs are ABI-checked on import
so only packages your device can actually run enter the vault.

Public repo: https://github.com/vastobb/kuro

## Features

- Add apps from installed apps (launcher query, search, one-tap add).
- Import APK files (system file picker, copy into vault, manifest parse).
- 64-bit ABI gate: native `lib/` entries are checked against
  `Build.SUPPORTED_ABIS`; incompatible APKs are rejected with a reason.
- Isolated launch: every vault app starts with `FLAG_ACTIVITY_NEW_TASK` plus
  `FLAG_ACTIVITY_MULTIPLE_TASK` and Kuro extras, so windows stay grouped per
  app and away from the main task stack.
- Per-app vault data dir with one-tap clear; crash in a launched app cannot
  take down Kuro (separate processes, result-checked launches).
- Clean original design: white + black + vermilion red, Material 3, dark mode
  follows the system, springy grid, animated navigation, monogram avatars.
  Deliberately not a BlackBox look-alike and no AI-gradient styling.

## Design

- `white #FFFFFF` background, `black #111111` ink, `vermilion #E53935` accent.
- Jetpack Compose + Material 3, edge-to-edge, adaptive launcher icon from
  `kuro.png` (black enso).
- Hand-rolled 3-destination navigation (Home / Add / Settings), no nav-graph
  dependency to keep the build lean.

## Architecture

Kuro is written from scratch; no code is copied from other projects.
Concepts studied from
[NewBlackbox](https://github.com/ALEX5402/NewBlackbox) (virtual-engine idea,
AIDL service virtualization, Dobby native hooks, ABI splits) informed the
layering, but every line here is original:

- `:core` (`com.kuro.core`) — pure-Kotlin engine, no JNI/NDK:
  `VirtualCore` (init, vault dirs, ABI info), `AbiGate` (APK `lib/` scan),
  `VirtualPackage` (installed + archive parsing across API 29-35),
  `VirtualStorage` (per-app dirs), `VirtualLauncher` (isolated intents),
  `IsolationPolicy` (flags and roadmap).
- `:app` (`com.kuro.virtual`) — Compose UI + `VaultRepository` (installed
  query, APK import) + `VaultStore` (SharedPreferences persistence).

Phase 1 runs managed shortcuts and managed APK installs with isolated task
stacks. Install-free execution (full AMS/PMS Binder virtualization + native
I/O hooks) is an explicit Phase 2 roadmap item in `IsolationPolicy`; the
interfaces are ready but not claimed as done.

## Requirements

- Android 10 (API 29) or later, 64-bit (`arm64-v8a`) recommended.
- No root.

## Install

1. Open the latest successful **Build Kuro APKs** workflow run.
2. Download `kuro-debug` or `kuro-release` artifact, install the APK.
3. Open Kuro, tap `+`, add an installed app or pick an APK file, tap an entry
   to launch it in isolation.

## Build (CI only)

Pushes to `main` build automatically. Manual equivalent:

```sh
gradle :app:assembleDebug :app:assembleRelease
```

APKs land in `app/build/outputs/apk/<debug|release>/`.
