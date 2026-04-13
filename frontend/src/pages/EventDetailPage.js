import React, { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import {
  Calendar, MapPin, Users, Ticket, ArrowLeft,
  CheckCircle, Minus, Plus, User
} from 'lucide-react';
import { format } from 'date-fns';
import { eventsAPI, reservationsAPI } from '../services/api';
import { useAuth } from '../context/AuthContext';
import { PageSpinner, CategoryBadge, StatusBadge, Alert, Modal } from '../components/common/UI';
import toast from 'react-hot-toast';

export default function EventDetailPage() {
  const { id }       = useParams();
  const navigate     = useNavigate();
  const { isLoggedIn, user } = useAuth();

  const [event,     setEvent]     = useState(null);
  const [loading,   setLoading]   = useState(true);
  const [error,     setError]     = useState('');
  const [tickets,   setTickets]   = useState(1);
  const [booking,   setBooking]   = useState(false);
  const [bookError, setBookError] = useState('');
  const [success,   setSuccess]   = useState(null); // reservation object

  useEffect(() => {
    eventsAPI.getById(id)
      .then(r => setEvent(r.data))
      .catch(() => setError('Event not found.'))
      .finally(() => setLoading(false));
  }, [id]);

  const totalPrice = event ? (event.price * tickets).toFixed(2) : '0.00';
  const canBook    = event?.status === 'ACTIVE' && event?.availableSeats > 0;
  const maxTickets = Math.min(event?.availableSeats || 0, 10);

  const handleBook = async () => {
    if (!isLoggedIn) { navigate('/login', { state: { from: { pathname: `/events/${id}` } } }); return; }
    setBooking(true); setBookError('');
    try {
      const res = await reservationsAPI.create({ eventId: Number(id), numTickets: tickets });
      setSuccess(res.data);
      setEvent(ev => ({ ...ev, availableSeats: ev.availableSeats - tickets }));
      toast.success('Tickets reserved! Check your email for confirmation.');
    } catch (err) {
      setBookError(err.response?.data?.message || 'Booking failed. Please try again.');
    } finally { setBooking(false); }
  };

  if (loading) return <PageSpinner />;
  if (error)   return (
    <div className="page-container">
      <Alert type="error" message={error} />
      <Link to="/events" className="btn-secondary mt-4 inline-flex items-center gap-2 text-sm">
        <ArrowLeft className="w-4 h-4" /> Back to Events
      </Link>
    </div>
  );

  const fillPct = Math.round(((event.totalSeats - event.availableSeats) / event.totalSeats) * 100);

  return (
    <div className="page-container">
      {/* Back */}
      <Link to="/events" className="inline-flex items-center gap-1.5 text-sm text-gray-500
                                    hover:text-gray-900 mb-6 transition-colors">
        <ArrowLeft className="w-4 h-4" /> Back to Events
      </Link>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        {/* Left: Event details */}
        <div className="lg:col-span-2 space-y-6">
          {/* Header card */}
          <div className="card overflow-hidden">
            <div className="h-4 bg-gradient-to-r from-blue-500 to-blue-700" />
            <div className="p-6">
              <div className="flex items-center gap-3 mb-4">
                <CategoryBadge category={event.category} />
                {event.status !== 'ACTIVE' && <StatusBadge status={event.status} />}
              </div>
              <h1 className="text-3xl font-bold text-gray-900 mb-4">{event.name}</h1>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 text-sm text-gray-700">
                <div className="flex items-start gap-3">
                  <Calendar className="w-5 h-5 text-blue-500 flex-shrink-0 mt-0.5" />
                  <div>
                    <p className="font-medium text-gray-900">Date & Time</p>
                    <p>{format(new Date(event.eventDate), 'EEEE, MMMM d, yyyy')}</p>
                    <p>{format(new Date(event.eventDate), 'h:mm a')}</p>
                  </div>
                </div>
                <div className="flex items-start gap-3">
                  <MapPin className="w-5 h-5 text-blue-500 flex-shrink-0 mt-0.5" />
                  <div>
                    <p className="font-medium text-gray-900">Location</p>
                    <p>{event.location}</p>
                  </div>
                </div>
                <div className="flex items-start gap-3">
                  <Users className="w-5 h-5 text-blue-500 flex-shrink-0 mt-0.5" />
                  <div>
                    <p className="font-medium text-gray-900">Availability</p>
                    <p>{event.availableSeats} of {event.totalSeats} seats left</p>
                  </div>
                </div>
                <div className="flex items-start gap-3">
                  <User className="w-5 h-5 text-blue-500 flex-shrink-0 mt-0.5" />
                  <div>
                    <p className="font-medium text-gray-900">Organizer</p>
                    <p>{event.organizerName || 'GoTix'}</p>
                  </div>
                </div>
              </div>

              {/* Seat fill bar */}
              <div className="mt-6">
                <div className="flex justify-between text-xs text-gray-500 mb-1">
                  <span>{fillPct}% booked</span>
                  <span>{event.availableSeats} seats remaining</span>
                </div>
                <div className="h-2 bg-gray-100 rounded-full overflow-hidden">
                  <div
                    className={`h-full rounded-full transition-all ${
                      fillPct > 85 ? 'bg-red-400' : fillPct > 60 ? 'bg-yellow-400' : 'bg-green-400'
                    }`}
                    style={{ width: `${fillPct}%` }}
                  />
                </div>
              </div>
            </div>
          </div>

          {/* Description */}
          {event.description && (
            <div className="card p-6">
              <h2 className="text-lg font-semibold text-gray-900 mb-3">About This Event</h2>
              <p className="text-gray-600 leading-relaxed whitespace-pre-line">{event.description}</p>
            </div>
          )}
        </div>

        {/* Right: Booking widget */}
        <div className="lg:col-span-1">
          <div className="card p-6 sticky top-24">
            <div className="text-center mb-6">
              <p className="text-sm text-gray-500">Price per ticket</p>
              <p className="text-4xl font-bold text-gray-900">${event.price?.toFixed(2)}</p>
            </div>

            {canBook ? (
              <>
                {/* Ticket quantity */}
                <div className="mb-5">
                  <label className="label text-center block mb-3">Number of Tickets</label>
                  <div className="flex items-center justify-center gap-4">
                    <button
                      onClick={() => setTickets(t => Math.max(1, t - 1))}
                      className="w-9 h-9 rounded-full border border-gray-300 flex items-center
                                 justify-center hover:bg-gray-50 transition-colors disabled:opacity-40"
                      disabled={tickets <= 1}
                    >
                      <Minus className="w-4 h-4" />
                    </button>
                    <span className="text-2xl font-bold w-8 text-center">{tickets}</span>
                    <button
                      onClick={() => setTickets(t => Math.min(maxTickets, t + 1))}
                      className="w-9 h-9 rounded-full border border-gray-300 flex items-center
                                 justify-center hover:bg-gray-50 transition-colors disabled:opacity-40"
                      disabled={tickets >= maxTickets}
                    >
                      <Plus className="w-4 h-4" />
                    </button>
                  </div>
                  <p className="text-xs text-center text-gray-400 mt-2">Max {maxTickets} per booking</p>
                </div>

                {/* Total */}
                <div className="bg-blue-50 rounded-xl p-4 mb-4 text-center">
                  <p className="text-sm text-blue-600 font-medium">Total</p>
                  <p className="text-3xl font-bold text-blue-700">${totalPrice}</p>
                  <p className="text-xs text-blue-400">{tickets} × ${event.price?.toFixed(2)}</p>
                </div>

                <Alert type="error" message={bookError} className="mb-3" />

                <button
                  onClick={handleBook}
                  disabled={booking}
                  className="btn-primary w-full py-3 text-base flex items-center justify-center gap-2"
                >
                  <Ticket className="w-5 h-5" />
                  {booking ? 'Reserving…' : isLoggedIn ? 'Reserve Tickets' : 'Sign In to Book'}
                </button>

                {!isLoggedIn && (
                  <p className="text-xs text-center text-gray-400 mt-2">
                    You'll be redirected to sign in
                  </p>
                )}
              </>
            ) : (
              <div className="text-center py-4">
                <StatusBadge status={event.status === 'CANCELLED' ? 'CANCELLED' : 'SOLD_OUT'} />
                <p className="text-sm text-gray-500 mt-3">
                  {event.status === 'CANCELLED'
                    ? 'This event has been cancelled.'
                    : 'All tickets have been sold for this event.'}
                </p>
              </div>
            )}

            <div className="mt-4 pt-4 border-t border-gray-100 text-xs text-gray-400 space-y-1">
              <p>✓ Instant confirmation email</p>
              <p>✓ Free cancellation (see policy)</p>
              <p>✓ Secure payment processing</p>
            </div>
          </div>
        </div>
      </div>

      {/* Success modal */}
      <Modal open={!!success} onClose={() => setSuccess(null)} title="🎉 Booking Confirmed!">
        {success && (
          <div className="space-y-4">
            <div className="flex items-center justify-center">
              <div className="w-16 h-16 bg-green-100 rounded-full flex items-center justify-center">
                <CheckCircle className="w-8 h-8 text-green-600" />
              </div>
            </div>
            <div className="text-center">
              <p className="text-lg font-semibold text-gray-900">{event.name}</p>
              <p className="text-sm text-gray-500">{format(new Date(event.eventDate), 'EEE, MMM d yyyy · h:mm a')}</p>
            </div>
            <div className="bg-gray-50 rounded-xl p-4 space-y-2 text-sm">
              <div className="flex justify-between">
                <span className="text-gray-500">Confirmation Code</span>
                <span className="font-mono font-bold text-blue-600">{success.confirmationCode}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-500">Tickets</span>
                <span className="font-medium">{success.numTickets}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-500">Total Paid</span>
                <span className="font-bold text-gray-900">${success.totalPrice?.toFixed(2)}</span>
              </div>
            </div>
            <p className="text-xs text-center text-gray-400">
              A confirmation has been sent to your email / phone.
            </p>
            <div className="flex gap-3">
              <button onClick={() => setSuccess(null)} className="btn-secondary flex-1 text-sm">Close</button>
              <button onClick={() => navigate('/my-reservations')} className="btn-primary flex-1 text-sm">
                View My Tickets
              </button>
            </div>
          </div>
        )}
      </Modal>
    </div>
  );
}
