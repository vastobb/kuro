# Kuro (黒)

Kuro (`dev.kuro.reu`) is an isolated app vault for modern Android. Vault
entries open from Kuro's own console — bottom navigation, sections for
clones vs device copies, and an action sheet per app that always says which
instance will open before anything launches. Nothing opens an original app
by surprise: entries without a clone offer to make one first.

Public repo: https://github.com/vastobb/kuro

## Features

- Add apps from installed apps (Clone lab, search, one-tap add).
- Import APK files (system file picker, copy into vault, manifest parse).
- 64-bit ABI gate: native `lib/` entries are checked against
  `Build.SUPPORTED_ABIS`; incompatible APKs are rejected with a reason.
- Clone-first vault: every entry shows Clone or Device. Tapping opens an
  action sheet — Open clone, Make clone, Install from vault, Open device
  copy (explicit), Clear vault data, Remove, Uninstall clone.
- Real clones via managed work profile: separate OS-level instances with
  separate data, launched cross-profile from Kuro. Kuro's staged clone APKs
  live in its own vault directory.
- Install from vault: APK entries whose package is missing get a one-tap
  user-confirmed system install, then launch from Kuro.
- Crash-safe: a crash in a launched app cannot take down Kuro (separate
  processes, result-checked launches).
- Clean original console UI: bottom navigation (Vault / Clone lab /
  Settings), sectioned vault, per-app action sheets, white + black +
  vermilion red, Material 3, dark mode follows the system. Written from
  scratch — not a BlackBox-style layout, no AI-gradient styling.

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

## Clones vs device copies

Tapping a vault entry never launches anything directly — it opens that app's
action sheet, which states the instance type up front. Clones are separate
OS-level instances with separate data (same approach as Island/Shelter,
public APIs only, no root):

1. Open Settings → Work profile clones → Set up. The system creates the
   profile with Kuro as its manager (one-time, user-confirmed).
2. Open the app's sheet in the Vault → Make clone. Kuro stages its APKs in
   its own vault directory and hands them to the profile-side installer;
   confirm once in the work context.
3. The entry shows a Clone badge and its primary button becomes Open clone.

APK entries whose package is not installed get Install from vault instead:
Kuro installs the vault APK through a user-confirmed system session, then
launches it. Clones can be removed from their sheet without touching the
device copy. Some OEMs restrict work-profile provisioning; Settings reports
when setup is unavailable.

Why not run APKs straight from Kuro's directory with no install at all?
That is in-process virtualization (a native hook engine over hidden
per-version Android APIs). It cannot be written from scratch without that
engine's code, and manifest-only repackaging without `classes.dex`
rewriting installs broken apps — so Kuro does not pretend to do either.

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
