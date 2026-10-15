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
CREATE TABLE IF NOT EXISTS bookings (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  service text NOT NULL, date text NOT NULL, slot text NOT NULL,
  estimated_kg integer NOT NULL, amount numeric(10,2) NOT NULL,
  status text NOT NULL DEFAULT 'Scheduled', paid boolean NOT NULL DEFAULT false,
  address_text text NOT NULL DEFAULT '',
  address_id uuid,
  razorpay_order_id text, razorpay_payment_id text,
  created_at timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS bookings_user_idx ON bookings(user_id, created_at DESC);
CREATE INDEX IF NOT EXISTS bookings_order_idx ON bookings(razorpay_order_id);
-- Areas you serve. Add rows to open a new area (keyword matches pincode exactly or area name by substring).
CREATE TABLE IF NOT EXISTS service_areas (keyword text PRIMARY KEY);
INSERT INTO service_areas(keyword) VALUES
 ('hsr'),('koramangala'),('indiranagar'),('btm'),('jayanagar'),('whitefield'),
 ('560102'),('560034'),('560038'),('560068'),('560011'),('560066')
ON CONFLICT DO NOTHING;
