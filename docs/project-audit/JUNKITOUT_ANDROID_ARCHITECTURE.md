# JunkItOut Android Application — System Architecture Document

## 1. System Architecture Overview

The **JunkItOut Platform** is structured as a multi-module Android application paired with a centralized Node.js Express cloud backend hosted on Render (`https://junkitout-android.onrender.com`) and backed by a PostgreSQL Supabase database (with `pg-mem` in-memory fallback).

```
                      ┌─────────────────────────────────┐
                      │    CUSTOMER APP (:app)          │
                      │  Package: com.app.junkitout     │
                      │  - Pickup Booking & Coupons     │
                      │  - Online Payments (Razorpay)   │
                      │  - Handshake OTP & Live Track   │
                      └────────────────┬────────────────┘
                                       │ REST API (HTTPS)
                                       ▼
                      ┌─────────────────────────────────┐
                      │     SHARED BACKEND SERVER       │
                      │   (Node.js + PostgreSQL)        │
                      │  - JWT Auth & Identity          │
                      │  - Order Dispatch Engine        │
                      │  - 30-Day Auto Retention        │
                      └────────────────┬────────────────┘
                                       │ REST API (HTTPS)
                                       ▼
                      ┌─────────────────────────────────┐
                      │    DRIVER PARTNER APP (:driver) │
                      │  Package: com.junkitout.driver  │
                      │  - Online/Offline Duty Toggle   │
                      │  - Google Maps Navigation       │
                      │  - Doorstep Customer OTP Verify │
                      │  - Weight Entry & Completion    │
                      └─────────────────────────────────┘
```

---

## 2. Module Specifications

### A. Customer App Module (`:app`)
- **Package / Application ID**: `com.app.junkitout`
- **Compile SDK / Target SDK**: API Level 36 (Android 15)
- **Minimum SDK**: API Level 24 (Android 7.0)
- **Version Code / Name**: `16` / `"2.1"`
- **Signing Config**: Signed with production release key (`app/release-key.jks`, alias `key0`, SHA1 `95:1C:8F:C1:02:8F:91:A6:94:37:31:28:27:E0:2E:22:94:12:75:F6`).

### B. Driver Partner App Module (`:driver`)
- **Package / Application ID**: `com.junkitout.driver`
- **Compile SDK / Target SDK**: API Level 36 (Android 15)
- **Minimum SDK**: API Level 24 (Android 7.0)
- **Version Code / Name**: `22` / `"3.2"`
- **Signing Config**: Signed with production release key (`driver/release-key.jks`, alias `key0`).

---

## 3. Technology Stack & Key Dependencies

- **Programming Language**: 100% Idiomatic Kotlin 1.9.24.
- **UI Framework**: Jetpack Compose (Material 3, Compose BOM `2024.06.00`).
- **Async & Threading**: Kotlin Coroutines (`kotlinx-coroutines-android:1.8.1`).
- **Image Loading**: Coil Compose `io.coil-kt:coil-compose:2.6.0`.
- **Navigation**: Jetpack Compose Navigation `androidx.navigation:navigation-compose:2.7.7`.
- **Location & Maps**: Google Maps Compose `4.4.1`, Play Services Maps `18.2.0`, Android Geocoder & LocationManager.
- **Payments**: Razorpay Android Checkout SDK `com.razorpay:checkout:1.6.40`.

---

## 4. Networking & Resilience Architecture

- **HTTP Client**: Native `HttpURLConnection` wrapped inside Kotlin Coroutine `withContext(Dispatchers.IO)`.
- **Timeout Management**:
  - `connectTimeout`: 60,000 ms (60s)
  - `readTimeout`: 60,000 ms (60s)
- **Automatic Retry Policy**: All network calls enforce an automatic 3-retry attempt loop with a 2-second delay on socket timeouts to handle Render server cold starts seamlessly.
- **Self-Keep-Alive Engine**: Backend executes an internal `/health` ping every 4 minutes to maintain 0ms cold start latency.

---

## 5. Security & Data Isolation

1. **Client-Side Safety**: No private database credentials, JWT secrets, or SMS API keys exist in APK/AAB files.
2. **Authentication**: JWT tokens (`Bearer <token>`) issued by backend upon OTP verification, stored securely in SharedPreferences.
3. **Data Retention Policy**:
   - Customer Profiles (`users`) & Saved Addresses (`addresses`): Retained **permanently**.
   - Bookings & Transactions (`bookings`, `wallet_transactions`): Automatically purged after **30 days** via background server job.
