import { useState } from 'react';
import { Save, UserCircle } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { authApi, extractErrorMessage } from '../services/api';

export default function ProfilePage() {
  const { user, updateUser } = useAuth();
  const [form, setForm] = useState({
    fullName: user?.fullName || '',
    age: user?.age || 20,
    collegeName: user?.collegeName || '',
    dailyGamingLimitMinutes: user?.dailyGamingLimitMinutes || 120,
    preferredGamingStart: user?.preferredGamingStart || '18:00',
    preferredGamingEnd: user?.preferredGamingEnd || '21:00',
  });
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSaving(true);
    setError('');
    setSuccess('');
    try {
      const updated = await authApi.updateProfile({
        ...form,
        age: Number(form.age),
        dailyGamingLimitMinutes: Number(form.dailyGamingLimitMinutes),
      });
      updateUser(updated);
      setSuccess('Profile and wellness preferences updated.');
    } catch (err) {
      setError(extractErrorMessage(err, 'Unable to update profile.'));
    } finally {
      setSaving(false);
    }
  };

  return (
    <div style={{ maxWidth: '720px' }}>
      {error && <div className="alert alert-danger mb-3">{error}</div>}
      {success && <div className="alert alert-success mb-3">{success}</div>}

      <div className="card">
        <div className="card-header">
          <div className="flex items-center gap-2">
            <UserCircle size={22} color="var(--accent)" />
            <div>
              <h2 className="card-title">Student Profile & Wellness Limits</h2>
              <p className="card-subtitle">Signed in as {user?.email}</p>
            </div>
          </div>
        </div>

        <form onSubmit={handleSubmit}>
          <div className="grid grid-cols-2" style={{ gap: '0.85rem' }}>
            <div className="form-group">
              <label className="form-label">Full Name *</label>
              <input
                type="text"
                className="form-input"
                value={form.fullName}
                onChange={(e) => setForm({ ...form, fullName: e.target.value })}
                required
              />
            </div>

            <div className="form-group">
              <label className="form-label">Age *</label>
              <input
                type="number"
                min="13"
                max="100"
                className="form-input"
                value={form.age}
                onChange={(e) => setForm({ ...form, age: e.target.value })}
                required
              />
            </div>
          </div>

          <div className="form-group">
            <label className="form-label">Student / College Name</label>
            <input
              type="text"
              className="form-input"
              placeholder="Optional"
              value={form.collegeName}
              onChange={(e) => setForm({ ...form, collegeName: e.target.value })}
            />
          </div>

          <div className="grid grid-cols-3" style={{ gap: '0.85rem' }}>
            <div className="form-group">
              <label className="form-label">Daily Gaming Limit (min)</label>
              <input
                type="number"
                min="15"
                max="720"
                className="form-input"
                value={form.dailyGamingLimitMinutes}
                onChange={(e) => setForm({ ...form, dailyGamingLimitMinutes: e.target.value })}
                required
              />
            </div>
            <div className="form-group">
              <label className="form-label">Preferred Start</label>
              <input
                type="time"
                className="form-input"
                value={form.preferredGamingStart}
                onChange={(e) => setForm({ ...form, preferredGamingStart: e.target.value })}
              />
            </div>
            <div className="form-group">
              <label className="form-label">Preferred End</label>
              <input
                type="time"
                className="form-input"
                value={form.preferredGamingEnd}
                onChange={(e) => setForm({ ...form, preferredGamingEnd: e.target.value })}
              />
            </div>
          </div>

          <button type="submit" className="btn btn-primary" disabled={saving}>
            <Save size={16} />
            <span>{saving ? 'Saving...' : 'Save Profile Changes'}</span>
          </button>
        </form>
      </div>
    </div>
  );
}
