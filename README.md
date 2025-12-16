## API rate-limiter

### Overview

This project implements an API rate limiter for a notification service using:

- Java 21, Spring Boot 3
- Redis (counters and quotas)
- PostgreSQL (per-client policies)
- Kafka (audit / throttle events)
- Bucket4J (local burst smoothing)
- Docker & Kubernetes (deployment)
- OpenAPI (Swagger UI) for API documentation

The rate limiter supports per-client time-window limits, monthly quotas, and global system limits, with both soft and hard throttling strategies.

---

### Mandatory use cases

#### 1. Define request limits within a time window for each client

- **Endpoint:** `POST /api/v1/rate-limits/clients`
- **Controller:** `RateLimitPolicyController.upsertClientPolicy(...)`
- **Entity:** `ClientRateLimitPolicy.windowSeconds`, `ClientRateLimitPolicy.windowMaxRequests`
- **Service:** `RateLimitPolicyService.upsertPolicy(...)`

**Tests:**

- `RateLimitPolicyServiceTest.upsertPolicy_createsNewPolicy()`
- `RateLimitPolicyControllerTest.upsertClientPolicy_returnsPolicy()`

#### 2. Define monthly request limits per client

- Same endpoint as above.
- **Entity:** `ClientRateLimitPolicy.monthlyMaxRequests`
- **DTOs:** `ClientRateLimitRequest.monthlyMaxRequests`, `ClientRateLimitResponse.monthlyMaxRequests`

**Tests:**

- `RateLimitPolicyServiceTest.upsertPolicy_createsNewPolicy()`
- `RateLimitPolicyControllerTest.upsertClientPolicy_returnsPolicy()`

#### 3. Enforce time-window request limits

- **Service:** `RateLimiterService.checkAndConsume(...)`
    - Uses Redis key: `rl:client:{clientId}:win:{window_ts}`
    - Rejects when `clientWinCount > windowMaxRequests`.

- **HTTP enforcement:** `RateLimitingFilter` (applied to `/api/v1/demo/**`)
    - Calls `RateLimiterService.checkAndConsume(clientId)`.
    - Returns HTTP `429` with `Retry-After` when not allowed.

**Tests:**

- `RateLimiterServiceTest.deniesWhenClientWindowExceededHard()`
- `DemoNotificationIntegrationTest.notifyEndpoint_hardThrottled_returns429AndRetryAfter()`

#### 4. Enforce monthly request limits

- **Service:** `RateLimiterService.checkAndConsume(...)`
    - Uses Redis key: `rl:client:{clientId}:month:{YYYYMM}`
    - Rejects when `clientMonthCount > monthlyMaxRequests`.

**Tests:**

- `RateLimiterServiceTest.deniesWhenClientMonthlyLimitExceeded()`

#### 5. Enforce global request limits across the entire system

- **Configuration:** `RateLimiterProperties` (`rate-limiter.global.*`)
- **Service:** `RateLimiterService.checkAndConsume(...)`
    - Global window key: `rl:global:win:{window_ts}`
    - Global monthly key: `rl:global:month:{YYYYMM}`
    - Enforces `maxRequests` and `monthlyMaxRequests` across **all clients**.

**Tests:**

- `RateLimiterServiceTest.deniesWhenGlobalWindowLimitExceeded()`
- `RateLimiterServiceTest.deniesWhenGlobalMonthlyLimitExceeded()`

---

### Key concerns

#### Handling high request volumes in the same time window

- Redis-based counters shared by all instances:
    - `RateLimiterService.incrementWithExpiry(...)` using `RedisTemplate`.
- Local burst smoothing with Bucket4J:
    - `RateLimiterService` maintains per-client `Bucket` instances in-memory on each node.
- Horizontal scaling via Kubernetes:
    - `k8s/deployment.yaml` specifies multiple replicas (`replicas: 3`).

#### Preventing a client from exceeding their monthly quota

- Redis monthly counter:
    - `rl:client:{clientId}:month:{YYYYMM}` with TTL ~31 days.
- Enforced in `RateLimiterService.checkAndConsume(...)`.
- Covered by `RateLimiterServiceTest.deniesWhenClientMonthlyLimitExceeded()`.

#### Enforcing global system limits

- Global counters in Redis:
    - `rl:global:win:{window_ts}`
    - `rl:global:month:{YYYYMM}`
- Configured via `RateLimiterProperties` (`rate-limiter.global.*` / ConfigMap).
- Enforced before per-client checks in `RateLimiterService.checkAndConsume(...)`.
- Covered by:
    - `RateLimiterServiceTest.deniesWhenGlobalWindowLimitExceeded()`
    - `RateLimiterServiceTest.deniesWhenGlobalMonthlyLimitExceeded()`

#### Ensuring the solution works in a distributed, multi-server environment

- Application is stateless with respect to counters; all authoritative state is in Redis.
- Local Bucket4J is only for smoothing, not for canonical limits.
- Kubernetes Deployment (`k8s/deployment.yaml`) runs multiple pods behind `ratelimiter-service` (`k8s/service.yaml`), sharing Redis.
- Any number of pods can be added via `kubectl scale`.

#### Handling throttling strategies (soft and hard throttling)

- Throttle mode per client:
    - `ThrottleMode` enum (`SOFT`, `HARD`)
    - Stored in `ClientRateLimitPolicy.throttleMode`.
- Decision logic in `RateLimiterService.deny(...)`:
    - **HARD:** `allowed=false`, `softThrottled=false`, HTTP `429` from filter.
    - **SOFT:** `allowed=true`, `softThrottled=true`, Kafka event `"SOFT_THROTTLE:..."`.
- Kafka integration:
    - `KafkaTemplate<String, String>` sends `"SOFT_THROTTLE"` or `"HARD_THROTTLE"` events.
- Tests:
    - `RateLimiterServiceTest.softThrottleWhenModeSoft()`
    - `DemoNotificationIntegrationTest.notifyEndpoint_hardThrottled_returns429AndRetryAfter()`

---

### How to run

1. **Local (no Kubernetes):**
    - Start Postgres, Redis, Kafka (e.g. via docker-compose).
    - `mvn spring-boot:run`
    - Use Swagger: `http://localhost:8080/swagger-ui.html`

2. **Tests:**
    - `mvn test`

3. **Kubernetes (minikube example):**
    - `minikube start`
    - `eval $(minikube docker-env)`
    - `docker build -t rate-limiter:latest .`
    - `kubectl apply -f k8s/configmap.yaml`
    - `kubectl apply -f k8s/deployment.yaml`
    - `kubectl apply -f k8s/service.yaml`
    - `kubectl port-forward svc/ratelimiter-service 8080:80`
    - Call APIs via `http://localhost:8080/...` from Postman.

## Environment Variables

### Local (example)
```bash
# Server
export PORT=8080

# JWT (must be >= 32 chars)
export APP_JWT_SECRET="replace-with-a-32+char-secret-key"

# CORS (Angular dev server)
export APP_CORS_ALLOWED_ORIGINS="http://localhost:4200"

# PostgreSQL (local example)
export SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5432/rate_limiter"
export SPRING_DATASOURCE_USERNAME="postgres"
export SPRING_DATASOURCE_PASSWORD="postgres"

# Seeder
export APP_SEED_ENABLED=true
export APP_SEED_RESET_PASSWORDS=false

# Redis
export REDIS_URL="redis://localhost:6379"