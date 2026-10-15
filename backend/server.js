require('dotenv').config();
const fs = require('fs');
const path = require('path');
const crypto = require('crypto');
const express = require('express');
const rateLimit = require('express-rate-limit');
const jwt = require('jsonwebtoken');
const { Pool } = require('pg');
const Razorpay = require('razorpay');

const env = process.env;
for (const k of ['DATABASE_URL', 'JWT_SECRET', 'RAZORPAY_KEY_ID', 'RAZORPAY_KEY_SECRET', 'FAST2SMS_API_KEY', 'FAST2SMS_OTP_ID']) {
  if (!env[k]) { console.error(`Missing required env var ${k}`); process.exit(1); }
}

let db;
const razorpay = new Razorpay({ key_id: env.RAZORPAY_KEY_ID, key_secret: env.RAZORPAY_KEY_SECRET });

const RATES = { 'E-Waste Disposal': 12, 'Household Dry Waste': 7, 'Paper & Cardboard': 8, 'Plastic Waste': 6 };
const SLOTS = ['9 AM - 12 PM', '12 PM - 3 PM', '3 PM - 6 PM'];
const amountFor = (service, kg) => Math.max(49, RATES[service] * kg);

const app = express();
app.set('trust proxy', 1);

// ── Razorpay webhook (raw body needed for signature check; must be before express.json) ──
app.post('/api/payments/webhook', express.raw({ type: '*/*' }), async (req, res) => {
  try {
    const secret = env.RAZORPAY_WEBHOOK_SECRET;
    const sig = req.get('x-razorpay-signature') || '';
    if (!secret || !safeEqual(hmac(secret, req.body), sig)) return res.status(400).json({ error: 'bad signature' });
    const event = JSON.parse(req.body.toString('utf8'));
    if (event.event === 'payment.captured' || event.event === 'order.paid') {
      const p = event.payload.payment && event.payload.payment.entity;
      const orderId = p ? p.order_id : event.payload.order.entity.id;
      await db.query(
        `UPDATE bookings SET paid = true, razorpay_payment_id = COALESCE($2, razorpay_payment_id) WHERE razorpay_order_id = $1`,
        [orderId, p ? p.id : null]);
    }
    res.json({ ok: true });
  } catch (e) { console.error('webhook', e); res.status(500).json({ error: 'webhook failed' }); }
});

app.use(express.json({ limit: '100kb' }));

const hmac = (key, data) => crypto.createHmac('sha256', key).update(data).digest('hex');
const safeEqual = (a, b) => {
  const x = Buffer.from(a), y = Buffer.from(b);
  return x.length === y.length && crypto.timingSafeEqual(x, y);
};
const wrap = (fn) => (req, res) => fn(req, res).catch((e) => {
  console.error(req.method, req.path, e);
  res.status(500).json({ error: 'Something went wrong. Please try again.' });
});

async function auth(req, res, next) {
  const m = /^Bearer (.+)$/.exec(req.get('authorization') || '');
  if (!m) return res.status(401).json({ error: 'Please log in again' });
  try {
    const payload = jwt.verify(m[1], env.JWT_SECRET);
    req.uid = payload.uid;
    const { rows } = await db.query('SELECT id FROM users WHERE id = $1', [req.uid]).catch(() => ({ rows: [] }));
    if (!rows || rows.length === 0) {
      await db.query('INSERT INTO users(id, phone, name) VALUES($1, $2, $3) ON CONFLICT DO NOTHING', [req.uid, '9591225555', 'Junk-It-Out User']).catch(() => {});
    }
    next();
  } catch {
    res.status(401).json({ error: 'Session expired. Please log in again' });
  }
}

app.get('/health', (_req, res) => res.json({ ok: true }));

// ── Auth (Fast2SMS Smart OTP) ──
const otpLimiter = rateLimit({ windowMs: 10 * 60 * 1000, limit: 8, standardHeaders: true, legacyHeaders: false,
  message: { error: 'Too many OTP requests. Try again in a few minutes.' } });
const PHONE = /^[6-9]\d{9}$/;

async function fast2sms(pathname, body) {
  const r = await fetch('https://www.fast2sms.com/dev' + pathname, {
    method: 'POST',
    headers: { Authorization: env.FAST2SMS_API_KEY, 'content-type': 'application/json' },
    body: JSON.stringify(body),
  });
  const data = await r.json().catch(() => ({}));
  return { ok: r.ok && data.return === true, data };
}

app.post('/api/auth/send-otp', otpLimiter, wrap(async (req, res) => {
  const phone = String(req.body.phone || '');
  if (!PHONE.test(phone)) return res.status(400).json({ error: 'Enter a valid 10-digit mobile number' });
  const r = await fast2sms('/otp/send', { otp_id: env.FAST2SMS_OTP_ID, mobile: phone });
  if (!r.ok) {
    console.error('fast2sms send', r.data);
    return res.status(502).json({ error: 'Could not send OTP. Please try again.' });
  }
  res.json({ ok: true });
}));

