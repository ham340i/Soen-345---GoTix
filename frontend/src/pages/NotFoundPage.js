import React from 'react';
import { Link } from 'react-router-dom';
import { Ticket, Home } from 'lucide-react';

export default function NotFoundPage() {
  return (
    <div className="min-h-[70vh] flex flex-col items-center justify-center px-4 text-center">
      <div className="text-8xl font-extrabold text-blue-100 select-none mb-4">404</div>
      <Ticket className="w-12 h-12 text-blue-300 mb-4" />
      <h1 className="text-2xl font-bold text-gray-900 mb-2">Page Not Found</h1>
      <p className="text-gray-500 mb-8 max-w-xs">
        The page you're looking for doesn't exist or has been moved.
      </p>
      <div className="flex gap-3">
        <Link to="/" className="btn-secondary flex items-center gap-2 text-sm">
          <Home className="w-4 h-4" /> Go Home
        </Link>
        <Link to="/events" className="btn-primary flex items-center gap-2 text-sm">
          <Ticket className="w-4 h-4" /> Browse Events
        </Link>
      </div>
    </div>
  );
}
