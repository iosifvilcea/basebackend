This file provides guidance when working with code in this repository.


## First-time setup

After cloning, run once to activate the pre-commit and pre-push hooks:

```bash
git config core.hooksPath .git-hooks
chmod +x .git-hooks/pre-commit .git-hooks/pre-push
```


## Building

### Profiles

| Profile | `app.url`                 | Secure cookies | Log format |
| ------- | ------------------------- | -------------- | ---------- |
| `dev`   | `http://0.0.0.0/`         | `false`        | Plain text |
| `prod`  | `https://your-app.com`    | `true`         | JSON (ECS) |
|         |                           |                |            |

The active profile is set in `application.properties` (`spring.profiles.active=dev`). Override at runtime with `--spring.profiles.active=prod`.

### Api Endpoints         

```bash
/api/auth
/api/auth?token=
/api/auth/refresh
/api/auth/logout
/api/profile/{email}
/api/profile/id/{id}
```

### Local Development (without Docker)

Create `src/main/resources/env.properties` using the keys defined in `[your-local-env]`, then run:

```bash
./gradlew bootRun
```

The app starts on port `8090`. Actuator endpoints are on `8091`.

### Docker (recommended)

```bash
cp [your-local-env] .env
# fill in values
docker compose up --build
```

Builds the app image and starts PostgreSQL, Prometheus, Loki, Promtail, and Grafana alongside it.

### Production JAR

```bash
./gradlew bootJar
java -jar build/libs/BaseBackend-0.0.1-SNAPSHOT.jar \
  --spring.profiles.active=prod \
  --DB_URL=jdbc:postgresql://host:5432/db \
  --JWT_SECRET=...
```

### Services

| Service    | URL                            |
|------------|--------------------------------|
| API        | http://localhost:8090          |
| Actuator   | http://localhost:8091/actuator |
| Prometheus | http://localhost:9090          |
| Grafana    | http://localhost:3000          |
| Loki       | http://localhost:3100          |


## Running Tests

### Tests

```bash
./gradlew test
```

Results are written to `build/reports/tests/test/index.html`.
#### Libraries

- **JUnit 5** — test runner
- **MockK** — Kotlin-native mocking for service and unit tests (`mockk<T>()`, `every`, `verify`)
- **Mockito** — bean mocking for controller slice tests (`@MockitoBean` within `@WebMvcTest`)
- **MockMvc** — controller-layer slice tests (`@WebMvcTest`)
- **Spring Security Test** — `@WithMockUser` for authenticated request simulation

### Code Quality

```bash
# Check code formatting (must pass before commit)
./gradlew spotlessCheck

# Auto-format code
./gradlew spotlessApply
```

**Note**
Code Quality must run and pass before commit.
Tests must run and pass before push.

### Git Hooks

Hooks live in `.git-hooks/`. Run once after cloning to activate them:

```bash
git config core.hooksPath .git-hooks
chmod +x .git-hooks/pre-commit .git-hooks/pre-push
```

| Hook         | What it runs                                                                                                                 |
| ------------ | ---------------------------------------------------------------------------------------------------------------------------- |
| `pre-commit` | `spotlessCheck` — if formatting fails, runs `spotlessApply` automatically and exits so you can stage the fixes and re-commit |
| `pre-push`   | `./gradlew test` — runs the full unit test suite before pushing                                                              |


## CI/CD

Defined in `.github/workflows/ci.yml`. Triggers on PRs to `develop` and `main`, and on every push to `main`.

### Pipeline

| Job      | Trigger              | Steps                                                              |
|----------|----------------------|--------------------------------------------------------------------|
| `ci`     | PRs + push to `main` | Spins up a PostgreSQL service container, runs `spotlessCheck` then `./gradlew test` |
| `deploy` | Push to `main` only  | Builds and pushes the Docker image to GHCR, then deploys to the VPS |

### Required secrets

| Secret                | Description                                        |
|-----------------------|----------------------------------------------------|
| `VPS_HOST`            | IP or hostname of the VPS                          |
| `VPS_USER`            | SSH user on the VPS                                |
| `VPS_SSH_KEY`         | Private SSH key for authentication                 |
| `GHCR_PAT`            | Personal access token for pulling the image on the VPS |
| `TS_OAUTH_CLIENT_ID`  | Tailscale OAuth client ID for the CI runner        |
| `TS_OAUTH_SECRET`     | Tailscale OAuth secret for the CI runner           |

