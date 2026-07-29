// ── load-tests/admission_peak.js ─────────────────────────────────────────────
// Peak load test — 1000 concurrent users submitting applications
// Run: k6 run --out experimental-prometheus-rw admission_peak.js

import http from 'k6/http';
import { check, sleep } from 'k6';

const BASE_URL = 'http://localhost:8080';

export const options = {
  stages: [
    { duration: '30s', target: 100 },   // ramp up to 100 users
    { duration: '1m',  target: 500 },   // ramp up to 500 users
    { duration: '1m',  target: 1000 },  // ramp up to 1000 users (peak)
    { duration: '30s', target: 1000 },  // hold peak
    { duration: '30s', target: 0 },     // ramp down
  ],
  thresholds: {
    http_req_duration: ['p(95)<3000'],  // 95% of requests under 3s
    http_req_failed:   ['rate<0.1'],    // less than 10% error rate
  },
};

// Get a token before the test starts
export function setup() {
  // Register a test user
  const registerRes = http.post(
    `${BASE_URL}/auth/register`,
    JSON.stringify({
      email: `loadtest_${Date.now()}@test.com`,
      password: 'Test@1234'
    }),
    { headers: { 'Content-Type': 'application/json' } }
  );

  if (registerRes.status === 201) {
    return { token: registerRes.json('accessToken') };
  }

  // If register fails, try login with existing test user
  const loginRes = http.post(
    `${BASE_URL}/auth/login`,
    JSON.stringify({
      email: 'loadtest@test.com',
      password: 'Test@1234'
    }),
    { headers: { 'Content-Type': 'application/json' } }
  );
  return { token: loginRes.json('accessToken') };
}

export default function (data) {
  const headers = {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${data.token}`,
  };

  // Submit application — the core load test endpoint
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

  check(res, {
    'status is 202 or 409': (r) => r.status === 202 || r.status === 409,
  });

  sleep(1);
}