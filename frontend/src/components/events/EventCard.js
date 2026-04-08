import React from 'react';
import { Link } from 'react-router-dom';
import { MapPin, Calendar, Users, Ticket } from 'lucide-react';
import { format } from 'date-fns';
import { CategoryBadge, StatusBadge } from '../common/UI';

const categoryEmoji = {
  MOVIE: '🎬', CONCERT: '🎵', SPORTS: '⚽', TRAVEL: '✈️',
  THEATER: '🎭', CONFERENCE: '💼', OTHER: '🎪'
};

export default function EventCard({ event }) {
  const isSoldOut   = event.availableSeats === 0;
  const isCancelled = event.status === 'CANCELLED';
  const fillPct     = Math.round(((event.totalSeats - event.availableSeats) / event.totalSeats) * 100);

  return (
    <div className={`card flex flex-col hover:shadow-md transition-shadow duration-200
                     ${isCancelled ? 'opacity-60' : ''}`}>
      {/* Header band */}
      <div className="h-3 bg-gradient-to-r from-blue-500 to-blue-700" />

      <div className="p-5 flex flex-col flex-1">
        {/* Category + status */}
        <div className="flex items-center justify-between mb-3">
          <div className="flex items-center gap-2">
            <span className="text-2xl">{categoryEmoji[event.category] || '🎟️'}</span>
            <CategoryBadge category={event.category} />
          </div>
          {event.status !== 'ACTIVE' && <StatusBadge status={event.status} />}
        </div>

        {/* Title */}
        <h3 className="text-lg font-semibold text-gray-900 mb-1 line-clamp-2 flex-1">
          {event.name}
        </h3>

        {/* Description */}
        {event.description && (
          <p className="text-sm text-gray-500 mb-3 line-clamp-2">{event.description}</p>
        )}

        {/* Details */}
        <div className="space-y-1.5 mb-4 text-sm text-gray-600">
          <div className="flex items-center gap-2">
            <Calendar className="w-4 h-4 text-blue-400 flex-shrink-0" />
            <span>{format(new Date(event.eventDate), 'EEE, MMM d yyyy · h:mm a')}</span>
          </div>
          <div className="flex items-center gap-2">
            <MapPin className="w-4 h-4 text-blue-400 flex-shrink-0" />
            <span className="truncate">{event.location}</span>
          </div>
          <div className="flex items-center gap-2">
            <Users className="w-4 h-4 text-blue-400 flex-shrink-0" />
            <span>{event.availableSeats} / {event.totalSeats} seats available</span>
          </div>
        </div>

        {/* Seat fill bar */}
        <div className="mb-4">
          <div className="h-1.5 bg-gray-100 rounded-full overflow-hidden">
            <div
              className={`h-full rounded-full transition-all ${
                fillPct > 85 ? 'bg-red-400' : fillPct > 60 ? 'bg-yellow-400' : 'bg-green-400'
              }`}
              style={{ width: `${fillPct}%` }}
            />
          </div>
          <p className="text-xs text-gray-400 mt-1">{fillPct}% booked</p>
        </div>

        {/* Price + CTA */}
        <div className="flex items-center justify-between mt-auto pt-3 border-t border-gray-100">
          <div>
            <p className="text-xs text-gray-400">From</p>
            <p className="text-xl font-bold text-gray-900">${event.price?.toFixed(2)}</p>
          </div>
          <Link
            to={`/events/${event.id}`}
            className={`flex items-center gap-1.5 text-sm font-semibold px-4 py-2 rounded-lg transition-colors ${
              isSoldOut || isCancelled
                ? 'bg-gray-100 text-gray-400 cursor-not-allowed pointer-events-none'
                : 'bg-blue-600 hover:bg-blue-700 text-white'
            }`}
          >
            <Ticket className="w-4 h-4" />
            {isSoldOut ? 'Sold Out' : isCancelled ? 'Cancelled' : 'Book Now'}
          </Link>
        </div>
      </div>
    </div>
  );
}
