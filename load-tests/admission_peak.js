// ── load-tests/admission_peak.js ─────────────────────────────────────────────
// Peak load test — 1000 concurrent users hitting Auth + Gateway
// Run: k6 run admission_peak.js

import http from 'k6/http';
import { check, sleep } from 'k6';

const BASE_URL = 'http://localhost:8080';

export const options = {
  stages: [
    { duration: '30s', target: 50  },  // ramp up to 100 users
    { duration: '1m',  target: 100 },  // ramp up to 500 users
    { duration: '1m',  target: 200 },  // ramp up to 1000 users (peak)
    { duration: '30s', target: 200 },  // hold peak
    { duration: '30s', target: 0    },  // ramp down
  ],
  thresholds: {
    http_req_duration: ['p(95)<3000'],  // 95% of requests under 3s
    http_req_failed:   ['rate<0.05'],   // less than 5% error rate
  },
};

export default function () {
  // ── Step 1: Login ──────────────────────────────────────────────────────────
  const loginRes = http.post(
    `${BASE_URL}/auth/login`,
    JSON.stringify({
      email: 'rick1@gmail.com',      // replace with a valid user in your auth_db
      password: 'rick1@395'
    }),
    { headers: { 'Content-Type': 'application/json' } }
  );

  const loginOk = check(loginRes, {
    'login 200': (r) => r.status === 200,
    'has accessToken': (r) => r.json('accessToken') !== undefined,
  });

  if (!loginOk) {
    sleep(1);
    return;
  }

  const token = loginRes.json('accessToken');

  // ── Step 2: Hit a protected endpoint with JWT ──────────────────────────────
  const profileRes = http.get(
    `${BASE_URL}/application/my`,
    {
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json',
      }
    }
  );

  check(profileRes, {
    'protected route 200': (r) => r.status === 200,
  });

  sleep(1);
}