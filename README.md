# KURO — Premium Android Virtualization Workspace

> Elegant. Fast. Premium. Minimal. Confident.

Kuro is a premium Android virtualization workspace where users import APKs or
installed apps and run them inside isolated virtual environments.

This milestone implements the **complete UI/UX foundation** before any
virtualization logic:

1. Design System (color / type / shape / spacing / motion)
2. Theme Engine (AMOLED dark-first + separately-designed light + dynamic color)
3. Navigation (Navigation Compose + 5-tab bottom bar with elevated Import)
4. Reusable Components (cards, buttons, chips, sheets, skeletons, meters, stepper)
5. All Screen Layouts (Splash / Home / Vault / Import / Import Flow / Activity / Detail / Settings)
6. Animation System (spring, 120/250/400ms, shimmer — no spinners)
7. Dark / Light Themes + accessibility hooks

## Design language

- Dark-first, AMOLED `#0B0D13`, Material 3 Expressive, Android 16 inspired
- Corners: small 12 / cards 20 / dialogs 24 / sheets 28 top / buttons 18 / hero 28
- Primary `#7C4DFF` · Container `#9A7DFF` · Secondary `#00D4FF` · Accent `#33E1B5`
- Type: Display 32 Bold / Headline 24 SemiBold / Title 18 SemiBold / Body 14 / Caption 12
- Motion: fast 120ms / normal 250ms / premium 400ms, spring-first, 60fps+
- Glow: purple + cyan only. No random gradients, no glassmorphism abuse.

## Project

```
app/src/main/java/dev/kuro/app/
  MainActivity.kt / KuroApp.kt
  data/Models.kt            # VirtualApp / RunningEnv / ImportUiState + PreviewData
  ui/theme/                 # Color / Type / Shape / Spacing / Motion / Theme
  ui/components/            # Card / Button / Chips / BottomBar / Sheet / Empty / Skeleton / Charts / Stepper / AppCard / Search
  ui/navigation/            # Routes / NavGraph
  ui/screens/               # Splash / Home / Vault / Import / ImportFlow / Activity / AppDetail / Settings
  util/Haptics.kt
```

## Build (cloud only — do not build locally)

Per repo policy: **no local Gradle / NDK builds**. Push to `main`, GitHub Actions builds.

- Workflow: `.github/workflows/android.yml`
- Runner uses preinstalled Android SDK, installs `platforms;android-34` + `build-tools;34.0.0`
- `./gradlew :app:assembleDebug` → artifact `kuro-debug`
- `./gradlew :app:testDebugUnitTest`

```bash
git push origin main
# Actions → Kuro UI Build → download kuro-debug APK
```

## Screens

| Route | Screen |
|---|---|
| `splash` | Black bg, logo fade, purple pulse, cyan particles, ≤1.2s → Home |
| `home` | Greeting hero, workspace status, memory card, Recent / Running / Pinned / Recommended |
| `vault` | All apps, chips (Running/Paused/Updated/Games/Tools/Social), animated search, realtime sort |
| `import` | Hero + 2 floating cards (APK / Installed) with press elevation |
| `import_flow` | 5-step modern stepper: Source → Analyze → Configure → Import → Ready |
| `activity` | CPU/mem/storage meters, animated charts, live pulse |
| `app_detail/{id}` | Header, badges, storage graph, permissions, runtime, Launch/Clone/Settings |
| `settings` | Grouped cards: Appearance / Virtualization / Performance / Storage / Security / Developer / Backup / About |

Every action opens a premium bottom sheet (drag/swipe/spring). Empty states have
illustration + headline + description + action. Taps scale 0.97 + haptic.

## Release build (signed)

`release.yml` runs `assembleRelease`, signs with repository secrets, verifies
with `apksigner` + `aapt2`, and uploads **`kuro-release.apk`**.
Emulator testing (`launch-audit.yml`) also uses the signed release APK.

Create the secrets once (keystore stays on your machine):

```bash
keytool -genkeypair -keystore kuro-release.jks -alias kuro \
  -keyalg RSA -keysize 2048 -validity 10950
base64 -w0 kuro-release.jks > kuro-release.jks.b64  # macOS: base64 -i kuro-release.jks
gh secret set ANDROID_KEYSTORE_BASE64 < kuro-release.jks.b64
gh secret set ANDROID_KEYSTORE_PASSWORD   # prompts, hidden
gh secret set ANDROID_KEY_ALIAS           # e.g. kuro
gh secret set ANDROID_KEY_PASSWORD        # prompts, hidden
```

## Next (engine, not this milestone)

- Virtualization runtime, work-profile clones, APK installer, activity monitor data
- Lottie success/particle assets, Coil icon loading from PackageManager
- Tablet / foldable / landscape adaptive layouts, large-font + screen-reader pass
