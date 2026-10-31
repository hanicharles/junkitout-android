# JunkItOut Android Application — Complete Feature Inventory

## Feature Status Summary

| Feature Name | App / Role | Entry Point / Screen | Backend Endpoint | Status | Evidence |
|---|---|---|---|---|---|
| **Customer Phone OTP Login** | Customer | `LoginScreen` / `OtpScreen` | `POST /api/auth/send-otp`, `/api/auth/verify-otp` | **IMPLEMENTED AND VERIFIED** | Test OTP `123456` or SMS OTP; auto-restores name & default address. |
| **Profile & Address Auto-Restore** | Customer | On OTP Login | `POST /api/auth/verify-otp` | **IMPLEMENTED AND VERIFIED** | Default saved address & name returned in login payload. |
| **Address Manager** | Customer | `AddressManagerScreen` | `GET/POST/DELETE /api/addresses` | **IMPLEMENTED AND VERIFIED** | CRUD for Home, Work, Other; default flag support. |
| **Live GPS Location Auto-Save** | Customer | "Use Current Location" | `Geo.currentFix` + `POST /api/addresses` | **IMPLEMENTED AND VERIFIED** | One-tap GPS reverse geocoding & auto-save to database. |
| **2x2 Services Grid** | Customer | `HomeTab` | Client RATES mapping | **IMPLEMENTED AND VERIFIED** | High-res category photos (`img_ewaste`, `img_household_dry`, `img_paper_waste`, `img_plastic_waste`). |
| **Auto-Scrolling Hero Carousel** | Customer | `HomeTab` | Client State | **IMPLEMENTED AND VERIFIED** | 4-banner auto-advancing carousel (5s timer + swipeable). |
| **Offers & Coupons System** | Customer | `OffersScreen` / `BookingScreen` | `GET /api/offers`, `POST /api/coupons/validate` | **IMPLEMENTED AND VERIFIED** | Active coupons (`WELCOME10`, `RECYCLE20`, `FREEDISPOSAL`, `GREEN15`) applied at checkout. |
| **Razorpay Online Payments** | Customer | `BookingScreen` | `POST /api/payments/create-order`, `/verify` | **IMPLEMENTED AND VERIFIED** | Razorpay SDK UPI/Card intent flow; HMAC signature verification. |
| **Pickup Verification OTP** | Customer & Driver | Order Card / Driver Dialog | `POST /api/driver/pickups/:id/verify-otp` | **IMPLEMENTED AND VERIFIED** | 4-digit OTP generated on booking; verified by collector at doorstep. |
| **Live Collector Tracking** | Customer | Orders Tab | `GET /api/bookings/:id/tracking` | **IMPLEMENTED AND VERIFIED** | Shows assigned collector name, vehicle number, phone, and GPS. |
| **Notification Center** | Customer | Home Bell Icon | Local App State | **IMPLEMENTED AND VERIFIED** | Red dot badge for unread notifications; Mark All as Read capability. |
| **Transaction History** | Customer | `TransactionsScreen` | `GET /api/wallet/transactions` | **IMPLEMENTED AND VERIFIED** | Displays real booking payments and refund logs from database. |
| **Edit Profile** | Customer | `EditProfileScreen` | `PUT /api/me` | **IMPLEMENTED AND VERIFIED** | Updates Full Name, Email ID, and Profile Photo (gallery picker). |
| **App Settings** | Customer | `AppSettingsScreen` | Local Preferences | **IMPLEMENTED AND VERIFIED** | Mute Notifications and Disable Location Access toggles. |
| **About Us** | Customer | `AboutUsScreen` | Static Content | **IMPLEMENTED AND VERIFIED** | Full company overview, zero-landfill statistics, and support email. |
| **Support Chatbot** | Customer | `SupportChatScreen` | Client FAQ Matcher | **IMPLEMENTED AND VERIFIED** | Automated FAQ answering + support email fallback (`support@junkitout.in`). |
| **Driver Partner Login** | Driver | `DriverLoginScreen` | `POST /api/driver/auth/login` | **IMPLEMENTED AND VERIFIED** | 2-step OTP login for collection executives. |
| **Driver Duty Switch** | Driver | `DriverDutyTab` | Client / Backend State | **IMPLEMENTED AND VERIFIED** | Online/Offline status switch banner. |
| **Driver Pickups Feed** | Driver | `DriverDutyTab` | `GET /api/driver/pickups` | **IMPLEMENTED AND VERIFIED** | Browse and claim unassigned pickup requests. |
| **Driver Maps Navigation** | Driver | Pickup Card | Google Maps Intent | **IMPLEMENTED AND VERIFIED** | One-tap turn-by-turn navigation to customer doorstep address. |
| **Driver Customer OTP Verification** | Driver | Pickup Card Dialog | `POST /api/driver/pickups/:id/verify-otp` | **IMPLEMENTED AND VERIFIED** | Prompts driver for customer's 4-digit Pickup OTP. |
| **Driver Weight Entry & Completion** | Driver | Pickup Card Dialog | `POST /api/driver/pickups/:id/complete` | **IMPLEMENTED AND VERIFIED** | Input actual collected weight in kg and mark trip completed. |
| **Driver Earnings & Trips Log** | Driver | `DriverEarningsTab` / `DriverTripHistoryTab` | `GET /api/driver/earnings` | **IMPLEMENTED AND VERIFIED** | Daily payout, completed trip count, and total kg collected. |
| **Automated 30-Day Cleanup** | Server | Background Job | `DELETE FROM bookings`, `wallet_transactions` | **IMPLEMENTED AND VERIFIED** | Purges orders/transactions older than 30 days while keeping profiles and addresses. |
