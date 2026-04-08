import React, { useState } from 'react';
import { User, Mail, Phone, ShieldCheck, Save } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { usersAPI } from '../services/api';
import { Alert } from '../components/common/UI';
import toast from 'react-hot-toast';

export default function ProfilePage() {
  const { user, login } = useAuth();
  const [editing, setEditing] = useState(false);
  const [saving,  setSaving]  = useState(false);
  const [error,   setError]   = useState('');
  const [form, setForm] = useState({
    name:        user?.name        || '',
    email:       user?.email       || '',
    phoneNumber: user?.phoneNumber || '',
  });

  const handle = (e) => setForm(f => ({ ...f, [e.target.name]: e.target.value }));

  const save = async (e) => {
    e.preventDefault();
    setSaving(true); setError('');
    try {
      await usersAPI.updateMe({
        name:        form.name        || undefined,
        email:       form.email       || undefined,
        phoneNumber: form.phoneNumber || undefined,
      });
      toast.success('Profile updated!');
      setEditing(false);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to update profile.');
    } finally { setSaving(false); }
  };

  const initials = user?.name?.split(' ').map(n => n[0]).join('').toUpperCase().slice(0, 2) || 'U';

  return (
    <div className="page-container max-w-2xl">
      <h1 className="section-title mb-6">My Profile</h1>

      {/* Avatar + role */}
      <div className="card p-6 mb-6">
        <div className="flex items-center gap-5">
          <div className="w-20 h-20 bg-blue-600 rounded-2xl flex items-center justify-center
                          text-white text-2xl font-bold flex-shrink-0">
            {initials}
          </div>
          <div>
            <h2 className="text-xl font-bold text-gray-900">{user?.name}</h2>
            <p className="text-gray-500 text-sm">{user?.email}</p>
            <span className={`badge mt-2 ${
              user?.role === 'ADMIN'
                ? 'bg-purple-100 text-purple-700'
                : 'bg-blue-100 text-blue-700'
            }`}>
              <ShieldCheck className="w-3 h-3 mr-1 inline" />
              {user?.role}
            </span>
          </div>
        </div>
      </div>

      {/* Edit form */}
      <div className="card p-6">
        <div className="flex items-center justify-between mb-5">
          <h3 className="font-semibold text-gray-900">Account Information</h3>
          {!editing && (
            <button onClick={() => setEditing(true)} className="btn-secondary text-sm">
              Edit Profile
            </button>
          )}
        </div>

        <Alert type="error" message={error} className="mb-4" />

        {editing ? (
          <form onSubmit={save} className="space-y-4">
            <div>
              <label className="label">
                <User className="w-4 h-4 inline mr-1.5 text-gray-400" />Full Name
              </label>
              <input name="name" value={form.name} onChange={handle} className="input" />
            </div>
            <div>
              <label className="label">
                <Mail className="w-4 h-4 inline mr-1.5 text-gray-400" />Email Address
              </label>
              <input name="email" type="email" value={form.email} onChange={handle} className="input" />
            </div>
            <div>
              <label className="label">
                <Phone className="w-4 h-4 inline mr-1.5 text-gray-400" />Phone Number
              </label>
              <input name="phoneNumber" type="tel" value={form.phoneNumber} onChange={handle}
                placeholder="+1 514 123 4567" className="input" />
            </div>
            <div className="flex gap-3 pt-2">
              <button type="button" onClick={() => { setEditing(false); setError(''); }}
                className="btn-secondary flex-1 text-sm">Cancel</button>
              <button type="submit" disabled={saving}
                className="btn-primary flex-1 text-sm flex items-center justify-center gap-2">
                <Save className="w-4 h-4" />
                {saving ? 'Saving…' : 'Save Changes'}
              </button>
            </div>
          </form>
        ) : (
          <div className="space-y-4">
            {[
              { icon: User,  label: 'Full Name',     val: user?.name        || '—' },
              { icon: Mail,  label: 'Email',          val: user?.email       || '—' },
              { icon: Phone, label: 'Phone Number',   val: user?.phoneNumber || '—' },
            ].map(({ icon: Icon, label, val }) => (
              <div key={label} className="flex items-center gap-4 py-3 border-b border-gray-100 last:border-0">
                <div className="w-8 h-8 bg-blue-50 rounded-lg flex items-center justify-center flex-shrink-0">
                  <Icon className="w-4 h-4 text-blue-500" />
                </div>
                <div>
                  <p className="text-xs text-gray-400">{label}</p>
                  <p className="text-sm font-medium text-gray-900">{val}</p>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
