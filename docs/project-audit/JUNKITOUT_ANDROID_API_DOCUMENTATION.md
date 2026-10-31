# JunkItOut Platform — Complete API Documentation

## Base Server Configuration
- **Production Base URL**: `https://junkitout-android.onrender.com`
- **Authentication**: JWT Bearer Token in `Authorization: Bearer <token>` header.
- **Content Type**: `application/json`

---

## 1. Customer API Endpoints

### `POST /api/auth/send-otp`
- **Purpose**: Initiates OTP dispatch for customer login.
- **Request Body**: `{"phone": "9591225555"}`
- **Response**: `{"ok": true}` (Non-blocking async dispatch; response time < 50ms).

### `POST /api/auth/verify-otp`
- **Purpose**: Verifies 6-digit OTP or test OTP (`123456`), returns JWT session token, user profile, and default saved address.
- **Request Body**: `{"phone": "9591225555", "otp": "123456"}`
- **Response**:
  ```json
  {
    "token": "eyJhbGciOi...",
    "user": {
      "id": "60ece380-4d8e-4d25-9a56-24df6466410c",
      "name": "Junk-It-Out User",
      "phone": "9591225555",
      "area": "Banahalli, Bengaluru",
      "pincode": "563137",
      "lat": 12.9141,
      "lng": 77.6412
    }
  }
  ```

### `GET /api/addresses`
- **Header**: `Authorization: Bearer <token>`
- **Response**: `{"addresses": [{"id": "...", "house": "...", "street": "...", "type": "Home", "isDefault": true}]}`

### `POST /api/addresses`
- **Header**: `Authorization: Bearer <token>`
- **Request Body**: `{"house": "123", "street": "HSR Layout", "city": "Bengaluru", "pincode": "560102", "type": "Home", "isDefault": true}`
- **Response**: `{"address": {...}}`

### `POST /api/bookings`
- **Header**: `Authorization: Bearer <token>`
- **Request Body**: `{"service": "E-Waste Disposal", "date": "2026-10-25", "slot": "9 AM - 12 PM", "estimatedKg": 15, "addressText": "123, HSR Layout", "couponCode": "WELCOME10", "discountAmount": 15.0}`
- **Response**: `{"booking": {"id": "...", "amount": 135.0, "paid": false, "pickupOtp": "5829"}}`

### `POST /api/payments/create-order`
- **Header**: `Authorization: Bearer <token>`
- **Request Body**: `{"bookingId": "<uuid>"}`
- **Response**: `{"orderId": "order_Px123...", "amount": 13500, "currency": "INR"}`

### `POST /api/payments/verify`
- **Header**: `Authorization: Bearer <token>`
- **Request Body**: `{"bookingId": "<uuid>", "razorpay_order_id": "order_...", "razorpay_payment_id": "pay_...", "razorpay_signature": "..."}`
- **Response**: `{"ok": true}`

### `GET /api/bookings/:id/tracking`
- **Header**: `Authorization: Bearer <token>`
- **Response**:
  ```json
  {
    "orderId": "<uuid>",
    "status": "En Route",
    "pickupOtp": "5829",
    "paid": true,
    "driver": {
      "name": "Collector Ramesh",
      "phone": "9591225555",
      "vehicleNumber": "KA-01-EA-2026",
      "lat": 12.9141,
      "lng": 77.6412
    }
  }
  ```

---

## 2. Driver Partner API Endpoints

### `POST /api/driver/auth/login`
- **Request Body**: `{"phone": "9591225555", "name": "Collector Ramesh", "vehicleNumber": "KA-01-EA-2026"}`
- **Response**:
  ```json
  {
    "token": "eyJhbG...",
    "driver": {
      "id": "<uuid>",
      "phone": "9591225555",
      "name": "Collector Ramesh",
      "vehicle_number": "KA-01-EA-2026",
      "status": "online"
    }
  }
  ```

### `GET /api/driver/pickups`
- **Header**: `Authorization: Bearer <driverToken>`
- **Response**: `{"pickups": [{"id": "...", "service": "E-Waste Disposal", "addressText": "...", "customerName": "...", "customerPhone": "...", "isAssignedToMe": true, "pickupOtp": "5829"}]}`

### `PUT /api/driver/pickups/:id/accept`
- **Header**: `Authorization: Bearer <driverToken>`
- **Response**: `{"ok": true, "status": "Accepted"}`

### `PUT /api/driver/pickups/:id/status`
- **Header**: `Authorization: Bearer <driverToken>`
- **Request Body**: `{"status": "En Route"}` (or `"Arrived"`)
- **Response**: `{"ok": true, "status": "En Route"}`

### `POST /api/driver/pickups/:id/verify-otp`
- **Header**: `Authorization: Bearer <driverToken>`
- **Request Body**: `{"otp": "5829"}`
- **Response**: `{"ok": true, "verified": true}`

### `POST /api/driver/pickups/:id/complete`
- **Header**: `Authorization: Bearer <driverToken>`
- **Request Body**: `{"actualKg": 18}`
- **Response**: `{"ok": true, "status": "Completed"}`

### `GET /api/driver/earnings`
- **Header**: `Authorization: Bearer <driverToken>`
- **Response**: `{"completedCount": 3, "totalKg": 42, "totalEarnings": 380.0}`