`GITHUB_TOKEN` is provided automatically by GitHub Actions for pushing to GHCR.


## Database Migrations

Migrations are managed by Flyway and live in `src/main/resources/db/migration/`. The app applies pending migrations automatically on startup.

### Rules

- Never edit a migration file after it has been applied — Flyway checksums each file and will refuse to start if a checksum changes.
- Every schema change gets a new versioned file: `V2__add_something.sql`, `V3__alter_something.sql`, etc.
- `V1__initial_schema.sql` covers the base auth schema (users, profiles, refresh_tokens, magic_link_tokens). New apps built from this scaffold should add their domain tables starting at `V2`.

### Adding a migration

```bash
# Create a new file — bump the version number
touch src/main/resources/db/migration/V2__your_change.sql
# Write your SQL, then run the app or ./gradlew bootRun to apply it
```


## Architecture Overview

### Domain Driven Module Structure

Layered MVC with domain-driven package structure. Each package owns its own controller, service, repository, entity, and exception.

| Package          | Responsibility                                                                       |
| ---------------- | ------------------------------------------------------------------------------------ |
| `auth`           | JWT generation/validation, refresh tokens, Spring Security config, cookie management |
| `user`           | User entity, session orchestration                                                   |
| `magiclinktoken` | Token creation, hashing, single-use validation                                       |
| `email`          | Email sending                                                                        |
| `profile`        | Profile management                                                                   |
| `analytics`      | Event tracking                                                                       |
| `utils`          | Secure token generation                                                              |

### Dependency Flow

```
Controllers (UserController, ProfileController, ...)
	↓
Services (UserService, ProfileService, EmailService, ...)
	↓
Repositories (UserRepository, ProfileRepository, ...)
```

### Technology Stack

| Category         | Technology                                                            |
| ---------------- | --------------------------------------------------------------------- |
| Language         | Kotlin 2.2                                                            |
| Framework        | Spring Boot 4.0                                                       |
| Security         | Spring Security — stateless JWT via HTTP-only cookies                 |
| Authentication   | JJWT 0.13 — JWT generation and validation                             |
| Database         | PostgreSQL + Spring Data JPA (Hibernate) + Flyway migrations          |
| Email            | Spring Mail — JavaMailSender over SMTP                                |
| Metrics          | Micrometer + Prometheus registry                                      |
| Logging          | SLF4J with ECS structured JSON format (prod)                          |
| Observability    | Spring Boot Actuator (health, metrics, info)                          |
| Code Style       | Spotless + ktlint                                                     |
| Testing          | JUnit 5, MockK, MockMvc, Spring Security Test                         |
| Build            | Gradle (Kotlin DSL) with version catalog                              |
| Containerisation | Docker Compose — app, PostgreSQL, Prometheus, Loki, Promtail, Grafana |


## Development Guidelines

### Code Style

- Line length: **120 characters**.
- Must pass `spotlessCheck` before merge (auto-format with `spotlessApply`).
- Use **TODO** for temporary notes, never use **FIXME**.
- All warnings treated as errors in Kotlin compilation.

### Testing

- Every feature requires unit tests.
- Write unit tests for Controllers, Services, Utility/Helper classes, and business logic.
- When refactoring code, update unit tests first.

### Analytics

- Log at boundaries and failures. The goal is to be able to reconstruct what happened without needing to reproduce it.
- Every log should contain enough context to be useful in isolation - who, what, when.
- Logs should never contain sensitive information.
- Use `AnalyticsTracker` service for event tracking.

### Exception Handling

 - Handle everything at the right boundary. Crash only at the right layer. Never leak raw failures across boundaries.
 - Is it an expected failure (invalid input, rule violation)? Handle it.
 - Is it recoverable (timeouts, external api failure, db issues)? Retry, degrade, translate.
 - Is it unrecoverable (corrupt state, programming errors)? Fail fast, log, handle/show error with grace.
