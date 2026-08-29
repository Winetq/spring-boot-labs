# Load test (k6)

A small [k6](https://k6.io) script to generate traffic against the ECS stack's API Gateway and watch
the ECS **service auto-scaling** add/remove Fargate tasks under load.

## Prerequisites

```bash
brew install k6
```

## Run

You need the API Gateway invoke URL (`ApiEndpoint` stack output) and a **valid Okta access token**.

```bash
cd load-test
k6 run \
  -e BASE_URL=https://<api-id>.execute-api.eu-central-1.amazonaws.com \
  -e TOKEN=<okta-access-token> \
  load-test.js
```

Optional: override the endpoint (defaults to `/coaches`) with `-e ENDPOINT_PATH=/swimmers`.

## Notes

- The Okta access token is valid for only **5 minutes**, so this test is kept to ~4 minutes total.
  Start k6 right after grabbing a fresh token; if a run starts returning 401/403, the token expired.
- Hit a **lightweight GET** so the service CPU (not RDS) is the bottleneck.
