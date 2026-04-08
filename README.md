# GoTix — Cloud-Based Ticket Reservation System
### SOEN 345 · Software Testing, Verification & Quality Assurance · Concordia University

Full-stack ticket reservation platform: Java 17 + Spring Boot 3.2 backend, React 18 + Tailwind CSS frontend.

---

## Project Structure

```
GoTix/
├── backend/                  ← Spring Boot REST API (Java 17, Maven)
│   ├── src/main/java/com/ticketreservation/
│   │   ├── config/           JwtUtil, JwtAuthFilter, SecurityConfig, CorsConfig, DataSeeder
│   │   ├── controller/       AuthController, EventController, ReservationController, UserController
│   │   ├── dto/              Request/Response DTOs
│   │   ├── exception/        Typed exceptions + GlobalExceptionHandler
│   │   ├── model/            User, Event, Reservation (JPA)
│   │   ├── repository/       Spring Data JPA repos
│   │   └── service/          UserService, EventService, ReservationService, NotificationService
│   ├── src/test/             55 JUnit 5 tests (unit + MockMvc)
│   ├── .github/workflows/    5-stage GitHub Actions CI/CD
│   ├── Dockerfile
│   └── pom.xml
│
├── frontend/                 ← React 18 SPA (Tailwind CSS, Axios, React Router)
│   ├── src/
│   │   ├── components/       Navbar, Footer, EventCard, EventFilter, ReservationCard, UI
│   │   ├── context/          AuthContext (JWT state management)
│   │   ├── pages/            12 pages: Landing, Events, Detail, Auth, Reservations, Admin x4
│   │   └── services/         api.js (Axios instance + all API calls)
│   ├── public/
│   ├── Dockerfile
│   ├── nginx.conf
│   └── package.json
│
├── docker-compose.yml        ← One-command startup (DB + Backend + Frontend)
├── .gitignore
└── README.md
```

---

## Quick Start

### Option A — Docker (one command, recommended)

Requires: Docker + Docker Compose

```bash
cd GoTix
docker-compose up --build
```

| Service | URL |
|---------|-----|
| Frontend | http://localhost:3000 |
| Backend API | http://localhost:8080/api |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| H2 Console (dev) | http://localhost:8080/h2-console |

### Option B — Run locally (no Docker)

**Backend** (requires Java 17 + Maven 3.8+):
```bash
cd backend
mvn spring-boot:run
```

**Frontend** (requires Node 18+):
```bash
cd frontend
npm install
npm start
```

---

## Demo Credentials

| Role     | Email               | Password   |
|----------|---------------------|------------|
| Admin    | admin@demo.com      | admin1234  |
| Customer | customer@demo.com   | demo1234   |

The DataSeeder creates these accounts + 7 sample events automatically on first startup.

---

## API Reference

### Public Endpoints (no auth)
| Method | Path | Description |
|--------|------|-------------|
| POST | /api/auth/register | Register (email or phone) |
| POST | /api/auth/login | Login → returns JWT |
| GET | /api/events | List active events |
| GET | /api/events/{id} | Event detail |
| GET | /api/events/search?keyword= | Full-text search |
| GET | /api/events/filter?category=&location=&startDate=&endDate= | Filter |

### Authenticated (Customer + Admin)
| Method | Path | Description |
|--------|------|-------------|
| POST | /api/reservations | Reserve tickets |
| GET | /api/reservations/my | My reservations |
| GET | /api/reservations/{id} | Get reservation |
| GET | /api/reservations/code/{code} | Lookup by confirmation code |
| DELETE | /api/reservations/{id} | Cancel reservation |
| GET | /api/users/me | My profile |
| PUT | /api/users/me | Update profile |

### Admin Only
| Method | Path | Description |
|--------|------|-------------|
| POST | /api/events | Create event |
| PUT | /api/events/{id} | Edit event |
| DELETE | /api/events/{id} | Cancel event (notifies all ticket holders) |
| GET | /api/reservations/event/{eventId} | All reservations for event |
| GET | /api/users | All users |
| DELETE | /api/users/{id} | Deactivate user |

---

## Functional Requirements

| FR | Requirement | Implemented |
|----|-------------|-------------|
| FR-01 | Register with email or phone | ✅ POST /api/auth/register |
| FR-02 | View available events | ✅ GET /api/events |
| FR-03 | Search & filter by date/location/category | ✅ /search + /filter |
| FR-04 | Reserve tickets | ✅ POST /api/reservations |
| FR-05 | Cancel reservation | ✅ DELETE /api/reservations/{id} |
| FR-06 | Email/SMS confirmation | ✅ NotificationService |
| FR-07 | Admin: add event | ✅ POST /api/events |
| FR-08 | Admin: edit event | ✅ PUT /api/events/{id} |
| FR-09 | Admin: cancel event | ✅ DELETE /api/events/{id} |

## Tech Stack

| | Technology | Version |
|-|-----------|---------|
| Language | Java | 17 |
| Framework | Spring Boot | 3.2 |
| Security | Spring Security + JWT | 6.2 / 0.12.3 |
| ORM | Spring Data JPA / Hibernate | 6.4 |
| Database | H2 (dev) / PostgreSQL (prod) | 15 |
| Frontend | React | 18 |
| Styling | Tailwind CSS | 3.4 |
| Testing | JUnit 5 + Mockito + AssertJ | 5.10.1 |
| Coverage | JaCoCo (≥80% gate) | 0.8.11 |
| CI/CD | GitHub Actions (5 stages) | — |
| Docs | SpringDoc OpenAPI / Swagger | 2.3.0 |
