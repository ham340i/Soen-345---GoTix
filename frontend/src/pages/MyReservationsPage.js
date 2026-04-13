import React, { useState, useEffect, useCallback } from 'react';
import { Link } from 'react-router-dom';
import { BookMarked, TicketCheck, XCircle } from 'lucide-react';
import { reservationsAPI } from '../services/api';
import ReservationCard from '../components/reservations/ReservationCard';
import { PageSpinner, EmptyState, Alert } from '../components/common/UI';

const TABS = [
  { key: 'all',       label: 'All',       icon: BookMarked  },
  { key: 'CONFIRMED', label: 'Active',    icon: TicketCheck },
  { key: 'CANCELLED', label: 'Cancelled', icon: XCircle     },
];

export default function MyReservationsPage() {
  const [reservations, setReservations] = useState([]);
  const [loading,      setLoading]      = useState(true);
  const [error,        setError]        = useState('');
  const [tab,          setTab]          = useState('all');

  const load = useCallback(async () => {
    setLoading(true); setError('');
    try {
      const res = await reservationsAPI.getMy();
      setReservations(res.data);
    } catch {
      setError('Failed to load your reservations.');
    } finally { setLoading(false); }
  }, []);

  useEffect(() => { load(); }, [load]);

  const handleCancelled = (id) => {
    setReservations(prev =>
      prev.map(r => r.id === id ? { ...r, status: 'CANCELLED' } : r)
    );
  };

  const filtered = tab === 'all' ? reservations : reservations.filter(r => r.status === tab);

  const counts = {
    all:       reservations.length,
    CONFIRMED: reservations.filter(r => r.status === 'CONFIRMED').length,
    CANCELLED: reservations.filter(r => r.status === 'CANCELLED').length,
  };

  return (
    <div className="page-container max-w-4xl">
      <div className="mb-6">
        <h1 className="section-title mb-1">My Tickets</h1>
        <p className="text-gray-500 text-sm">View and manage your event reservations</p>
      </div>

      {/* Tabs */}
      <div className="flex gap-1 bg-gray-100 rounded-xl p-1 mb-6 w-fit">
        {TABS.map(t => (
          <button key={t.key} onClick={() => setTab(t.key)}
            className={`flex items-center gap-1.5 px-4 py-2 rounded-lg text-sm font-medium transition-all ${
              tab === t.key
                ? 'bg-white text-gray-900 shadow-sm'
                : 'text-gray-500 hover:text-gray-700'
            }`}>
            <t.icon className="w-4 h-4" />
            {t.label}
            <span className={`text-xs px-1.5 py-0.5 rounded-full ${
              tab === t.key ? 'bg-blue-100 text-blue-600' : 'bg-gray-200 text-gray-500'
            }`}>
              {counts[t.key]}
            </span>
          </button>
        ))}
      </div>

      <Alert type="error" message={error} className="mb-4" />

      {loading ? (
        <PageSpinner />
      ) : filtered.length === 0 ? (
        <EmptyState
          icon={BookMarked}
          title={tab === 'all' ? "No reservations yet" : `No ${tab.toLowerCase()} reservations`}
          description={tab === 'all' ? "Start exploring events and book your first ticket!" : undefined}
          action={
            tab === 'all' && (
              <Link to="/events" className="btn-primary text-sm">Browse Events</Link>
            )
          }
        />
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          {filtered.map(r => (
            <ReservationCard key={r.id} reservation={r} onCancelled={handleCancelled} />
          ))}
        </div>
      )}
    </div>
  );
}
