import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Ticket, Eye, EyeOff } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { Alert } from '../components/common/UI';
import toast from 'react-hot-toast';

export default function RegisterPage() {
  const { register } = useAuth();
  const navigate     = useNavigate();

  const [form, setForm]       = useState({ name: '', email: '', phoneNumber: '', password: '', confirm: '' });
  const [showPw, setShowPw]   = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError]     = useState('');
  const [mode, setMode]       = useState('email'); // 'email' | 'phone'

  const handle = (e) => setForm(f => ({ ...f, [e.target.name]: e.target.value }));

  const submit = async (e) => {
    e.preventDefault();
    setError('');
    if (form.password !== form.confirm) { setError('Passwords do not match'); return; }
    if (form.password.length < 8)       { setError('Password must be at least 8 characters'); return; }
    setLoading(true);
    // Default notification channel matches the chosen registration method
    const notificationPreference = mode === 'email' ? 'EMAIL' : 'SMS';
    try {
      await register(
        form.name,
        mode === 'email' ? form.email : undefined,
        mode === 'phone' ? form.phoneNumber : undefined,
        form.password,
        notificationPreference,
      );
      toast.success('Account created! Welcome to GoTix 🎉');
      navigate('/events');
    } catch (err) {
      setError(err.response?.data?.message || 'Registration failed. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-[80vh] flex items-center justify-center px-4 py-12">
      <div className="w-full max-w-md">
        <div className="card p-8">
          <div className="flex justify-center mb-8">
            <div className="flex items-center gap-2 text-blue-600 font-bold text-2xl">
              <Ticket className="w-7 h-7" />
              GoTix
            </div>
          </div>

          <h1 className="text-2xl font-bold text-gray-900 text-center mb-1">Create your account</h1>
          <p className="text-gray-500 text-sm text-center mb-6">
            Already have an account?{' '}
            <Link to="/login" className="text-blue-600 font-medium hover:underline">Sign in</Link>
          </p>

          <Alert type="error" message={error} className="mb-4" />

          <form onSubmit={submit} className="space-y-4">
            {/* Full name */}
            <div>
              <label className="label">Full Name <span className="text-red-500">*</span></label>
              <input name="name" required placeholder="Alice Smith"
                value={form.name} onChange={handle} className="input" />
            </div>

            {/* Mode toggle */}
            <div>
              <label className="label">Register with</label>
              <div className="flex rounded-lg border border-gray-300 overflow-hidden">
                {['email','phone'].map(m => (
                  <button key={m} type="button" onClick={() => setMode(m)}
                    className={`flex-1 py-2 text-sm font-medium transition-colors ${
                      mode === m ? 'bg-blue-600 text-white' : 'bg-white text-gray-600 hover:bg-gray-50'
                    }`}>
                    {m === 'email' ? 'Email' : 'Phone'}
                  </button>
                ))}
              </div>
            </div>

            {/* Contact field */}
            {mode === 'email' ? (
              <div>
                <label className="label">Email Address <span className="text-red-500">*</span></label>
                <input name="email" type="email" required placeholder="you@example.com"
                  value={form.email} onChange={handle} className="input" />
              </div>
            ) : (
              <div>
                <label className="label">Phone Number <span className="text-red-500">*</span></label>
                <input name="phoneNumber" type="tel" required placeholder="+1 514 123 4567"
                  value={form.phoneNumber} onChange={handle} className="input" />
              </div>
            )}

            {/* Password */}
            <div>
              <label className="label">Password <span className="text-red-500">*</span></label>
              <div className="relative">
                <input name="password" type={showPw ? 'text' : 'password'} required
                  placeholder="Min 8 characters" value={form.password} onChange={handle} className="input pr-10" />
                <button type="button" onClick={() => setShowPw(!showPw)}
                  className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600">
                  {showPw ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
                </button>
              </div>
            </div>

            {/* Confirm */}
            <div>
              <label className="label">Confirm Password <span className="text-red-500">*</span></label>
              <input name="confirm" type="password" required placeholder="Re-enter password"
                value={form.confirm} onChange={handle} className="input" />
            </div>

            <button type="submit" disabled={loading} className="btn-primary w-full py-2.5 mt-2">
              {loading ? 'Creating account…' : 'Create Account'}
            </button>
          </form>

          <p className="text-xs text-gray-400 text-center mt-4">
            By signing up you agree to our Terms of Service.
          </p>
        </div>
      </div>
    </div>
  );
}
