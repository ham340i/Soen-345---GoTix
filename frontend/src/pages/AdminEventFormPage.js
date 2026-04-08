import React, { useState, useEffect } from 'react';
import { useNavigate, useParams, Link } from 'react-router-dom';
import { ArrowLeft, Save } from 'lucide-react';
import { eventsAPI } from '../services/api';
import { PageSpinner, Alert, FormField } from '../components/common/UI';
import toast from 'react-hot-toast';
import { format } from 'date-fns';

const CATEGORIES = ['MOVIE','CONCERT','SPORTS','TRAVEL','THEATER','CONFERENCE','OTHER'];

const EMPTY = {
  name: '', description: '', category: 'CONCERT',
  location: '', eventDate: '', totalSeats: 100, price: '',
};

function toLocalDatetime(iso) {
  if (!iso) return '';
  const d = new Date(iso);
  return format(d, "yyyy-MM-dd'T'HH:mm");
}

export default function AdminEventFormPage() {
  const { id }    = useParams();
  const isEdit    = !!id;
  const navigate  = useNavigate();

  const [form,    setForm]    = useState(EMPTY);
  const [loading, setLoading] = useState(isEdit);
  const [saving,  setSaving]  = useState(false);
  const [error,   setError]   = useState('');

  useEffect(() => {
    if (!isEdit) return;
    eventsAPI.getById(id)
      .then(r => {
        const ev = r.data;
        setForm({
          name:        ev.name        || '',
          description: ev.description || '',
          category:    ev.category    || 'CONCERT',
          location:    ev.location    || '',
          eventDate:   toLocalDatetime(ev.eventDate),
          totalSeats:  ev.totalSeats  || 100,
          price:       ev.price       || '',
        });
      })
      .catch(() => setError('Failed to load event.'))
      .finally(() => setLoading(false));
  }, [id, isEdit]);

  const handle = (e) => {
    const { name, value, type } = e.target;
    setForm(f => ({ ...f, [name]: type === 'number' ? Number(value) : value }));
  };

  const submit = async (e) => {
    e.preventDefault();
    setError('');

    if (!form.name.trim())     { setError('Event name is required.');    return; }
    if (!form.location.trim()) { setError('Location is required.');       return; }
    if (!form.eventDate)       { setError('Event date is required.');     return; }
    if (!form.price || form.price <= 0) { setError('Price must be positive.'); return; }
    if (form.totalSeats <= 0)  { setError('Total seats must be positive.'); return; }

    setSaving(true);
    try {
      const payload = {
        ...form,
        eventDate:  new Date(form.eventDate).toISOString(),
        price:      parseFloat(form.price),
        totalSeats: parseInt(form.totalSeats),
      };

      if (isEdit) {
        await eventsAPI.update(id, payload);
        toast.success('Event updated successfully!');
      } else {
        await eventsAPI.create(payload);
        toast.success('Event created successfully!');
      }
      navigate('/admin/events');
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to save event.');
    } finally { setSaving(false); }
  };

  if (loading) return <PageSpinner />;

  return (
    <div className="page-container max-w-3xl">
      <Link to="/admin/events"
        className="inline-flex items-center gap-1.5 text-sm text-gray-500 hover:text-gray-900 mb-6">
        <ArrowLeft className="w-4 h-4" /> Back to Events
      </Link>

      <div className="card p-8">
        <h1 className="section-title mb-1">{isEdit ? 'Edit Event' : 'Create New Event'}</h1>
        <p className="text-gray-500 text-sm mb-6">
          {isEdit ? 'Update the event details below.' : 'Fill in the details to create a new event.'}
        </p>

        <Alert type="error" message={error} className="mb-5" />

        <form onSubmit={submit} className="space-y-5">
          {/* Name */}
          <FormField label="Event Name" required>
            <input name="name" value={form.name} onChange={handle}
              placeholder="e.g. Jazz Festival 2026" className="input" required />
          </FormField>

          {/* Description */}
          <FormField label="Description">
            <textarea name="description" value={form.description} onChange={handle}
              placeholder="Describe the event…"
              rows={3} className="input resize-none" />
          </FormField>

          {/* Category + Location */}
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <FormField label="Category" required>
              <select name="category" value={form.category} onChange={handle} className="input">
                {CATEGORIES.map(c => <option key={c} value={c}>{c}</option>)}
              </select>
            </FormField>
            <FormField label="Location" required>
              <input name="location" value={form.location} onChange={handle}
                placeholder="e.g. Montreal Bell Centre" className="input" required />
            </FormField>
          </div>

          {/* Date + Seats + Price */}
          <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
            <FormField label="Event Date & Time" required>
              <input name="eventDate" type="datetime-local" value={form.eventDate}
                onChange={handle} className="input" required />
            </FormField>
            <FormField label="Total Seats" required>
              <input name="totalSeats" type="number" min={1} value={form.totalSeats}
                onChange={handle} className="input" required />
            </FormField>
            <FormField label="Price per Ticket ($)" required>
              <input name="price" type="number" min={0} step="0.01" value={form.price}
                onChange={handle} placeholder="49.99" className="input" required />
            </FormField>
          </div>

          {/* Preview */}
          {(form.name || form.price) && (
            <div className="bg-blue-50 rounded-xl p-4 border border-blue-100">
              <p className="text-xs font-semibold text-blue-600 mb-2 uppercase tracking-wider">Preview</p>
              <p className="font-semibold text-gray-900">{form.name || 'Event Name'}</p>
              <p className="text-sm text-gray-500">{form.location || 'Location'}</p>
              <p className="text-sm text-blue-700 font-medium mt-1">
                ${parseFloat(form.price || 0).toFixed(2)} · {form.totalSeats} seats · {form.category}
              </p>
            </div>
          )}

          {/* Actions */}
          <div className="flex gap-3 pt-2">
            <Link to="/admin/events" className="btn-secondary flex-1 text-center text-sm">Cancel</Link>
            <button type="submit" disabled={saving}
              className="btn-primary flex-1 text-sm flex items-center justify-center gap-2">
              <Save className="w-4 h-4" />
              {saving ? 'Saving…' : isEdit ? 'Update Event' : 'Create Event'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
