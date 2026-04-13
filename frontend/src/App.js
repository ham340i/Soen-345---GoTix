import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { Toaster } from 'react-hot-toast';
import { AuthProvider, useAuth } from './context/AuthContext';

// Pages
import LandingPage      from './pages/LandingPage';
import LoginPage        from './pages/LoginPage';
import RegisterPage     from './pages/RegisterPage';
import EventsPage       from './pages/EventsPage';
import EventDetailPage  from './pages/EventDetailPage';
import MyReservations   from './pages/MyReservationsPage';
import ProfilePage      from './pages/ProfilePage';
import AdminDashboard   from './pages/AdminDashboard';
import AdminEvents      from './pages/AdminEventsPage';
import AdminEventForm   from './pages/AdminEventFormPage';
import AdminUsers       from './pages/AdminUsersPage';
import NotFoundPage     from './pages/NotFoundPage';

// Layout
import Navbar from './components/common/Navbar';
import Footer from './components/common/Footer';

function ProtectedRoute({ children }) {
  const { isLoggedIn, loading } = useAuth();
  if (loading) return <div className="min-h-screen flex items-center justify-center"><Spinner /></div>;
  return isLoggedIn ? children : <Navigate to="/login" replace />;
}

function AdminRoute({ children }) {
  const { isLoggedIn, isAdmin, loading } = useAuth();
  if (loading) return <div className="min-h-screen flex items-center justify-center"><Spinner /></div>;
  if (!isLoggedIn) return <Navigate to="/login" replace />;
  if (!isAdmin)    return <Navigate to="/" replace />;
  return children;
}

function Spinner() {
  return (
    <div className="w-10 h-10 border-4 border-blue-200 border-t-blue-600 rounded-full animate-spin" />
  );
}

function AppLayout({ children }) {
  return (
    <div className="min-h-screen flex flex-col bg-gray-50">
      <Navbar />
      <main className="flex-1">{children}</main>
      <Footer />
    </div>
  );
}

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Toaster
          position="top-right"
          toastOptions={{
            duration: 4000,
            style: { fontFamily: 'Inter, sans-serif', fontSize: '14px' },
            success: { iconTheme: { primary: '#2563eb', secondary: '#fff' } },
          }}
        />
        <Routes>
          {/* Public */}
          <Route path="/"        element={<AppLayout><LandingPage /></AppLayout>} />
          <Route path="/login"   element={<AppLayout><LoginPage /></AppLayout>} />
          <Route path="/register" element={<AppLayout><RegisterPage /></AppLayout>} />
          <Route path="/events"  element={<AppLayout><EventsPage /></AppLayout>} />
          <Route path="/events/:id" element={<AppLayout><EventDetailPage /></AppLayout>} />

          {/* Protected (customers + admins) */}
          <Route path="/my-reservations" element={
            <ProtectedRoute><AppLayout><MyReservations /></AppLayout></ProtectedRoute>
          } />
          <Route path="/profile" element={
            <ProtectedRoute><AppLayout><ProfilePage /></AppLayout></ProtectedRoute>
          } />

          {/* Admin only */}
          <Route path="/admin" element={
            <AdminRoute><AppLayout><AdminDashboard /></AppLayout></AdminRoute>
          } />
          <Route path="/admin/events" element={
            <AdminRoute><AppLayout><AdminEvents /></AppLayout></AdminRoute>
          } />
          <Route path="/admin/events/new" element={
            <AdminRoute><AppLayout><AdminEventForm /></AppLayout></AdminRoute>
          } />
          <Route path="/admin/events/:id/edit" element={
            <AdminRoute><AppLayout><AdminEventForm /></AppLayout></AdminRoute>
          } />
          <Route path="/admin/users" element={
            <AdminRoute><AppLayout><AdminUsers /></AppLayout></AdminRoute>
          } />

          {/* 404 */}
          <Route path="*" element={<AppLayout><NotFoundPage /></AppLayout>} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}
