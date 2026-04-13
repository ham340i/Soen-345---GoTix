import React, { useState } from 'react';
import { Link, NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import {
  Ticket, Menu, X, User, LogOut, LayoutDashboard,
  CalendarDays, BookMarked, ChevronDown
} from 'lucide-react';

export default function Navbar() {
  const { isLoggedIn, isAdmin, user, logout } = useAuth();
  const navigate  = useNavigate();
  const [open, setOpen]       = useState(false);
  const [userMenu, setUserMenu] = useState(false);

  const handleLogout = () => {
    logout();
    navigate('/');
    setUserMenu(false);
  };

  const navLink = ({ isActive }) =>
    `text-sm font-medium transition-colors px-1 py-0.5 rounded ${
      isActive ? 'text-blue-600' : 'text-gray-600 hover:text-gray-900'
    }`;

  return (
    <nav className="bg-white border-b border-gray-200 sticky top-0 z-50 shadow-sm">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between h-16">

          {/* Logo */}
          <Link to="/" className="flex items-center gap-2 text-blue-600 font-bold text-xl">
            <Ticket className="w-6 h-6" />
            <span>GoTix</span>
          </Link>

          {/* Desktop nav */}
          <div className="hidden md:flex items-center gap-6">
            <NavLink to="/events" className={navLink}>Browse Events</NavLink>
            {isLoggedIn && (
              <NavLink to="/my-reservations" className={navLink}>My Tickets</NavLink>
            )}
            {isAdmin && (
              <NavLink to="/admin" className={navLink}>Admin Panel</NavLink>
            )}
          </div>

          {/* Desktop right */}
          <div className="hidden md:flex items-center gap-3">
            {isLoggedIn ? (
              <div className="relative">
                <button
                  onClick={() => setUserMenu(!userMenu)}
                  className="flex items-center gap-2 text-sm font-medium text-gray-700
                             hover:text-gray-900 px-3 py-2 rounded-lg hover:bg-gray-100 transition-colors"
                >
                  <div className="w-8 h-8 bg-blue-600 rounded-full flex items-center justify-center text-white text-sm font-semibold">
                    {user?.name?.[0]?.toUpperCase() || 'U'}
                  </div>
                  <span className="hidden lg:block">{user?.name}</span>
                  <ChevronDown className="w-4 h-4" />
                </button>

                {userMenu && (
                  <div className="absolute right-0 mt-1 w-52 bg-white rounded-xl shadow-lg border border-gray-100 py-1 z-50">
                    <div className="px-4 py-2 border-b border-gray-100">
                      <p className="text-xs text-gray-500">Signed in as</p>
                      <p className="text-sm font-semibold text-gray-900 truncate">{user?.email}</p>
                      <span className={`badge mt-1 ${isAdmin ? 'bg-purple-100 text-purple-700' : 'bg-blue-100 text-blue-700'}`}>
                        {user?.role}
                      </span>
                    </div>
                    <Link to="/profile" onClick={() => setUserMenu(false)}
                      className="flex items-center gap-2 px-4 py-2 text-sm text-gray-700 hover:bg-gray-50 transition-colors">
                      <User className="w-4 h-4" /> Profile
                    </Link>
                    <Link to="/my-reservations" onClick={() => setUserMenu(false)}
                      className="flex items-center gap-2 px-4 py-2 text-sm text-gray-700 hover:bg-gray-50 transition-colors">
                      <BookMarked className="w-4 h-4" /> My Tickets
                    </Link>
                    {isAdmin && (
                      <Link to="/admin" onClick={() => setUserMenu(false)}
                        className="flex items-center gap-2 px-4 py-2 text-sm text-gray-700 hover:bg-gray-50 transition-colors">
                        <LayoutDashboard className="w-4 h-4" /> Admin Panel
                      </Link>
                    )}
                    <div className="border-t border-gray-100 mt-1">
                      <button onClick={handleLogout}
                        className="flex items-center gap-2 w-full px-4 py-2 text-sm text-red-600 hover:bg-red-50 transition-colors">
                        <LogOut className="w-4 h-4" /> Sign Out
                      </button>
                    </div>
                  </div>
                )}
              </div>
            ) : (
              <>
                <Link to="/login" className="btn-ghost text-sm">Sign In</Link>
                <Link to="/register" className="btn-primary text-sm">Get Started</Link>
              </>
            )}
          </div>

          {/* Mobile hamburger */}
          <button className="md:hidden p-2 rounded-lg text-gray-600 hover:bg-gray-100"
            onClick={() => setOpen(!open)}>
            {open ? <X className="w-5 h-5" /> : <Menu className="w-5 h-5" />}
          </button>
        </div>
      </div>

      {/* Mobile menu */}
      {open && (
        <div className="md:hidden border-t border-gray-100 bg-white px-4 py-4 space-y-2">
          <NavLink to="/events" className={navLink} onClick={() => setOpen(false)}>
            <div className="flex items-center gap-2 py-2"><CalendarDays className="w-4 h-4" /> Browse Events</div>
          </NavLink>
          {isLoggedIn && (
            <>
              <NavLink to="/my-reservations" className={navLink} onClick={() => setOpen(false)}>
                <div className="flex items-center gap-2 py-2"><BookMarked className="w-4 h-4" /> My Tickets</div>
              </NavLink>
              <NavLink to="/profile" className={navLink} onClick={() => setOpen(false)}>
                <div className="flex items-center gap-2 py-2"><User className="w-4 h-4" /> Profile</div>
              </NavLink>
              {isAdmin && (
                <NavLink to="/admin" className={navLink} onClick={() => setOpen(false)}>
                  <div className="flex items-center gap-2 py-2"><LayoutDashboard className="w-4 h-4" /> Admin</div>
                </NavLink>
              )}
              <button onClick={handleLogout}
                className="flex items-center gap-2 text-sm text-red-600 py-2 w-full">
                <LogOut className="w-4 h-4" /> Sign Out
              </button>
            </>
          )}
          {!isLoggedIn && (
            <div className="flex gap-2 pt-2">
              <Link to="/login" className="btn-secondary flex-1 text-center text-sm" onClick={() => setOpen(false)}>Sign In</Link>
              <Link to="/register" className="btn-primary flex-1 text-center text-sm" onClick={() => setOpen(false)}>Register</Link>
            </div>
          )}
        </div>
      )}

      {/* Click-outside to close user menu */}
      {userMenu && <div className="fixed inset-0 z-40" onClick={() => setUserMenu(false)} />}
    </nav>
  );
}
