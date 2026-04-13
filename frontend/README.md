# GoTix — React Frontend

## Quick Start

```bash
# Install dependencies
npm install

# Start development server (proxies /api → localhost:8080)
npm start

# Open in browser
open http://localhost:3000

# Production build
npm run build
```

## Prerequisites

Make sure the **Spring Boot backend** is running on `http://localhost:8080` before starting the frontend.

## Pages & Routes

| Route | Access | Description |
|---|---|---|
| `/` | Public | Landing page with hero, categories, features |
| `/events` | Public | Browse all events with search & filter |
| `/events/:id` | Public | Event detail + ticket booking widget |
| `/login` | Public | Sign in with email + password |
| `/register` | Public | Create account with email or phone |
| `/my-reservations` | Auth | View & cancel your tickets |
| `/profile` | Auth | View & edit profile |
| `/admin` | Admin | Dashboard with stats |
| `/admin/events` | Admin | Manage all events (table view) |
| `/admin/events/new` | Admin | Create new event form |
| `/admin/events/:id/edit` | Admin | Edit existing event |
| `/admin/users` | Admin | View & deactivate user accounts |

## Project Structure

```
src/
├── context/
│   └── AuthContext.js       # Global auth state, JWT storage
├── services/
│   └── api.js               # Axios instance + all API calls
├── components/
│   ├── common/
│   │   ├── Navbar.js        # Responsive navigation bar
│   │   ├── Footer.js        # Site footer
│   │   └── UI.js            # Spinner, Modal, Badge, Alert, EmptyState…
│   ├── events/
│   │   ├── EventCard.js     # Event grid card with seat fill bar
│   │   └── EventFilter.js   # Search + advanced filter bar
│   └── reservations/
│       └── ReservationCard.js  # Reservation card with cancel flow
├── pages/
│   ├── LandingPage.js       # Hero, categories, features, CTA
│   ├── LoginPage.js         # Login form with demo credentials
│   ├── RegisterPage.js      # Register with email or phone toggle
│   ├── EventsPage.js        # Events grid with live search/filter
│   ├── EventDetailPage.js   # Full event detail + booking widget
│   ├── MyReservationsPage.js # Tabbed reservations (All/Active/Cancelled)
│   ├── ProfilePage.js       # View/edit own profile
│   ├── AdminDashboard.js    # Stats cards + recent events table
│   ├── AdminEventsPage.js   # Full events table with admin actions
│   ├── AdminEventFormPage.js # Create/edit event form
│   ├── AdminUsersPage.js    # Users table with search + deactivate
│   └── NotFoundPage.js      # 404 page
└── App.js                   # Router (public / protected / admin routes)
```

## Features

- **JWT auth** — token stored in localStorage, auto-attached on every API call
- **Auto-logout** — 401 response clears token and redirects to login
- **Role-based UI** — admin links and pages hidden from customers
- **Optimistic updates** — reservation cancellations update UI instantly
- **Seat fill bar** — visual progress bar on every event card and detail page
- **Booking modal** — success confirmation with confirmation code after booking
- **Responsive** — mobile-first layout with hamburger menu
- **Toast notifications** — success/error toasts for all actions
