function showSection(sectionId) {
    document.getElementById('events-section').style.display = 'none';
    document.getElementById(sectionId).style.display = 'block';

    if (sectionId === 'events-section') {
        loadEvents();
    }
}

async function loadEvents() {
    try {
        const response = await fetch('/events');
        const events = await response.json();
        renderEvents(events);
    } catch (error) {
        console.error('Error loading events:', error);
    }
}

async function searchEvents() {
    const date = document.getElementById('search-date').value;
    const location = document.getElementById('search-location').value;
    const category = document.getElementById('search-category').value;

    const params = new URLSearchParams({
        date: date,
        location: location,
        category: category
    });

    try {
        const response = await fetch(`/events/search?${params.toString()}`);
        const events = await response.json();
        renderEvents(events);
    } catch (error) {
        console.error('Error searching events:', error);
    }
}

function renderEvents(events) {
    const list = document.getElementById('events-list');
    list.innerHTML = '';

    if (events.length === 0) {
        list.innerHTML = '<p>No events found.</p>';
        return;
    }

    events.forEach(event => {
        const card = document.createElement('div');
        card.className = 'event-card';
        card.innerHTML = `
            <h3>${event.title}</h3>
            <p><strong>Date:</strong> ${event.date}</p>
            <p><strong>Location:</strong> ${event.location}</p>
            <span class="category-badge">${event.category}</span>
        `;
        list.appendChild(card);
    });
}

// Initial load
window.onload = loadEvents;
