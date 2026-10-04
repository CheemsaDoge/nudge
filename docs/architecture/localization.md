# Android app languages

Settings → Language offers System default, English, 简体中文, 日本語 and Français.
All four language packs are bundled in APKs and AABs, so switching works offline.
Chrome extension strings are outside this change.

## Resource ownership

Android UI text is stored in `values/strings.xml`, with complete counterparts in
`values-b+zh+Hans`, `values-ja` and `values-fr`. Native language names deliberately
use `translatable="false"` so someone can recover from selecting an unfamiliar language.
Android OS dialogs follow the OS's language. App names, website addresses, QR data,
user-created rules/groups and custom block messages retain their original contents.

Activities use AppCompat and `setApplicationLocales`. Android 13+ uses the platform
per-app locale API; older supported Android versions use AppCompat's persisted locales.
Switching recreates the Activity automatically. No manual process restart, separate
translated screens or per-page environment-variable checks are needed.

Compose reads resources from the current Activity configuration. Non-Activity surfaces
(widgets, notifications and native overlays) use `ContextCompat.getContextForLanguage`.
An Activity configuration signal refreshes widgets and notification copy when language
changes, without a polling loop. Notification copy still goes through the existing
change-only post gate.

Pure domain calculators and import builders retain their Android-free interface.
Presentation adapters in `ui/localization` localize built-in model labels, durations,
rule summaries and diagnostics. Do not apply these adapters to user data or app names.
Import summaries use quantity resources for rule/group/history counts. JSON field names,
values and platform parser details are preserved in diagnostics.

Default block message pools are resource arrays. A nonblank custom pool is displayed
verbatim; empty custom text selects defaults in the current app language.

## Verification and maintenance

Run `./gradlew test lintDebug assembleDebug`. `TranslationResourcesTest` checks exact
resource-key coverage, argument indexes/types, array/plural counts, the supported locale
list and bundled AAB languages. Add every new translatable resource to all three locales.
Use quantity resources when grammar depends on a count; keep format arguments indexed.

`LanguageSwitchTest` operates the actual Settings screen on Android 8 (API 26) and
Android 13 (API 33), chooses all four languages, verifies recreation and non-Activity
resource lookup, and returns to System default. CI uploads emulator screenshots and
reports alongside the debug APK. This checks both sides of the platform locale API.
