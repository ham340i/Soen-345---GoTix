import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import {
  CalendarDays, Users, Ticket, TrendingUp,
  Plus, ArrowRight, Clock
} from 'lucide-react';
import { eventsAPI, reservationsAPI, usersAPI } from '../services/api';
import { PageSpinner, StatsCard, StatusBadge } from '../components/common/UI';
import { format } from 'date-fns';

export default function AdminDashboard() {
  const [stats,   setStats]   = useState(null);
  const [events,  setEvents]  = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    Promise.all([
      eventsAPI.getAll(),
      usersAPI.getAll(),
    ]).then(([evRes, usRes]) => {
      const evs = evRes.data;
      setEvents(evs.slice(0, 5));
      setStats({
        totalEvents:  evs.length,
        activeEvents: evs.filter(e => e.status === 'ACTIVE').length,
        totalUsers:   usRes.data.length,
        totalSeats:   evs.reduce((s, e) => s + e.totalSeats, 0),
        bookedSeats:  evs.reduce((s, e) => s + (e.totalSeats - e.availableSeats), 0),
      });
    }).catch(console.error)
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <PageSpinner />;

  const fillRate = stats.totalSeats > 0
    ? Math.round((stats.bookedSeats / stats.totalSeats) * 100)
    : 0;

  return (
    <div className="page-container">
      <div className="flex items-center justify-between mb-8">
        <div>
          <h1 className="section-title mb-1">Admin Dashboard</h1>
          <p className="text-gray-500 text-sm">Overview of the GoTix platform</p>
        </div>
        <Link to="/admin/events/new" className="btn-primary flex items-center gap-2 text-sm">
          <Plus className="w-4 h-4" /> New Event
        </Link>
      </div>

      {/* Stats */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4 mb-8">
        <StatsCard label="Total Events"   value={stats.totalEvents}  icon={CalendarDays} color="blue"   sub={`${stats.activeEvents} active`} />
        <StatsCard label="Registered Users" value={stats.totalUsers} icon={Users}        color="purple" />
        <StatsCard label="Tickets Booked" value={stats.bookedSeats}  icon={Ticket}       color="green"  sub={`of ${stats.totalSeats} total`} />
        <StatsCard label="Booking Rate"   value={`${fillRate}%`}     icon={TrendingUp}   color="orange" sub="seats filled" />
      </div>

      {/* Recent events table */}
      <div className="card">
        <div className="px-6 py-4 border-b border-gray-100 flex items-center justify-between">
          <h2 className="font-semibold text-gray-900">Recent Events</h2>
          <Link to="/admin/events" className="text-sm text-blue-600 hover:underline flex items-center gap-1">
            View all <ArrowRight className="w-3 h-3" />
          </Link>
        </div>
        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead>
              <tr className="bg-gray-50 text-left">
                <th className="px-6 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wider">Event</th>
                <th className="px-6 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wider">Date</th>
                <th className="px-6 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wider">Seats</th>
                <th className="px-6 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wider">Status</th>
                <th className="px-6 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wider">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100">
              {events.length === 0 ? (
                <tr>
                  <td colSpan={5} className="px-6 py-8 text-center text-gray-400 text-sm">
                    No events yet. <Link to="/admin/events/new" className="text-blue-600 hover:underline">Create the first one →</Link>
                  </td>
                </tr>
              ) : events.map(ev => (
                <tr key={ev.id} className="hover:bg-gray-50 transition-colors">
                  <td className="px-6 py-4">
                    <p className="font-medium text-gray-900 line-clamp-1">{ev.name}</p>
                    <p className="text-xs text-gray-400">{ev.location}</p>
                  </td>
                  <td className="px-6 py-4 text-gray-600">
                    <div className="flex items-center gap-1.5">
                      <Clock className="w-3.5 h-3.5 text-gray-400" />
                      {format(new Date(ev.eventDate), 'MMM d, yyyy')}
                    </div>
                  </td>
                  <td className="px-6 py-4">
                    <div className="text-gray-600">
                      {ev.totalSeats - ev.availableSeats}/{ev.totalSeats}
                    </div>
                    <div className="h-1 bg-gray-100 rounded-full mt-1 w-16">
                      <div
                        className="h-full bg-blue-400 rounded-full"
                        style={{ width: `${((ev.totalSeats - ev.availableSeats) / ev.totalSeats) * 100}%` }}
                      />
                    </div>
                  </td>
                  <td className="px-6 py-4"><StatusBadge status={ev.status} /></td>
                  <td className="px-6 py-4">
                    <div className="flex items-center gap-2">
                      <Link to={`/admin/events/${ev.id}/edit`}
                        className="text-xs text-blue-600 hover:underline font-medium">Edit</Link>
                      <Link to={`/events/${ev.id}`}
                        className="text-xs text-gray-500 hover:underline">View</Link>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {/* Quick links */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 mt-6">
        {[
          { to: '/admin/events',   icon: CalendarDays, label: 'Manage Events',  desc: 'Add, edit, cancel events' },
          { to: '/admin/users',    icon: Users,        label: 'Manage Users',   desc: 'View and deactivate accounts' },
          { to: '/events',         icon: Ticket,       label: 'Public View',    desc: 'See what customers see' },
        ].map(l => (
          <Link key={l.to} to={l.to}
            className="card p-5 hover:shadow-md transition-shadow flex items-center gap-4">
            <div className="w-10 h-10 bg-blue-50 rounded-xl flex items-center justify-center flex-shrink-0">
              <l.icon className="w-5 h-5 text-blue-600" />
            </div>
            <div>
              <p className="font-semibold text-gray-900 text-sm">{l.label}</p>
              <p className="text-xs text-gray-400">{l.desc}</p>
            </div>
            <ArrowRight className="w-4 h-4 text-gray-300 ml-auto" />
          </Link>
        ))}
      </div>
    </div>
  );
}
