# Junk-It-Out (Android – Kotlin + Jetpack Compose)

Native Android implementation of the 12-screen Junk-It-Out design:
Splash → Login → OTP → Location → Checking → Available / Not Available → Confirm → Add Address → Saved → Home → Menu.

## Open in Android Studio
1. Unzip, then **File ▸ Open** and select the `JunkItOut` folder.
2. Wait for Gradle sync (needs internet the first time; Android Studio Koala/Ladybug or newer, JDK 17).
3. Pick an emulator or phone and press **Run ▶**.
4. APK: **Build ▸ Build Bundle(s) / APK(s) ▸ Build APK(s)**.

## Run it
1. **Backend** (Node 18+): `cd backend && npm install && npm start`. On first boot it creates the tables in Supabase and seeds `service_areas`. Check `http://localhost:3000/health`.
2. **App**: open `JunkItOut` in Android Studio, sync, run on an emulator (or a phone with `API_BASE_URL` set to your PC's LAN IP; phone and PC on the same Wi-Fi).
3. Razorpay webhook (optional but recommended): expose the backend over HTTPS and add `https://<host>/api/payments/webhook` in the Razorpay dashboard with the same secret as `RAZORPAY_WEBHOOK_SECRET`, event `payment.captured`.
4. Add serviced areas by inserting rows into `service_areas` (pincode or area-name keyword).

---
## Configuration (what the app needs)
`local.properties` is already filled in; format is in `local.properties.example` (git-ignored) and set **client-safe** values only:

| Key | Used for | Notes |
|---|---|---|
| `API_BASE_URL` | Backend (OTP, serviceability, addresses, bookings, payments) | Emulator → `http://10.0.2.2:3000`. |
| `RAZORPAY_KEY_ID` | Razorpay Checkout | Public key id only. Use `rzp_test_...` while developing. |
| `GOOGLE_MAPS_API_KEY` | Map on Confirm screen + Geocoding | Restrict to package `com.junkitout.app` + SHA-1. |

**Never put in the app:** `RAZORPAY_KEY_SECRET`, `RAZORPAY_WEBHOOK_SECRET`, `JWT_SECRET`, `DATABASE_URL`, `FAST2SMS_*`. They belong in the backend `.env` only.

## Backend contract (edit paths in `data/Api.kt` if yours differ)
- `POST /api/auth/send-otp {phone}` → 200
- `POST /api/auth/verify-otp {phone, otp}` → `{token, user:{name}}`
- `GET /api/serviceability?pincode=&lat=&lng=` → `{serviceable:true|false}`
- `POST /api/addresses`, `GET /api/addresses` → `{addresses:[...]}` (Bearer token)
- `POST /api/bookings`, `GET /api/bookings` → `{booking}` / `{bookings:[...]}`
- `POST /api/payments/create-order {bookingId}` → `{orderId, amount(paise)}` (server uses Razorpay secret)
- `POST /api/payments/verify {bookingId, razorpay_order_id, razorpay_payment_id, razorpay_signature}`
