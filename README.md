# YAKU
Yet Another Kiosk Unlocker is a LSPosed module that allows you to break freely from the kiosk exam screen lock

---

## Purpose
YAKU allows the owner of a device to bypass the vendor-imposed kiosk / lock-down mode of the "com.cbt.exam.browser" application without triggering the internal violation counter.

Navigation gestures, the Recents menu and the system notification shade remain fully functional while the examination client is active.

---

## Features
- Prevents the target application from entering Android lock-task mode.
- Suppresses TRIM_MEMORY_UI_HIDDEN to disable the violation logger.
- Maintains synthetic window focus to avoid secondary pause-based checks.
- Optional one-line activation toast for confirmation.
- Pure Java implementation, no Kotlin runtime overhead.
- Hooks are restricted to the examination process; zero system-wide impact.

---

## Installation
1. Build the APK
```bash
$ ./gradlew assembleDebug
```
2. Install on the device
```bash
$ adb install -r app/build/outputs/apk/debug/app-debug.apk
```
3. Open LSPosed Manager → Modules → YAKU → enable.
4. Select only com.cbt.exam.browser in the module scope.
5. Reboot once.
