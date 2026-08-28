import http from "k6/http";
import { check, sleep } from "k6";

// --- Configuration (all overridable from the CLI via -e KEY=value) ---
// BASE_URL       - the API Gateway invoke URL
// TOKEN          - a valid Okta access token
// ENDPOINT_PATH  - the endpoint -> a lightweight GET so CPU (not RDS) is the bottleneck
const BASE_URL = __ENV.BASE_URL;
const TOKEN = __ENV.TOKEN;
const ENDPOINT_PATH = __ENV.ENDPOINT_PATH || "/coaches";

if (!BASE_URL || !TOKEN) {
  throw new Error("BASE_URL and TOKEN must be set, e.g. k6 run -e BASE_URL=... -e TOKEN=... load-test.js");
}

export const options = {
  // Ramp virtual users up, hold, then down. It takes 4 minutes because the Okta access token
  // is only valid for 5 minutes - the hold is still long enough to trigger a scale-out.
  //
  // A "target" is the number of concurrent virtual users (VUs), and "duration" is how long k6 takes
  // to linearly ramp from the previous target to this one. Each VU runs the default function in a
  // loop (GET -> sleep(0.1) -> GET ...). The real throughput depends on response latency:
  // 100 VUs won't do 1000 req/s if each request takes ~200ms - expect a few hundred req/s.
  stages: [
    { duration: "30s", target: 20 },  // ramp 0 -> 20 VUs (warm up)
    { duration: "1m", target: 100 },  // ramp 20 -> 100 VUs
    { duration: "2m", target: 100 },  // hold at 100 VUs - watch the task count grow here
    { duration: "30s", target: 0 },   // ramp 100 -> 0 VUs (ramp down)
  ],
  thresholds: {
    // Fail the run if too many requests error or the API gets too slow.
    http_req_failed: ["rate<0.05"],        // < 5% errors
    http_req_duration: ["p(95)<2000"],     // 95% of requests under 2s
  },
};

const params = {
  headers: {
    Authorization: `Bearer ${TOKEN}`,
  },
};

export default function () {
  const res = http.get(`${BASE_URL}${ENDPOINT_PATH}`, params);

  const ok = check(res, {
    "status is 200": (r) => r.status === 200,
    "not unauthorized": (r) => r.status !== 401 && r.status !== 403,
  });

  // On failure, log the real status + a short body snippet to see what actually comes back
  // (e.g. a 404 from API Gateway, or a 5xx from the ALB/service). Throttled so the console isn't
  // flooded: only ~1% of failures are printed.
  if (!ok && Math.random() < 0.01) {
    console.error(
      `FAIL status=${res.status} method=${res.request.method} url=${res.request.url} ` +
      `body=${String(res.body).slice(0, 200)}`
    );
  }

  sleep(0.1); // short pause between requests so 100 VUs generate meaningful load on the service CPU
}
