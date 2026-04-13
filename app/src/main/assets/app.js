// ── State ──────────────────────────────────────────────────────────────────
let activeEventId   = null;
let activeEventTitle = '';
let activeEventSeats = 0;

// ── Tab navigation ─────────────────────────────────────────────────────────
function showTab(name) {
    ['events', 'reservations', 'register'].forEach(t => {
        document.getElementById('tab-' + t).style.display = (t === name) ? 'block' : 'none';
    });
    if (name === 'events') loadEvents();
}

// ── Events ─────────────────────────────────────────────────────────────────
async function loadEvents() {
    try {
        const res  = await fetch('/events');
        const data = await res.json();
        renderEvents(data);
    } catch (e) {
        console.error('loadEvents:', e);
    }
}

async function searchEvents() {
    const date     = document.getElementById('search-date').value;
    const location = document.getElementById('search-location').value;
    const category = document.getElementById('search-category').value;
    const params   = new URLSearchParams({ date, location, category });
    try {
        const res  = await fetch('/events/search?' + params);
        const data = await res.json();
        renderEvents(data);
    } catch (e) {
        console.error('searchEvents:', e);
    }
}

function renderEvents(events) {
    const list = document.getElementById('events-list');
    if (!events || events.length === 0) {
        list.innerHTML = '<p class="muted">No events found.</p>';
        return;
    }
    list.innerHTML = events.map(e => `
        <div class="event-card">
            <h3>${e.title}</h3>
            <p><strong>Date:</strong> ${e.date}</p>
            <p><strong>Location:</strong> ${e.location}</p>
            <p><strong>Seats available:</strong> ${e.availableSeats} / ${e.totalSeats}</p>
            <span class="category-badge">${e.category}</span>
            <br>
            <button class="btn-book" onclick="openBookModal('${e.id}','${e.title.replace(/'/g,"\\'")}',${e.availableSeats})">
                Book tickets
            </button>
        </div>
    `).join('');
}

// ── Book modal ─────────────────────────────────────────────────────────────
function openBookModal(eventId, title, seats) {
    activeEventId    = eventId;
    activeEventTitle = title;
    activeEventSeats = seats;
    document.getElementById('modal-title').textContent = 'Reserve: ' + title;
    document.getElementById('modal-seats').textContent = seats + ' seats remaining';
    document.getElementById('book-user').value = '';
    document.getElementById('book-qty').value  = '1';
    setMsg('book-msg', '', '');
    document.getElementById('book-modal').style.display = 'flex';
}

function closeModal() {
    document.getElementById('book-modal').style.display = 'none';
}

async function confirmBook() {
    const userId     = document.getElementById('book-user').value.trim();
    const numTickets = parseInt(document.getElementById('book-qty').value, 10);

    if (!userId) {
        setMsg('book-msg', 'Please enter your User ID.', 'error'); return;
    }
    if (!numTickets || numTickets < 1) {
        setMsg('book-msg', 'Number of tickets must be at least 1.', 'error'); return;
    }
    if (numTickets > activeEventSeats) {
        setMsg('book-msg', 'Not enough seats available.', 'error'); return;
    }

    try {
        const res  = await fetch('/reservations', {
            method:  'POST',
            headers: { 'Content-Type': 'application/json' },
            body:    JSON.stringify({ userId, eventId: activeEventId, numTickets })
        });
        const data = await res.json();
        if (res.ok && data.status === 'ok') {
            setMsg('book-msg',
                'Booking confirmed! Reference: ' + data.reservationId.slice(0, 8).toUpperCase(),
                'success');
            loadEvents(); // Refresh seat counts
        } else {
            setMsg('book-msg', data.error || 'Booking failed.', 'error');
        }
    } catch (e) {
        setMsg('book-msg', 'Network error. Try again.', 'error');
    }
}

// ── My Reservations ────────────────────────────────────────────────────────
async function loadReservations() {
    const userId = document.getElementById('res-user-id').value.trim();
    if (!userId) {
        setMsg('res-msg', 'Please enter your User ID.', 'error'); return;
    }
    setMsg('res-msg', '', '');
    try {
        const res  = await fetch('/reservations?userId=' + encodeURIComponent(userId));
        const data = await res.json();
        renderReservations(data);
    } catch (e) {
        setMsg('res-msg', 'Failed to load tickets.', 'error');
    }
}

function renderReservations(list) {
    const el = document.getElementById('reservations-list');
    if (!list || list.length === 0) {
        el.innerHTML = '<p class="muted">No reservations found.</p>'; return;
    }
    el.innerHTML = list.map(r => `
        <div class="event-card ${r.status === 'CANCELLED' ? 'cancelled' : ''}">
            <h3>${r.eventTitle}</h3>
            <p><strong>Tickets:</strong> ${r.numTickets}</p>
            <p><strong>Reference:</strong> ${r.id.slice(0, 8).toUpperCase()}</p>
            <span class="status-badge ${r.status === 'CONFIRMED' ? 'badge-confirmed' : 'badge-cancelled'}">
                ${r.status}
            </span>
            ${r.status === 'CONFIRMED' ? `
            <br>
            <button class="btn-cancel" onclick="cancelReservation('${r.id}')">
                Cancel reservation
            </button>` : ''}
        </div>
    `).join('');
}

async function cancelReservation(reservationId) {
    if (!confirm('Cancel this reservation?')) return;
    try {
        const res  = await fetch('/reservations/' + reservationId, { method: 'DELETE' });
        const data = await res.json();
        if (res.ok && data.status === 'ok') {
            setMsg('res-msg', 'Reservation cancelled successfully.', 'success');
            loadReservations();
        } else {
            setMsg('res-msg', data.error || 'Cancellation failed.', 'error');
        }
    } catch (e) {
        setMsg('res-msg', 'Network error. Try again.', 'error');
    }
}

// ── Register ───────────────────────────────────────────────────────────────
async function register() {
    const email = document.getElementById('reg-email').value.trim();
    const phone = document.getElementById('reg-phone').value.trim();

    if (!email && !phone) {
        setMsg('reg-msg', 'Please provide an email or phone number.', 'error'); return;
    }

    try {
        const res  = await fetch('/users/register', {
            method:  'POST',
            headers: { 'Content-Type': 'application/json' },
            body:    JSON.stringify({ email, phone })
        });
        const data = await res.json();
        if (res.ok && data.status === 'ok') {
            setMsg('reg-msg',
                'Account created! Your User ID: ' + data.userId.slice(0, 8).toUpperCase(),
                'success');
            document.getElementById('reg-email').value = '';
            document.getElementById('reg-phone').value = '';
        } else {
            setMsg('reg-msg', data.error || 'Registration failed.', 'error');
        }
    } catch (e) {
        setMsg('reg-msg', 'Network error. Try again.', 'error');
    }
}

// ── Helpers ────────────────────────────────────────────────────────────────
function setMsg(id, text, type) {
    const el = document.getElementById(id);
    el.textContent  = text;
    el.className    = 'message' + (type ? ' ' + type : '');
}

// ── Boot ───────────────────────────────────────────────────────────────────
window.onload = loadEvents;
