# Cloud-Based Ticket Reservation System
### SOEN 345 — Software Testing, Verification & Quality Assurance | Concordia University

A production-quality, cloud-ready **ticket reservation REST API** built with Java 17 and Spring Boot 3.2.

---

## Quick Start

```bash
# Clone the repository
git clone https://github.com/your-team/go-tix.git
cd go-tix

# Run all tests with coverage report
mvn test

# Start the application (uses H2 in-memory DB by default)
mvn spring-boot:run

# Access Swagger UI
open http://localhost:8080/swagger-ui.html

# Access H2 console (dev only)
open http://localhost:8080/h2-console
```

---

## API Endpoints

### Authentication (Public)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | Register with email or phone |
| POST | `/api/auth/login` | Login, receive JWT |

### Events (Public GET, Admin POST/PUT/DELETE)
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/api/events` | — | List all active events |
| GET | `/api/events/{id}` | — | Get event by ID |
| GET | `/api/events/search?keyword=` | — | Full-text search |
| GET | `/api/events/filter?category=&location=&startDate=&endDate=` | — | Filter events |
| POST | `/api/events` | ADMIN | Create event |
| PUT | `/api/events/{id}` | ADMIN | Update event |
| DELETE | `/api/events/{id}` | ADMIN | Cancel event |

### Reservations (Authenticated)
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/reservations` | Customer | Reserve tickets |
| GET | `/api/reservations/my` | Customer | View own reservations |
| GET | `/api/reservations/{id}` | Customer/Admin | Get reservation |
| GET | `/api/reservations/code/{code}` | Customer/Admin | Lookup by confirmation code |
| DELETE | `/api/reservations/{id}` | Customer/Admin | Cancel reservation |
| GET | `/api/reservations/event/{eventId}` | ADMIN | All reservations for event |

### Users
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/api/users/me` | Customer | Get own profile |
| PUT | `/api/users/me` | Customer | Update profile |
| GET | `/api/users` | ADMIN | List all users |
| GET | `/api/users/{id}` | ADMIN | Get user by ID |
| DELETE | `/api/users/{id}` | ADMIN | Deactivate user |

---

## Architecture

```
src/main/java/com/ticketreservation/
├── controller/          # REST API layer (HTTP in/out, DTOs)
│   ├── AuthController.java
│   ├── EventController.java
│   ├── ReservationController.java
│   └── UserController.java
├── service/             # Business logic
│   ├── UserService.java
│   ├── EventService.java
│   ├── ReservationService.java
│   └── NotificationService.java
├── repository/          # Spring Data JPA repositories
│   ├── UserRepository.java
│   ├── EventRepository.java
│   └── ReservationRepository.java
├── model/               # JPA entities
│   ├── User.java
│   ├── Event.java
│   └── Reservation.java
├── dto/                 # Request/Response objects
│   ├── RegisterRequest / LoginRequest / AuthResponse
│   ├── EventRequest / EventResponse
│   ├── ReservationRequest / ReservationResponse
│   └── ApiErrorResponse
├── config/              # Security, JWT
│   ├── SecurityConfig.java
│   ├── JwtUtil.java
│   ├── JwtAuthFilter.java
│   └── CustomUserDetailsService.java
└── exception/           # Exception hierarchy + global handler
    ├── TicketReservationException.java
    └── GlobalExceptionHandler.java
```

---

## Testing

```bash
mvn test                          # Run all 55 tests
mvn jacoco:report                 # Generate HTML coverage report
open target/site/jacoco/index.html
```

| Test Class | Tests | Type |
|---|---|---|
| UserServiceTest | 10 | Unit |
| EventServiceTest | 10 | Unit |
| ReservationServiceTest | 12 | Unit |
| EventTest | 8 | Unit (Model) |
| ReservationTest | 7 | Unit (Model) |
| AuthControllerTest | 8 | Controller (MockMvc) |
| EventControllerTest | 11 | Controller (MockMvc) |
| ReservationControllerTest | 14 | Controller (MockMvc) |
| **Total** | **80** | |

---

## CI/CD Pipeline

Five-stage GitHub Actions workflow (`.github/workflows/ci-cd.yml`):

1. **Build & Unit Tests** — compile + `mvn test` + upload JaCoCo report
2. **Code Quality** — SpotBugs static analysis
3. **Integration Tests** — PostgreSQL 15 container
4. **Package** — builds JAR artifact (main/develop branches)
5. **Deploy to Staging** — smoke tests + team notification (main branch only)

---

## Technologies

| Category | Technology | Version |
|---|---|---|
| Language | Java | 17 |
| Framework | Spring Boot | 3.2.0 |
| Security | Spring Security + JWT (JJWT) | 6.2 / 0.12.3 |
| Persistence | Spring Data JPA / Hibernate | 6.4 |
| Database (dev) | H2 In-Memory | 2.2 |
| Database (prod) | PostgreSQL | 15 |
| Testing | JUnit 5 + Mockito + AssertJ | 5.10.1 |
| Coverage | JaCoCo | 0.8.11 |
| Build | Maven | 3.9 |
| API Docs | SpringDoc OpenAPI (Swagger) | 2.3.0 |
| CI/CD | GitHub Actions | — |

---

## Functional Requirements Coverage

| FR | Requirement | Implemented |
|---|---|---|
| FR-01 | Register with email or phone | ✅ `POST /api/auth/register` |
| FR-02 | View available events | ✅ `GET /api/events` |
| FR-03 | Search & filter events | ✅ `GET /api/events/search`, `/filter` |
| FR-04 | Reserve tickets | ✅ `POST /api/reservations` |
| FR-05 | Cancel reservation | ✅ `DELETE /api/reservations/{id}` |
| FR-06 | Email/SMS confirmation | ✅ `NotificationService` |
| FR-07 | Admin: add event | ✅ `POST /api/events` |
| FR-08 | Admin: edit event | ✅ `PUT /api/events/{id}` |
| FR-09 | Admin: cancel event | ✅ `DELETE /api/events/{id}` |
