CREATE TABLE IF NOT EXISTS users (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  phone text UNIQUE NOT NULL,
  name text NOT NULL DEFAULT '',
  created_at timestamptz NOT NULL DEFAULT now()
);
CREATE TABLE IF NOT EXISTS addresses (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  name text NOT NULL DEFAULT '',
  phone text NOT NULL DEFAULT '',
  house text NOT NULL, street text NOT NULL, landmark text NOT NULL DEFAULT '',
  city text NOT NULL DEFAULT 'Bengaluru', state text NOT NULL DEFAULT 'Karnataka',
  type text NOT NULL DEFAULT 'Home', pincode text NOT NULL DEFAULT '',
  is_default boolean NOT NULL DEFAULT false,
  lat double precision, lng double precision,
  created_at timestamptz NOT NULL DEFAULT now()
);
CREATE TABLE IF NOT EXISTS drivers (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  phone text UNIQUE NOT NULL,
  name text NOT NULL DEFAULT '',
  vehicle_number text NOT NULL DEFAULT 'KA-01-EA-2026',
  service_area text NOT NULL DEFAULT 'Bengaluru',
  status text NOT NULL DEFAULT 'online',
  current_lat double precision DEFAULT 12.9141,
  current_lng double precision DEFAULT 77.6412,
  created_at timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS bookings (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  service text NOT NULL, date text NOT NULL, slot text NOT NULL,
  estimated_kg integer NOT NULL, amount numeric(10,2) NOT NULL,
  status text NOT NULL DEFAULT 'Scheduled', paid boolean NOT NULL DEFAULT false,
  address_text text NOT NULL DEFAULT '',
  address_id uuid,
  coupon_code text NOT NULL DEFAULT '',
  discount_amount numeric(10,2) NOT NULL DEFAULT 0.00,
  razorpay_order_id text, razorpay_payment_id text,
  driver_id uuid REFERENCES drivers(id),
  pickup_otp text NOT NULL DEFAULT '1234',
  actual_kg integer,
  completed_at timestamptz,
  created_at timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS bookings_user_idx ON bookings(user_id, created_at DESC);
CREATE INDEX IF NOT EXISTS bookings_order_idx ON bookings(razorpay_order_id);
CREATE INDEX IF NOT EXISTS bookings_driver_idx ON bookings(driver_id);

CREATE TABLE IF NOT EXISTS offers (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  code text UNIQUE NOT NULL,
  title text NOT NULL,
  description text NOT NULL DEFAULT '',
  discount_type text NOT NULL DEFAULT 'percentage',
  discount_value numeric(10,2) NOT NULL,
  min_order_value numeric(10,2) NOT NULL DEFAULT 0.00,
  max_discount numeric(10,2) NOT NULL DEFAULT 500.00,
  valid_till text NOT NULL DEFAULT '31 Dec 2026',
  is_active boolean NOT NULL DEFAULT true,
  created_at timestamptz NOT NULL DEFAULT now()
);

INSERT INTO offers(code, title, description, discount_type, discount_value, min_order_value, valid_till) VALUES
 ('WELCOME10', '10% OFF First Pickup', 'Get 10% off on your first pickup', 'percentage', 10, 0, '31 Dec 2026'),
 ('RECYCLE20', '₹20 OFF on ₹100+', 'Get ₹20 off on orders above ₹100', 'fixed', 20, 100, '30 Nov 2026'),
 ('FREEDISPOSAL', 'Free Pickup Discount', 'Free pickup on orders above ₹200', 'fixed', 49, 200, '31 Dec 2026'),
 ('GREEN15', '15% OFF E-Waste', 'Get 15% off on E-Waste disposal', 'percentage', 15, 0, '15 Nov 2026')
ON CONFLICT DO NOTHING;

-- Areas you serve. Add rows to open a new area (keyword matches pincode exactly or area name by substring).
CREATE TABLE IF NOT EXISTS service_areas (keyword text PRIMARY KEY);
INSERT INTO service_areas(keyword) VALUES
 ('hsr'),('koramangala'),('indiranagar'),('btm'),('jayanagar'),('whitefield'),
 ('560102'),('560034'),('560038'),('560068'),('560011'),('560066')
ON CONFLICT DO NOTHING;
