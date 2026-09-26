import http from 'k6/http';
import { check } from 'k6';

const BASE_URL = 'http://localhost:8080';

const ACCESS_TOKEN = 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJtYW5hc0BnbWFpbC5jb20iLCJpYXQiOjE3ODk5MTU0MDUsImV4cCI6MTc4OTkxNjMwNX0.dveOC-Ggnk9PhwN0DvoUorKXu4UeWcxmgg2T6W0o2Ps';

export const options = {
    scenarios: {
        get_appointments: {
            executor: 'constant-arrival-rate',
            rate: 2500,
            timeUnit: '1s',
            duration: '1m',
            preAllocatedVUs: 400,
            maxVUs: 500,
        },
    },

    thresholds: {
        http_req_failed: ['rate<0.01'],
        http_req_duration: ['p(95)<500'],
    },
};

export default function () {

    const response = http.get(
        `${BASE_URL}/api/patient/get-all-appointments`,
        {
            headers: {
                Authorization: `Bearer ${ACCESS_TOKEN}`,
                'Content-Type': 'application/json',
            },
        }
    );

    if (response.status !== 200) {
        console.log(
            `FAILED: status=${response.status}, body=${String(response.body || '').substring(0, 300)}`
        );
    }

    check(response, {
        'status is 200': (r) => r.status === 200,

        'response contains appointments': (r) =>
            r.body !== null &&
            r.body !== undefined &&
            r.body.length > 0,
    });
}