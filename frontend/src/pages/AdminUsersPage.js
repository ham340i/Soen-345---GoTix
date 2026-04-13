import React, { useState, useEffect, useCallback } from 'react';
import { Users, ShieldCheck, UserX, Search } from 'lucide-react';
import { format } from 'date-fns';
import { usersAPI } from '../services/api';
import { PageSpinner, EmptyState, Alert, ConfirmDialog } from '../components/common/UI';
import toast from 'react-hot-toast';

export default function AdminUsersPage() {
  const [users,     setUsers]     = useState([]);
  const [filtered,  setFiltered]  = useState([]);
  const [loading,   setLoading]   = useState(true);
  const [error,     setError]     = useState('');
  const [search,    setSearch]    = useState('');
  const [deactivate, setDeactivate] = useState(null);
  const [deactivating, setDeactivating] = useState(false);

  const load = useCallback(async () => {
    setLoading(true); setError('');
    try {
      const res = await usersAPI.getAll();
      setUsers(res.data);
      setFiltered(res.data);
    } catch {
      setError('Failed to load users.');
    } finally { setLoading(false); }
  }, []);

  useEffect(() => { load(); }, [load]);

  useEffect(() => {
    const q = search.toLowerCase();
    setFiltered(
      q ? users.filter(u =>
        u.name?.toLowerCase().includes(q) ||
        u.email?.toLowerCase().includes(q) ||
        u.phoneNumber?.includes(q)
      ) : users
    );
  }, [search, users]);

  const handleDeactivate = async () => {
    setDeactivating(true);
    try {
      await usersAPI.deactivate(deactivate.id);
      setUsers(prev => prev.map(u => u.id === deactivate.id ? { ...u, active: false } : u));
      toast.success(`${deactivate.name}'s account deactivated.`);
      setDeactivate(null);
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to deactivate user.');
    } finally { setDeactivating(false); }
  };

  const admins    = users.filter(u => u.role === 'ADMIN').length;
  const customers = users.filter(u => u.role === 'CUSTOMER').length;
  const active    = users.filter(u => u.active).length;

  return (
    <div className="page-container">
      {/* Header */}
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="section-title mb-1">Manage Users</h1>
          <p className="text-gray-500 text-sm">
            {users.length} total · {admins} admin{admins !== 1 ? 's' : ''} · {customers} customer{customers !== 1 ? 's' : ''} · {active} active
          </p>
        </div>
      </div>

      {/* Search */}
      <div className="relative mb-5 max-w-md">
        <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
        <input
          type="text"
          placeholder="Search by name, email or phone…"
          value={search}
          onChange={e => setSearch(e.target.value)}
          className="input pl-10"
        />
      </div>

      <Alert type="error" message={error} className="mb-4" />

      {loading ? <PageSpinner /> : filtered.length === 0 ? (
        <EmptyState icon={Users} title="No users found" description="Try a different search query." />
      ) : (
        <div className="card overflow-hidden">
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr className="bg-gray-50 text-left border-b border-gray-200">
                  {['User', 'Contact', 'Role', 'Joined', 'Status', 'Actions'].map(h => (
                    <th key={h} className="px-4 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wider">
                      {h}
                    </th>
                  ))}
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100">
                {filtered.map(u => (
                  <tr key={u.id} className={`hover:bg-gray-50 transition-colors ${!u.active ? 'opacity-50' : ''}`}>
                    {/* User */}
                    <td className="px-4 py-3">
                      <div className="flex items-center gap-3">
                        <div className={`w-8 h-8 rounded-full flex items-center justify-center text-white text-xs font-bold flex-shrink-0 ${
                          u.role === 'ADMIN' ? 'bg-purple-600' : 'bg-blue-600'
                        }`}>
                          {u.name?.[0]?.toUpperCase() || '?'}
                        </div>
                        <div>
                          <p className="font-medium text-gray-900">{u.name}</p>
                          <p className="text-xs text-gray-400">ID #{u.id}</p>
                        </div>
                      </div>
                    </td>

                    {/* Contact */}
                    <td className="px-4 py-3 text-gray-600">
                      <p>{u.email || '—'}</p>
                      {u.phoneNumber && <p className="text-xs text-gray-400">{u.phoneNumber}</p>}
                    </td>

                    {/* Role */}
                    <td className="px-4 py-3">
                      <span className={`badge ${u.role === 'ADMIN' ? 'bg-purple-100 text-purple-700' : 'bg-blue-100 text-blue-700'}`}>
                        {u.role === 'ADMIN' && <ShieldCheck className="w-3 h-3 mr-1 inline" />}
                        {u.role}
                      </span>
                    </td>

                    {/* Joined */}
                    <td className="px-4 py-3 text-gray-500 text-xs">
                      {u.createdAt ? format(new Date(u.createdAt), 'MMM d, yyyy') : '—'}
                    </td>

                    {/* Status */}
                    <td className="px-4 py-3">
                      <span className={`badge ${u.active ? 'bg-green-100 text-green-700' : 'bg-gray-100 text-gray-500'}`}>
                        {u.active ? 'Active' : 'Inactive'}
                      </span>
                    </td>

                    {/* Actions */}
                    <td className="px-4 py-3">
                      {u.active && u.role !== 'ADMIN' && (
                        <button
                          onClick={() => setDeactivate(u)}
                          className="flex items-center gap-1 text-xs text-red-600 hover:text-red-700
                                     hover:bg-red-50 px-2 py-1 rounded transition-colors font-medium"
                        >
                          <UserX className="w-3.5 h-3.5" /> Deactivate
                        </button>
                      )}
                      {u.role === 'ADMIN' && (
                        <span className="text-xs text-gray-400 italic">Protected</span>
                      )}
                      {!u.active && (
                        <span className="text-xs text-gray-400 italic">Deactivated</span>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      <ConfirmDialog
        open={!!deactivate}
        onClose={() => setDeactivate(null)}
        onConfirm={handleDeactivate}
        title="Deactivate Account"
        message={`Deactivate ${deactivate?.name}'s account? They will no longer be able to log in.`}
        confirmLabel={deactivating ? 'Deactivating…' : 'Deactivate'}
        danger
      />
    </div>
  );
}
