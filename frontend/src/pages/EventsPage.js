import React, { useState, useEffect, useCallback } from 'react';
import { useSearchParams } from 'react-router-dom';
import { CalendarDays } from 'lucide-react';
import { eventsAPI } from '../services/api';
import EventCard from '../components/events/EventCard';
import EventFilter from '../components/events/EventFilter';
import { PageSpinner, EmptyState, Alert } from '../components/common/UI';

export default function EventsPage() {
  const [searchParams] = useSearchParams();
  const [events,  setEvents]  = useState([]);
  const [loading, setLoading] = useState(true);
  const [error,   setError]   = useState('');
  const [label,   setLabel]   = useState('All Available Events');

  const loadAll = useCallback(async () => {
    setLoading(true); setError('');
    try {
      const res = await eventsAPI.getAll();
      setEvents(res.data);
      setLabel('All Available Events');
    } catch {
      setError('Failed to load events. Please try again.');
    } finally { setLoading(false); }
  }, []);

  // Handle ?category= from landing page links
  useEffect(() => {
    const cat = searchParams.get('category');
    if (cat) {
      handleFilter({ category: cat });
    } else {
      loadAll();
    }
  }, []); // eslint-disable-line

  const handleSearch = async (keyword) => {
    setLoading(true); setError('');
    try {
      const res = await eventsAPI.search(keyword);
      setEvents(res.data);
      setLabel(`Search results for "${keyword}"`);
    } catch {
      setError('Search failed. Please try again.');
    } finally { setLoading(false); }
  };

  const handleFilter = async (params) => {
    if (!Object.values(params).some(Boolean)) { loadAll(); return; }
    setLoading(true); setError('');
    try {
      const res = await eventsAPI.filter(params);
      setEvents(res.data);
      const parts = [];
      if (params.category) parts.push(params.category);
      if (params.location)  parts.push(params.location);
      setLabel(parts.length ? `Filtered: ${parts.join(' · ')}` : 'Filtered Events');
    } catch {
      setError('Filter failed. Please try again.');
    } finally { setLoading(false); }
  };

  return (
    <div className="page-container">
      <div className="mb-6">
        <h1 className="section-title mb-1">Events</h1>
        <p className="text-gray-500 text-sm">Discover and book tickets for upcoming events</p>
      </div>

      <EventFilter onFilter={handleFilter} onSearch={handleSearch} />

      <Alert type="error" message={error} className="mb-4" />

      {loading ? (
        <PageSpinner />
      ) : (
        <>
          <div className="flex items-center justify-between mb-4">
            <p className="text-sm font-medium text-gray-700">{label}</p>
            <p className="text-sm text-gray-400">{events.length} event{events.length !== 1 ? 's' : ''}</p>
          </div>

          {events.length === 0 ? (
            <EmptyState
              icon={CalendarDays}
              title="No events found"
              description="Try adjusting your search or filters to discover more events."
              action={
                <button onClick={loadAll} className="btn-primary text-sm">
                  View All Events
                </button>
              }
            />
          ) : (
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-5">
              {events.map(ev => <EventCard key={ev.id} event={ev} />)}
            </div>
          )}
        </>
      )}
    </div>
  );
}
