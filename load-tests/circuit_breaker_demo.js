// ── load-tests/circuit_breaker_demo.js ───────────────────────────────────────
// Circuit breaker demo — sustained load, then kill Admission Service mid-test
// Run: k6 run circuit_breaker_demo.js
//
// Demo steps:
// 1. Start this script
// 2. Wait 30 seconds (requests flowing normally)
// 3. Stop Admission Service in IntelliJ
// 4. Watch responses change from 202 to 503 fallback
// 5. Restart Admission Service
// 6. Watch circuit breaker recover (HALF_OPEN → CLOSED)

import http from 'k6/http';
import { check, sleep } from 'k6';

const BASE_URL = 'http://localhost:8080';

export const options = {
  vus: 20,
  duration: '3m',
};

export function setup() {
  const loginRes = http.post(
    `${BASE_URL}/auth/login`,
    JSON.stringify({ email: 'disha@gmail.com', password: 'Test@1234' }),
    { headers: { 'Content-Type': 'application/json' } }
  );
  return { token: loginRes.json('accessToken') };
}

export default function (data) {
  const headers = {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${data.token}`,
  };

  const res = http.post(
    `${BASE_URL}/application`,
    JSON.stringify({
      profileId: '9078a21a-55fc-4c46-853c-cd481e833ed1',
      courseName: 'Computer Science',
      university: 'MIT',
      intakeYear: 2026
    }),
    { headers }
  );

  // Log the status so you can see when CB opens (202 → 503)
  console.log(`Status: ${res.status} | Body: ${res.body.substring(0, 100)}`);

  check(res, {
    '202 Accepted (normal)':       (r) => r.status === 202,
    '409 Conflict (duplicate)':    (r) => r.status === 409,
    '503 CB Fallback (CB open)':   (r) => r.status === 503,
  });

  sleep(2);
}