import React from 'react';
import { AlertCircle, CheckCircle, Info, XCircle } from 'lucide-react';

// ── Loading Spinner ───────────────────────────────────────────────────────────
export function Spinner({ size = 'md', className = '' }) {
  const sizes = { sm: 'w-4 h-4', md: 'w-8 h-8', lg: 'w-12 h-12' };
  return (
    <div className={`${sizes[size]} border-4 border-blue-100 border-t-blue-600
                     rounded-full animate-spin ${className}`} />
  );
}

export function PageSpinner() {
  return (
    <div className="min-h-[60vh] flex items-center justify-center">
      <Spinner size="lg" />
    </div>
  );
}

// ── Alert / Error Banner ──────────────────────────────────────────────────────
const alertStyles = {
  error:   'bg-red-50 border-red-200 text-red-800',
  success: 'bg-green-50 border-green-200 text-green-800',
  info:    'bg-blue-50 border-blue-200 text-blue-800',
  warning: 'bg-yellow-50 border-yellow-200 text-yellow-800',
};
const alertIcons = {
  error:   XCircle,
  success: CheckCircle,
  info:    Info,
  warning: AlertCircle,
};

export function Alert({ type = 'info', message, className = '' }) {
  if (!message) return null;
  const Icon = alertIcons[type];
  return (
    <div className={`flex items-start gap-3 p-4 rounded-lg border ${alertStyles[type]} ${className}`}>
      <Icon className="w-5 h-5 flex-shrink-0 mt-0.5" />
      <p className="text-sm">{message}</p>
    </div>
  );
}

// ── Empty State ───────────────────────────────────────────────────────────────
export function EmptyState({ icon: Icon, title, description, action }) {
  return (
    <div className="flex flex-col items-center justify-center py-16 text-center px-4">
      {Icon && (
        <div className="w-16 h-16 bg-blue-50 rounded-full flex items-center justify-center mb-4">
          <Icon className="w-8 h-8 text-blue-400" />
        </div>
      )}
      <h3 className="text-lg font-semibold text-gray-900 mb-2">{title}</h3>
      {description && <p className="text-gray-500 text-sm mb-6 max-w-xs">{description}</p>}
      {action}
    </div>
  );
}

// ── Modal ─────────────────────────────────────────────────────────────────────
export function Modal({ open, onClose, title, children, maxWidth = 'max-w-lg' }) {
  if (!open) return null;
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
      <div className="fixed inset-0 bg-black/50 backdrop-blur-sm" onClick={onClose} />
      <div className={`relative bg-white rounded-2xl shadow-2xl w-full ${maxWidth} z-10`}>
        {title && (
          <div className="px-6 py-4 border-b border-gray-100">
            <h2 className="text-lg font-semibold text-gray-900">{title}</h2>
          </div>
        )}
        <div className="px-6 py-4">{children}</div>
      </div>
    </div>
  );
}

// ── Confirm Dialog ────────────────────────────────────────────────────────────
export function ConfirmDialog({ open, onClose, onConfirm, title, message, confirmLabel = 'Confirm', danger = false }) {
  return (
    <Modal open={open} onClose={onClose} title={title} maxWidth="max-w-sm">
      <p className="text-sm text-gray-600 mb-6">{message}</p>
      <div className="flex gap-3 justify-end">
        <button onClick={onClose} className="btn-secondary text-sm">Cancel</button>
        <button onClick={onConfirm} className={danger ? 'btn-danger text-sm' : 'btn-primary text-sm'}>
          {confirmLabel}
        </button>
      </div>
    </Modal>
  );
}

// ── Badge helpers ─────────────────────────────────────────────────────────────
export function StatusBadge({ status }) {
  const styles = {
    ACTIVE:     'bg-green-100 text-green-700',
    CONFIRMED:  'bg-green-100 text-green-700',
    CANCELLED:  'bg-red-100 text-red-700',
    SOLD_OUT:   'bg-orange-100 text-orange-700',
    COMPLETED:  'bg-gray-100 text-gray-600',
    PENDING:    'bg-yellow-100 text-yellow-700',
  };
  return (
    <span className={`badge ${styles[status] || 'bg-gray-100 text-gray-600'}`}>
      {status?.replace('_', ' ')}
    </span>
  );
}

export function CategoryBadge({ category }) {
  const styles = {
    MOVIE:    'bg-purple-100 text-purple-700',
    CONCERT:  'bg-pink-100 text-pink-700',
    SPORTS:   'bg-blue-100 text-blue-700',
    TRAVEL:   'bg-teal-100 text-teal-700',
    THEATER:  'bg-indigo-100 text-indigo-700',
    CONFERENCE:'bg-orange-100 text-orange-700',
    OTHER:    'bg-gray-100 text-gray-600',
  };
  return (
    <span className={`badge ${styles[category] || 'bg-gray-100 text-gray-600'}`}>
      {category}
    </span>
  );
}

// ── Form Field ────────────────────────────────────────────────────────────────
export function FormField({ label, error, children, required }) {
  return (
    <div>
      {label && (
        <label className="label">
          {label} {required && <span className="text-red-500">*</span>}
        </label>
      )}
      {children}
      {error && <p className="mt-1 text-xs text-red-600">{error}</p>}
    </div>
  );
}

// ── Stats Card ────────────────────────────────────────────────────────────────
export function StatsCard({ label, value, icon: Icon, color = 'blue', sub }) {
  const colors = {
    blue:   'bg-blue-50 text-blue-600',
    green:  'bg-green-50 text-green-600',
    purple: 'bg-purple-50 text-purple-600',
    orange: 'bg-orange-50 text-orange-600',
    red:    'bg-red-50 text-red-600',
  };
  return (
    <div className="card p-6">
      <div className="flex items-center justify-between mb-3">
        <p className="text-sm font-medium text-gray-500">{label}</p>
        {Icon && (
          <div className={`w-10 h-10 rounded-xl flex items-center justify-center ${colors[color]}`}>
            <Icon className="w-5 h-5" />
          </div>
        )}
      </div>
      <p className="text-3xl font-bold text-gray-900">{value}</p>
      {sub && <p className="text-xs text-gray-500 mt-1">{sub}</p>}
    </div>
  );
}
