import React from 'react';
import { Link } from 'react-router-dom';
import { Ticket, Zap, ShieldCheck, Bell, ArrowRight, Star } from 'lucide-react';
import { useAuth } from '../context/AuthContext';

const FEATURES = [
  { icon: Zap,        title: 'Instant Booking',    desc: 'Reserve tickets in seconds. Seats are held atomically — no double bookings, ever.' },
  { icon: ShieldCheck, title: 'Secure & Reliable', desc: 'JWT authentication, BCrypt passwords, role-based access, and cloud-ready architecture.' },
  { icon: Bell,        title: 'Instant Confirmation', desc: 'Get email and SMS confirmations the moment your reservation is confirmed.' },
  { icon: Ticket,      title: 'All Event Types',   desc: 'Movies, concerts, sports, travel, theater — one platform for every experience.' },
];

const CATEGORIES = [
  { emoji: '🎬', name: 'Movies',    color: 'bg-purple-50 hover:bg-purple-100 text-purple-700', cat: 'MOVIE' },
  { emoji: '🎵', name: 'Concerts',  color: 'bg-pink-50 hover:bg-pink-100 text-pink-700',       cat: 'CONCERT' },
  { emoji: '⚽', name: 'Sports',    color: 'bg-blue-50 hover:bg-blue-100 text-blue-700',       cat: 'SPORTS' },
  { emoji: '✈️', name: 'Travel',    color: 'bg-teal-50 hover:bg-teal-100 text-teal-700',       cat: 'TRAVEL' },
  { emoji: '🎭', name: 'Theater',   color: 'bg-indigo-50 hover:bg-indigo-100 text-indigo-700', cat: 'THEATER' },
  { emoji: '💼', name: 'Conference',color: 'bg-orange-50 hover:bg-orange-100 text-orange-700', cat: 'CONFERENCE' },
];

export default function LandingPage() {
  const { isLoggedIn } = useAuth();

  return (
    <div>
      {/* Hero */}
      <section className="bg-gradient-to-br from-blue-900 via-blue-800 to-blue-700 text-white">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-24 text-center">
          <div className="inline-flex items-center gap-2 bg-blue-700/50 rounded-full px-4 py-1.5 text-sm font-medium mb-6">
            <Star className="w-4 h-4 text-yellow-400" />
            <span>SOEN 345 — Cloud Ticket Reservation System</span>
          </div>
          <h1 className="text-5xl sm:text-6xl font-extrabold leading-tight mb-6">
            Book Tickets for <br />
            <span className="text-blue-300">Any Experience</span>
          </h1>
          <p className="text-blue-200 text-xl max-w-2xl mx-auto mb-10 leading-relaxed">
            Movies, concerts, sports, travel — find and reserve seats in seconds.
            Instant confirmations, atomic bookings, zero hassle.
          </p>
          <div className="flex flex-col sm:flex-row gap-4 justify-center">
            <Link to="/events"
              className="bg-white text-blue-700 hover:bg-blue-50 font-semibold px-8 py-3
                         rounded-xl flex items-center justify-center gap-2 transition-colors text-lg">
              Browse Events <ArrowRight className="w-5 h-5" />
            </Link>
            {!isLoggedIn && (
              <Link to="/register"
                className="bg-blue-600 hover:bg-blue-500 border border-blue-500 text-white
                           font-semibold px-8 py-3 rounded-xl transition-colors text-lg">
                Create Free Account
              </Link>
            )}
          </div>

          {/* Quick stats */}
          <div className="grid grid-cols-3 gap-6 mt-16 max-w-lg mx-auto">
            {[['1000+','Events'], ['50K+','Tickets Sold'], ['99.9%','Uptime']].map(([v, l]) => (
              <div key={l} className="text-center">
                <p className="text-3xl font-bold text-white">{v}</p>
                <p className="text-blue-300 text-sm">{l}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* Categories */}
      <section className="py-16 bg-white">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <h2 className="section-title text-center mb-3">Browse by Category</h2>
          <p className="text-gray-500 text-center mb-10">Find exactly what you're looking for</p>
          <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-6 gap-4">
            {CATEGORIES.map(c => (
              <Link
                key={c.cat}
                to={`/events?category=${c.cat}`}
                className={`flex flex-col items-center gap-2 p-4 rounded-xl font-medium
                            transition-all duration-150 ${c.color} cursor-pointer`}
              >
                <span className="text-4xl">{c.emoji}</span>
                <span className="text-sm font-semibold">{c.name}</span>
              </Link>
            ))}
          </div>
        </div>
      </section>

      {/* Features */}
      <section className="py-16 bg-gray-50">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <h2 className="section-title text-center mb-3">Why GoTix?</h2>
          <p className="text-gray-500 text-center mb-12">Built with production-grade engineering</p>
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
            {FEATURES.map(f => (
              <div key={f.title} className="card p-6 text-center hover:shadow-md transition-shadow">
                <div className="w-12 h-12 bg-blue-50 rounded-xl flex items-center justify-center mx-auto mb-4">
                  <f.icon className="w-6 h-6 text-blue-600" />
                </div>
                <h3 className="font-semibold text-gray-900 mb-2">{f.title}</h3>
                <p className="text-sm text-gray-500 leading-relaxed">{f.desc}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* CTA */}
      {!isLoggedIn && (
        <section className="py-16 bg-blue-700 text-white text-center">
          <div className="max-w-2xl mx-auto px-4">
            <h2 className="text-3xl font-bold mb-4">Ready to Book Your Next Event?</h2>
            <p className="text-blue-200 mb-8">
              Create a free account and start reserving tickets in under a minute.
            </p>
            <Link to="/register"
              className="inline-flex items-center gap-2 bg-white text-blue-700 hover:bg-blue-50
                         font-semibold px-8 py-3 rounded-xl text-lg transition-colors">
              Get Started Free <ArrowRight className="w-5 h-5" />
            </Link>
          </div>
        </section>
      )}
    </div>
  );
}
