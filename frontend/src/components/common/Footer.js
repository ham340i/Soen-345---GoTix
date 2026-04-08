import React from 'react';
import { Link } from 'react-router-dom';
import { Ticket, Github, Mail } from 'lucide-react';

export default function Footer() {
  return (
    <footer className="bg-gray-900 text-gray-400 mt-auto">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
          {/* Brand */}
          <div>
            <div className="flex items-center gap-2 text-white font-bold text-lg mb-3">
              <Ticket className="w-5 h-5 text-blue-400" />
              GoTix
            </div>
            <p className="text-sm leading-relaxed">
              Cloud-based ticket reservation for movies, concerts, sports and more.
              Book your next experience in seconds.
            </p>
          </div>

          {/* Links */}
          <div>
            <h4 className="text-white font-semibold mb-3 text-sm uppercase tracking-wider">Quick Links</h4>
            <ul className="space-y-2 text-sm">
              <li><Link to="/events" className="hover:text-white transition-colors">Browse Events</Link></li>
              <li><Link to="/register" className="hover:text-white transition-colors">Create Account</Link></li>
              <li><Link to="/login" className="hover:text-white transition-colors">Sign In</Link></li>
              <li><Link to="/my-reservations" className="hover:text-white transition-colors">My Tickets</Link></li>
            </ul>
          </div>

          {/* Tech */}
          <div>
            <h4 className="text-white font-semibold mb-3 text-sm uppercase tracking-wider">Tech Stack</h4>
            <ul className="space-y-1 text-sm">
              <li>Java 17 + Spring Boot 3.2</li>
              <li>React 18 + Tailwind CSS</li>
              <li>Spring Security + JWT</li>
              <li>PostgreSQL / H2</li>
              <li>GitHub Actions CI/CD</li>
            </ul>
          </div>
        </div>

        <div className="border-t border-gray-800 mt-10 pt-6 flex flex-col sm:flex-row items-center justify-between gap-4">
          <p className="text-xs">© 2026 GoTix — SOEN 345, Concordia University</p>
          <div className="flex items-center gap-4">
            <a href="https://github.com" className="hover:text-white transition-colors">
              <Github className="w-4 h-4" />
            </a>
            <a href="mailto:support@gotix.com" className="hover:text-white transition-colors">
              <Mail className="w-4 h-4" />
            </a>
          </div>
        </div>
      </div>
    </footer>
  );
}
