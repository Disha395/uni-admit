// ── load-tests/admission_peak.js ─────────────────────────────────────────────
// Peak load test — 2,000 concurrent users checking application status
// Run: k6 run admission_peak.js

import http from 'k6/http';
import { check, sleep } from 'k6';

const BASE_URL = 'http://localhost:8080';

export const options = {
    setupTimeout: '120s',

    stages: [
        { duration: '30s', target: 100 },
        { duration: '1m',  target: 300 },
        { duration: '1m',  target: 700 },
        { duration: '1m',  target: 1000 },
        { duration: '1m',  target: 1500 },
        { duration: '1m',  target: 2000 },
        { duration: '1m',  target: 2000 },  // hold at 2,000
        { duration: '30s', target: 0 },
    ],

    thresholds: {
        http_req_duration: ['p(95)<3000'],
        http_req_failed: ['rate<0.05'],
    },
};

// Login once — get token before test starts
export function setup() {
    const loginRes = http.post(
        `${BASE_URL}/auth/login`,
        JSON.stringify({
            email: 'test@example.com',
            password: 'Test@12345'
        }),
        {
            headers: {
                'Content-Type': 'application/json'
            }
        }
    );

    const loginOk = check(loginRes, {
        'login successful': (r) => r.status === 200,
        'has accessToken': (r) => r.json('accessToken') !== undefined,
    });

    if (!loginOk) {
        console.error(`Login failed: ${loginRes.status} ${loginRes.body}`);
    }

    return {
        token: loginRes.json('accessToken')
    };
}

// 2,000 VUs all use the same token
// Tests Gateway JWT validation + Admission Service
export default function (data) {
    const headers = {
        'Authorization': `Bearer ${data.token}`,
        'Content-Type': 'application/json',
    };

    // GET /application/my
    // Simulates students checking application status
    const res = http.get(
        `${BASE_URL}/application/my`,
        { headers }
    );

    check(res, {
        'status is 200': (r) => r.status === 200,
        'response has data': (r) => r.body.length > 0,
    });

    if (res.status !== 200) {
        console.log(
            `FAILED STATUS: ${res.status} | BODY: ${res.body.substring(0, 200)}`
        );
    }

    sleep(0.5);
}