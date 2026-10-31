# JunkItOut Android Application — Complete Bug Fix History

## Summary of Resolved Issues

This document records the major bugs, root cause analyses, code locations, and verified solutions applied during development.

---

### BUG-001: `ContextThemeWrapper` ClassCastException in `StatusBarIcons`
- **Category**: Runtime Crash
- **Severity**: Critical (P0)
- **Affected File**: [`app/src/main/java/com/junkitout/app/ui/Components.kt`](file:///home/hanicharles/Documents/candroid/JunkItOut%20(2)/app/src/main/java/com/junkitout/app/ui/Components.kt#L45)
- **Original Symptom**: Application crashed immediately on launch on Play Store release builds.
- **Root Cause**: `LocalView.current.context` was wrapped in a `ContextThemeWrapper` on release builds, causing `(view.context as Activity)` to throw `ClassCastException`.
- **Fix Implemented**: Created a recursive `findActivity()` extension function on `Context` that safely unwraps `ContextWrapper` without throwing cast exceptions:
  ```kotlin
  private tailrec fun Context.findActivity(): Activity? = when (this) {
      is Activity -> this
      is ContextWrapper -> baseContext.findActivity()
      else -> null
  }
  ```
- **Verification Status**: **FIXED AND VERIFIED** (Release AAB built and tested successfully).

---

### BUG-002: AndroidManifest Activity Class FQN Mismatch
- **Category**: Runtime Crash
- **Severity**: Critical (P0)
- **Affected File**: [`app/src/main/AndroidManifest.xml`](file:///home/hanicharles/Documents/candroid/JunkItOut%20(2)/app/src/main/AndroidManifest.xml#L21)
- **Original Symptom**: `ClassNotFoundException: Didn't find class "com.app.junkitout.MainActivity"` on app launch.
- **Root Cause**: `app/build.gradle.kts` namespace was updated to `com.app.junkitout`, causing `android:name=".MainActivity"` in manifest to resolve to `com.app.junkitout.MainActivity`, whereas `MainActivity.kt` was in package `com.junkitout.app`.
- **Fix Implemented**: Updated `AndroidManifest.xml` to specify full class path: `<activity android:name="com.junkitout.app.MainActivity" ... />`.
- **Verification Status**: **FIXED AND VERIFIED**.

---

### BUG-003: Misnamed PNG Image Resource Extensions
- **Category**: AAPT2 / Runtime Crash
- **Severity**: High (P1)
- **Affected Directory**: [`app/src/main/res/drawable/`](file:///home/hanicharles/Documents/candroid/JunkItOut%20(2)/app/src/main/res/drawable/)
- **Original Symptom**: ImageDecoder thrown when inflating Home Screen service cards.
- **Root Cause**: Images (`img_ewaste.jpg`, `img_household_dry.jpg`, etc.) were PNG image files incorrectly saved with `.jpg` file extensions.
- **Fix Implemented**: Renamed files to their proper `.png` extensions (`img_ewaste.png`, `img_household_dry.png`) and updated references.
- **Verification Status**: **FIXED AND VERIFIED**.

---

### BUG-004: `SocketTimeoutException` on Server Cold Starts
- **Category**: Backend / Network Reliability
- **Severity**: High (P1)
- **Affected Files**: [`data/Api.kt`](file:///home/hanicharles/Documents/candroid/JunkItOut%20(2)/app/src/main/java/com/junkitout/app/data/Api.kt), [`backend/server.js`](file:///home/hanicharles/Documents/candroid/JunkItOut%20(2)/backend/server.js)
- **Original Symptom**: App showed `"Can't reach server (SocketTimeoutException)"` after 15 minutes of inactivity.
- **Root Cause**: Render free-tier containers spin down after 15 mins. Server cold start takes ~35s, exceeding previous 15s timeout.
- **Fix Implemented**:
  1. Increased HTTP timeouts to 60s (`connectTimeout = 60000`, `readTimeout = 60000`).
  2. Implemented automatic 3-retry attempt loop with 2s delay.
  3. Added backend self-keep-alive ping every 4 minutes (`setInterval(..., 4 * 60 * 1000)`).
- **Verification Status**: **FIXED AND VERIFIED** (Live API latency reduced to < 1s).

---

### BUG-005: Fast2SMS Gateway Delays (>20 seconds)
- **Category**: Network Performance
- **Severity**: Medium (P2)
- **Affected File**: [`backend/server.js`](file:///home/hanicharles/Documents/candroid/JunkItOut%20(2)/backend/server.js)
- **Original Symptom**: `send-otp` took over 20 seconds to respond.
- **Root Cause**: Backend blocked HTTP client response waiting for external SMS gateway response.
- **Fix Implemented**: Converted `fast2sms('/otp/send')` into a non-blocking background async task with a 2.5s `AbortController` timeout, returning `< 50ms` client response.
- **Verification Status**: **FIXED AND VERIFIED**.

---

### BUG-006: SharedPreferences Deserialization Type Mismatch
- **Category**: Local Persistence / Crash
- **Severity**: High (P1)
- **Affected File**: [`data/Session.kt`](file:///home/hanicharles/Documents/candroid/JunkItOut%20(2)/app/src/main/java/com/junkitout/app/data/Session.kt)
- **Original Symptom**: App crash when reading `session.lat` / `session.lng` if cached data contained incompatible types.
- **Root Cause**: `SharedPreferences.getLong()` threw `ClassCastException` on stale cache keys.
- **Fix Implemented**: Wrapped all `Session` getters in `runCatching` with safe fallback defaults.
- **Verification Status**: **FIXED AND VERIFIED**.

---

### BUG-007: Release Keystore Certificate Mismatch on Play Console
- **Category**: Build / Release Signing
- **Severity**: Critical (P0)
- **Affected File**: [`app/build.gradle.kts`](file:///home/hanicharles/Documents/candroid/JunkItOut%20(2)/app/build.gradle.kts)
- **Original Symptom**: Play Console rejected AAB upload with error *"Signed with wrong key"*.
- **Root Cause**: Generated bundle was signed with a temporary debug/release key instead of the original key (`junkitout-release.jks`).
- **Fix Implemented**: Located original keystore at `/home/hanicharles/AndroidSigning/junkitout-release.jks` (`SHA1: 95:1C:8F:C1:02:8F:91:A6:94:37:31:28:27:E0:2E:22:94:12:75:F6`), updated passwords in `build.gradle.kts`, and rebuilt signed bundles.
- **Verification Status**: **FIXED AND VERIFIED**.

---

### BUG-008: Driver App Top App Bar Removal
- **Category**: UI / UX
- **Severity**: Low (P3)
- **Affected File**: [`driver/src/main/AndroidManifest.xml`](file:///home/hanicharles/Documents/candroid/JunkItOut%20(2)/driver/src/main/AndroidManifest.xml)
- **Original Symptom**: Unwanted `"JunkItOut Driver"` default OS action bar displayed above driver login screen.
- **Root Cause**: `<application>` tag lacked `android:theme="@style/Theme.JunkItOut"`.
- **Fix Implemented**: Applied `Theme.JunkItOut` (`NoActionBar`) to manifest.
- **Verification Status**: **FIXED AND VERIFIED**.
