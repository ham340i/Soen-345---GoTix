import React, { useState, useEffect, useCallback } from 'react';
import { Link } from 'react-router-dom';
import { Plus, Edit2, XCircle, Eye, CalendarDays } from 'lucide-react';
import { format } from 'date-fns';
import { eventsAPI } from '../services/api';
import { PageSpinner, EmptyState, StatusBadge, CategoryBadge, ConfirmDialog, Alert } from '../components/common/UI';
import toast from 'react-hot-toast';

export default function AdminEventsPage() {
  const [events,  setEvents]  = useState([]);
  const [loading, setLoading] = useState(true);
  const [error,   setError]   = useState('');
  const [cancel,  setCancel]  = useState(null); // event to cancel
  const [cancelling, setCancelling] = useState(false);

  const load = useCallback(async () => {
    setLoading(true); setError('');
    try {
      const res = await eventsAPI.getAll();
      setEvents(res.data);
    } catch {
      setError('Failed to load events.');
    } finally { setLoading(false); }
  }, []);

  useEffect(() => { load(); }, [load]);

  const handleCancel = async () => {
    setCancelling(true);
    try {
      await eventsAPI.cancel(cancel.id);
      setEvents(prev => prev.map(e => e.id === cancel.id ? { ...e, status: 'CANCELLED' } : e));
      toast.success(`"${cancel.name}" cancelled and all ticket holders notified.`);
      setCancel(null);
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to cancel event.');
    } finally { setCancelling(false); }
  };

  return (
    <div className="page-container">
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="section-title mb-1">Manage Events</h1>
          <p className="text-gray-500 text-sm">{events.length} total events</p>
        </div>
        <Link to="/admin/events/new" className="btn-primary flex items-center gap-2 text-sm">
          <Plus className="w-4 h-4" /> New Event
        </Link>
      </div>

      <Alert type="error" message={error} className="mb-4" />

      {loading ? <PageSpinner /> : events.length === 0 ? (
        <EmptyState
          icon={CalendarDays}
          title="No events yet"
          description="Create your first event to get started."
          action={<Link to="/admin/events/new" className="btn-primary text-sm">Create Event</Link>}
        />
      ) : (
        <div className="card overflow-hidden">
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr className="bg-gray-50 text-left border-b border-gray-200">
                  {['Event', 'Category', 'Date', 'Location', 'Seats', 'Price', 'Status', 'Actions'].map(h => (
                    <th key={h} className="px-4 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wider whitespace-nowrap">
                      {h}
                    </th>
                  ))}
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100">
                {events.map(ev => {
                  const isCancelled = ev.status === 'CANCELLED';
                  const booked = ev.totalSeats - ev.availableSeats;
                  return (
                    <tr key={ev.id} className={`hover:bg-gray-50 transition-colors ${isCancelled ? 'opacity-60' : ''}`}>
                      <td className="px-4 py-3">
                        <p className="font-medium text-gray-900 max-w-[180px] truncate">{ev.name}</p>
                        {ev.organizerName && <p className="text-xs text-gray-400">{ev.organizerName}</p>}
                      </td>
                      <td className="px-4 py-3"><CategoryBadge category={ev.category} /></td>
                      <td className="px-4 py-3 text-gray-600 whitespace-nowrap">
                        {format(new Date(ev.eventDate), 'MMM d, yyyy')}
                        <br />
                        <span className="text-xs text-gray-400">{format(new Date(ev.eventDate), 'h:mm a')}</span>
                      </td>
                      <td className="px-4 py-3 text-gray-600 max-w-[140px] truncate">{ev.location}</td>
                      <td className="px-4 py-3">
                        <p className="text-gray-900 font-medium">{booked}/{ev.totalSeats}</p>
                        <div className="h-1 bg-gray-100 rounded-full mt-1 w-14">
                          <div
                            className="h-full bg-blue-400 rounded-full"
                            style={{ width: `${(booked / ev.totalSeats) * 100}%` }}
                          />
                        </div>
                      </td>
                      <td className="px-4 py-3 font-medium text-gray-900">${ev.price?.toFixed(2)}</td>
                      <td className="px-4 py-3"><StatusBadge status={ev.status} /></td>
                      <td className="px-4 py-3">
                        <div className="flex items-center gap-1">
                          <Link to={`/events/${ev.id}`} title="View"
                            className="p-1.5 rounded text-gray-400 hover:text-blue-600 hover:bg-blue-50 transition-colors">
                            <Eye className="w-4 h-4" />
                          </Link>
                          {!isCancelled && (
                            <>
                              <Link to={`/admin/events/${ev.id}/edit`} title="Edit"
                                className="p-1.5 rounded text-gray-400 hover:text-green-600 hover:bg-green-50 transition-colors">
                                <Edit2 className="w-4 h-4" />
                              </Link>
                              <button title="Cancel" onClick={() => setCancel(ev)}
                                className="p-1.5 rounded text-gray-400 hover:text-red-600 hover:bg-red-50 transition-colors">
                                <XCircle className="w-4 h-4" />
                              </button>
                            </>
                          )}
                        </div>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        </div>
      )}

      <ConfirmDialog
        open={!!cancel}
        onClose={() => setCancel(null)}
        onConfirm={handleCancel}
        title="Cancel Event"
        message={`Are you sure you want to cancel "${cancel?.name}"? All ticket holders will be notified and their reservations cancelled automatically.`}
        confirmLabel={cancelling ? 'Cancelling…' : 'Yes, Cancel Event'}
        danger
      />
    </div>
  );
}
