## Introduction

I started developing this project during laboratories on my studies but then I have developed it by myself.
Its assumption is that we have coaches and swimmers — every swimmer can have one coach, but coaches can have
many swimmers. In connection with this we have two microservices. They publish REST API that enables different
operations on coaches and swimmers. Apart from that I implemented a frontend (JS, CSS and HTML) and an API
gateway. Each component is dockerized including two PostgreSQL database servers for microservices.
The communication between coach and swimmer services is handled asynchronously and synchronously via RabbitMQ.
I also implemented authentication and authorization using Okta, and provided a CI/CD pipeline using GitHub Actions.

## Technology Stack

| Layer          | Technology                                                          |
|----------------|---------------------------------------------------------------------|
| Language       | Java 21                                                             |
| Framework      | Spring Boot 3.5, Spring Security, Spring Data JPA, Spring AMQP     |
| API Gateway    | Spring Cloud Gateway (WebFlux)                                      |
| Messaging      | RabbitMQ (JSON messages via `Jackson2JsonMessageConverter`)          |
| Databases      | PostgreSQL (coach, swimmer), H2 (coach tests)                      |
| Auth           | Okta (OAuth 2.0 + PKCE, JWT resource server)                       |
| Frontend       | Vanilla JavaScript, HTML, CSS, Okta Auth JS SDK                     |
| Containerization | Docker, Docker Compose                                            |
| CI/CD          | GitHub Actions                                                      |
| Testing        | TestNG, Testcontainers (PostgreSQL, RabbitMQ), Awaitility, MockMvc  |

## Architecture Diagram

```
                                ┌──────────┐
                                │   Okta   │
                                │(OAuth 2.0│
                                │ + PKCE)  │
                                └────┬─────┘
                                     │ 
                                     │
┌──────────┐    JWTs (Bearer)   ┌────▼─────┐
│ Frontend │───────────────────►│ Gateway  │
│ (httpd)  │                    │ (WebFlux)│
└──────────┘                    └────┬─────┘
                                     │
                        ┌────────────┴────────────┐
                        │                         │
                        ▼                         ▼
             ┌──────────────┐          ┌──────────────┐
             │    Coach     │          │   Swimmer    │
             │   Service    │          │   Service    │
             └───┬──────┬───┘          └───┬──────┬───┘
                 │      │                  │      │
                 │      │  ┌───────────┐   │      │
                 │      └─►│ RabbitMQ  │◄──┘      │
                 │         │           │          │
                 │         │ Queues:   │          │
                 │         │ • delete. │          │
                 │         │   coach   │          │
                 │         │ • get.    │          │
                 │         │   coach.  │          │
                 │         │   swimmers│          │
                 │         └───────────┘          │
                 │                                │
             ┌───▼─────────┐         ┌────────────▼──┐
             │  PostgreSQL │         │   PostgreSQL  │
             │  (coach_db) │         │  (swimmer_db) │
             └─────────────┘         └───────────────┘
```

**Coach → Swimmer communication via RabbitMQ:**
- **`delete.coach.queue`** — when a coach is deleted, the coach service publishes a `DeleteCoachEvent` (asynchronous, fire-and-forget). The swimmer service consumes it and unassigns all swimmers from that coach.
- **`get.coach.swimmers.queue`** — when requesting swimmers of a coach, the coach service publishes a `GetCoachSwimmersRequest` and synchronously waits for the response (request-response pattern via `convertSendAndReceiveAsType`).

## Authentication & Authorization (Okta)

Authentication is configured using the **Okta Integrator Free Plan** with **OAuth 2.0 Authorization Code flow + PKCE**.
No client secret is used — PKCE (Proof Key for Code Exchange) is enabled to increase security, making it suitable
for public clients like the frontend.

**Token configuration:**
- **Access token** — valid for **5 minutes**
- **Refresh token** — valid for **10 minutes** (with `offline_access` scope)
- **Auto-renewal** — the Okta Auth JS SDK automatically renews the access token using the refresh token
- **Inactivity timeout** — after **10 minutes** of inactivity (refresh token expires), the user is automatically logged out

**Authorization:**
- JWT tokens carry a custom `role` claim (configured in Okta Authorization Server)
- The gateway and both microservices validate JWTs as OAuth2 resource servers
- Method-level security is enforced using `@PreAuthorize` — for example, deleting a coach requires the `admin` authority

**Logout:**
- Tokens are cleared from `localStorage` and the user is signed out of the Okta session via `oktaAuth.signOutOfOkta()`

## CI/CD Pipeline (GitHub Actions)

The CI/CD pipeline is defined in `.github/workflows/ci.yml`.

**CI — runs on every push to any branch:**
- **`build-coach`** — `mvn verify` (compiles + runs integration tests)
- **`build-swimmer`** — `mvn verify` (compiles + runs integration tests)
- **`build-gateway`** — `mvn package` (compiles only, no tests)

All three build jobs run **in parallel**.

**CD — runs only on push to `master`:**
- **`docker`** — after all build jobs pass, Docker images are built and pushed to Docker Hub for: `coach`, `swimmer`, `gateway`, `frontend`

## Integration Tests

Integration tests use **TestNG** with **Testcontainers** to spin up real PostgreSQL and RabbitMQ containers.

**Test infrastructure:**
- `RabbitMqTestContainer` — starts a RabbitMQ container and exposes a connection factory
- `PostgreSqlTestContainer` — starts a PostgreSQL container initialized with `init.sql`
- `IntegrationTestConfiguration` — base class that extends `AbstractTestNGSpringContextTests`, starts containers, declares queues, and overrides Spring properties via `@DynamicPropertySource`

**Test classes (named `*IT` for failsafe plugin):**
- **swimmer service:**
    - `PostMethodTestIT` — tests CRUD operations via MockMvc
    - `CoachConsumerTestIT` — tests RabbitMQ consumers (delete coach event, get coach swimmers request)
- **coach service:**
    - `PostMethodTestIT` — tests coach creation, deletion (with `admin` role), and verifies that `DeleteCoachEvent` is published to RabbitMQ. Also includes a negative security test (DELETE with `user` role returns 403)

**Running integration tests locally:**
```bash
# swimmer service (requires Docker running for Testcontainers)
mvn verify --file swimmer/pom.xml

# coach service (requires Docker running for Testcontainers)
mvn verify --file coach/pom.xml
```

> **Note:** Docker must be running locally because Testcontainers starts real PostgreSQL and RabbitMQ containers during the tests.

## How to Run

### Prerequisites
- Docker and Docker Compose

### Start the application

```bash
cd docker
docker-compose up --build -d
```

This starts all services:

| Service             | URL                    |
|---------------------|------------------------|
| Frontend            | http://localhost:8083   |
| Gateway             | http://localhost:8080   |
| Coach service       | http://localhost:8081   |
| Swimmer service     | http://localhost:8082   |
| RabbitMQ Management | http://localhost:15672  |

### Stop the application

```bash
cd docker
docker-compose down
```