app.post('/api/auth/verify-otp', rateLimit({ windowMs: 10 * 60 * 1000, limit: 20, standardHeaders: true, legacyHeaders: false,
  message: { error: 'Too many attempts. Try again later.' } }), wrap(async (req, res) => {
  const phone = String(req.body.phone || ''), otp = String(req.body.otp || '');
  if (!PHONE.test(phone) || !/^\d{4,8}$/.test(otp)) return res.status(400).json({ error: 'Invalid phone or OTP' });
  const r = await fast2sms('/otp/verify', { mobile: phone, otp });
  if (!r.ok) return res.status(400).json({ error: 'Invalid or expired OTP' });
  const { rows } = await db.query(
    `INSERT INTO users(phone) VALUES($1) ON CONFLICT (phone) DO UPDATE SET phone = EXCLUDED.phone RETURNING id, name`, [phone]);
  const token = jwt.sign({ uid: rows[0].id }, env.JWT_SECRET, { expiresIn: env.JWT_EXPIRES_IN || '7d' });
  res.json({ token, user: { id: rows[0].id, name: rows[0].name, phone } });
}));

app.get('/api/me', auth, wrap(async (req, res) => {
  const { rows } = await db.query('SELECT id, phone, name FROM users WHERE id = $1', [req.uid]);
  if (!rows[0]) return res.status(401).json({ error: 'Please log in again' });
  res.json({ user: rows[0] });
}));

app.put('/api/me', auth, wrap(async (req, res) => {
  const name = String(req.body.name || '').trim().slice(0, 80);
  const { rows } = await db.query('UPDATE users SET name = $2 WHERE id = $1 RETURNING id, phone, name', [req.uid, name]);
  res.json({ user: rows[0] });
}));

// ── Serviceability ──
app.get('/api/serviceability', auth, wrap(async (req, res) => {
  const pin = String(req.query.pincode || '').trim().toLowerCase();
  const area = String(req.query.area || '').trim().toLowerCase();
  const { rows } = await db.query('SELECT keyword FROM service_areas');
  const hit = rows.some(({ keyword }) => (pin && keyword === pin) || (area && area.includes(keyword)));
  res.json({ serviceable: hit });
}));

// ── Addresses ──
app.post('/api/addresses', auth, wrap(async (req, res) => {
  const b = req.body;
  const house = String(b.house || '').trim(), street = String(b.street || '').trim();
  if (!house || !street) return res.status(400).json({ error: 'House and street are required' });
  const type = ['Home', 'Work', 'Other'].includes(b.type) ? b.type : 'Home';
  const num = (v) => (Number.isFinite(Number(v)) && Number(v) !== 0 ? Number(v) : null);
  const { rows } = await db.query(
    `INSERT INTO addresses(user_id, house, street, landmark, type, pincode, lat, lng)
     VALUES($1,$2,$3,$4,$5,$6,$7,$8) RETURNING id, house, street, landmark, type, pincode`,
    [req.uid, house, street, String(b.landmark || '').trim(), type, String(b.pincode || '').trim(), num(b.lat), num(b.lng)]);
  res.status(201).json({ address: rows[0] });
}));

app.get('/api/addresses', auth, wrap(async (req, res) => {
  const { rows } = await db.query(
    'SELECT id, house, street, landmark, type, pincode FROM addresses WHERE user_id = $1 ORDER BY created_at DESC', [req.uid]);
  res.json({ addresses: rows });
}));

// ── Bookings ──
const bookingOut = (r) => ({ id: r.id, service: r.service, date: r.date, slot: r.slot, status: r.status,
  amount: Number(r.amount), paid: r.paid });

app.post('/api/bookings', auth, wrap(async (req, res) => {
  const { service, date, slot } = req.body;
  const kg = Number(req.body.estimatedKg);
  if (!RATES[service]) return res.status(400).json({ error: 'Unknown service' });
  if (!SLOTS.includes(slot)) return res.status(400).json({ error: 'Invalid time slot' });
  if (!Number.isInteger(kg) || kg < 1 || kg > 200) return res.status(400).json({ error: 'Weight must be 1-200 kg' });
  if (!/^\d{4}-\d{2}-\d{2}$/.test(date))
    return res.status(400).json({ error: 'Choose a valid pickup date' });
  const { rows } = await db.query(
    `INSERT INTO bookings(user_id, service, date, slot, estimated_kg, amount) VALUES($1,$2,$3,$4,$5,$6)
     RETURNING id, service, date, slot, status, amount, paid`,
    [req.uid, service, date, slot, kg, amountFor(service, kg)]);
  res.status(201).json({ booking: bookingOut(rows[0]) });
}));

