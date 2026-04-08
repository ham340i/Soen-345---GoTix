import React, { useState } from 'react';
import { Calendar, MapPin, Ticket, Hash, X } from 'lucide-react';
import { format } from 'date-fns';
import { StatusBadge, ConfirmDialog } from '../common/UI';
import { reservationsAPI } from '../../services/api';
import toast from 'react-hot-toast';

export default function ReservationCard({ reservation, onCancelled }) {
  const [showConfirm, setShowConfirm] = useState(false);
  const [cancelling,  setCancelling]  = useState(false);

  const isCancelled = reservation.status === 'CANCELLED';

  const handleCancel = async () => {
    setCancelling(true);
    try {
      await reservationsAPI.cancel(reservation.id);
      toast.success('Reservation cancelled successfully');
      setShowConfirm(false);
      onCancelled?.(reservation.id);
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to cancel reservation');
    } finally {
      setCancelling(false);
    }
  };

  return (
    <>
      <div className={`card p-5 transition-opacity ${isCancelled ? 'opacity-60' : ''}`}>
        {/* Header */}
        <div className="flex items-start justify-between mb-4">
          <div className="flex-1 min-w-0">
            <h3 className="font-semibold text-gray-900 text-base line-clamp-1 pr-2">
              {reservation.eventName}
            </h3>
            <div className="flex items-center gap-1.5 mt-1">
              <Hash className="w-3 h-3 text-gray-400" />
              <span className="text-xs font-mono text-blue-600 font-medium">
                {reservation.confirmationCode}
              </span>
            </div>
          </div>
          <StatusBadge status={reservation.status} />
        </div>

        {/* Details */}
        <div className="space-y-2 text-sm text-gray-600 mb-4">
          <div className="flex items-center gap-2">
            <Calendar className="w-4 h-4 text-blue-400 flex-shrink-0" />
            <span>{format(new Date(reservation.eventDate), 'EEE, MMM d yyyy · h:mm a')}</span>
          </div>
          <div className="flex items-center gap-2">
            <MapPin className="w-4 h-4 text-blue-400 flex-shrink-0" />
            <span className="truncate">{reservation.eventLocation}</span>
          </div>
          <div className="flex items-center gap-2">
            <Ticket className="w-4 h-4 text-blue-400 flex-shrink-0" />
            <span>{reservation.numTickets} ticket{reservation.numTickets > 1 ? 's' : ''}</span>
          </div>
        </div>

        {/* Footer */}
        <div className="flex items-center justify-between pt-3 border-t border-gray-100">
          <div>
            <p className="text-xs text-gray-400">Total paid</p>
            <p className="text-lg font-bold text-gray-900">${reservation.totalPrice?.toFixed(2)}</p>
          </div>

          {!isCancelled && (
            <button
              onClick={() => setShowConfirm(true)}
              className="flex items-center gap-1.5 text-sm text-red-600 hover:text-red-700
                         hover:bg-red-50 px-3 py-1.5 rounded-lg transition-colors font-medium"
            >
              <X className="w-4 h-4" />
              Cancel
            </button>
          )}

          {isCancelled && reservation.cancelledAt && (
            <p className="text-xs text-gray-400">
              Cancelled {format(new Date(reservation.cancelledAt), 'MMM d, yyyy')}
            </p>
          )}
        </div>
      </div>

      <ConfirmDialog
        open={showConfirm}
        onClose={() => setShowConfirm(false)}
        onConfirm={handleCancel}
        title="Cancel Reservation"
        message={`Are you sure you want to cancel your ${reservation.numTickets} ticket(s) for "${reservation.eventName}"? This action cannot be undone.`}
        confirmLabel={cancelling ? 'Cancelling…' : 'Yes, Cancel'}
        danger
      />
    </>
  );
}