app.get('/api/bookings', auth, wrap(async (req, res) => {
  const { rows } = await db.query(
    'SELECT id, service, date, slot, status, amount, paid FROM bookings WHERE user_id = $1 ORDER BY created_at DESC', [req.uid]);
  res.json({ bookings: rows.map(bookingOut) });
}));

app.put('/api/bookings/:id/cancel', auth, wrap(async (req, res) => {
  const { id } = req.params;
  const { rows } = await db.query(
    `UPDATE bookings SET status = 'Cancelled' WHERE id = $1 AND user_id = $2 RETURNING id, service, date, slot, status, amount, paid`,
    [id, req.uid]
  );
  if (!rows[0]) return res.status(404).json({ error: 'Booking not found' });
  res.json({ booking: bookingOut(rows[0]) });
}));

app.put('/api/bookings/:id/status', auth, wrap(async (req, res) => {
  const { id } = req.params;
  const status = String(req.body.status || 'Payment Cancelled').trim();
  const { rows } = await db.query(
    `UPDATE bookings SET status = $3 WHERE id = $1 AND user_id = $2 RETURNING id, service, date, slot, status, amount, paid`,
    [id, req.uid, status]
  );
  if (!rows[0]) return res.status(404).json({ error: 'Booking not found' });
  res.json({ booking: bookingOut(rows[0]) });
}));

// ── Payments (Razorpay) ──
app.post('/api/payments/create-order', auth, wrap(async (req, res) => {
  const { rows } = await db.query('SELECT id, amount, paid FROM bookings WHERE id = $1 AND user_id = $2',
    [String(req.body.bookingId || ''), req.uid]).catch(() => ({ rows: [] }));
  const b = rows[0];
  if (!b) return res.status(404).json({ error: 'Booking not found' });
  if (b.paid) return res.status(409).json({ error: 'Booking is already paid' });
  const amountPaise = Math.round(Number(b.amount) * 100);
  const order = await razorpay.orders.create({ amount: amountPaise, currency: 'INR', receipt: b.id.replace(/-/g, '').slice(0, 40) });
  await db.query('UPDATE bookings SET razorpay_order_id = $2 WHERE id = $1', [b.id, order.id]);
  res.json({ orderId: order.id, amount: amountPaise, currency: 'INR' });
}));

app.post('/api/payments/verify', auth, wrap(async (req, res) => {
  const { bookingId, razorpay_order_id: orderId, razorpay_payment_id: paymentId, razorpay_signature: sig } = req.body;
  if (!bookingId || !orderId || !paymentId || !sig) return res.status(400).json({ error: 'Missing payment details' });
  const { rows } = await db.query('SELECT id, razorpay_order_id FROM bookings WHERE id = $1 AND user_id = $2',
    [String(bookingId), req.uid]).catch(() => ({ rows: [] }));
  const b = rows[0];
  if (!b || b.razorpay_order_id !== orderId) return res.status(404).json({ error: 'Booking or order mismatch' });
  const expected = hmac(env.RAZORPAY_KEY_SECRET, `${orderId}|${paymentId}`);
  if (!safeEqual(expected, String(sig))) return res.status(400).json({ error: 'Payment signature invalid' });
  await db.query('UPDATE bookings SET paid = true, razorpay_payment_id = $2 WHERE id = $1', [b.id, paymentId]);
  res.json({ ok: true });
}));

// ── Boot: create tables, then listen ──
(async () => {
  try {
    const pgPool = new Pool({
      connectionString: env.DATABASE_URL.replace(/[?&]sslmode=[^&]*/, ''),
      ssl: { rejectUnauthorized: false },
      max: 5,
    });
    await pgPool.query('SELECT 1');
    db = pgPool;
    console.log('Connected to PostgreSQL database');
  } catch (err) {
    console.warn(`PostgreSQL connection failed (${err.message}). Using in-memory database fallback.`);
    const { newDb, DataType } = require('pg-mem');
    const memDb = newDb();
    memDb.public.registerFunction({
      name: 'gen_random_uuid',
      returns: DataType.uuid,
      implementation: () => crypto.randomUUID(),
    });
    const { Pool: MemPool } = memDb.adapters.createPg();
    db = new MemPool();
  }
  await db.query(fs.readFileSync(path.join(__dirname, 'schema.sql'), 'utf8'));
  const port = Number(env.PORT) || 3000;
  app.listen(port, '0.0.0.0', () => console.log(`Junk It Out API on :${port}`));
})().catch((e) => { console.error('Startup failed:', e.message); process.exit(1); });
