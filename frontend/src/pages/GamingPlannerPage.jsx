import { useCallback, useEffect, useState } from 'react';
import { AlertTriangle, CalendarClock, CheckCircle2, Plus, Settings, Trash2 } from 'lucide-react';
import { extractErrorMessage, plannerApi } from '../services/api';
import { useAuth } from '../context/AuthContext';
import StatCard from '../components/StatCard';
import { DualBarChart } from '../components/Charts';
import LoadingSpinner from '../components/LoadingSpinner';

export default function GamingPlannerPage() {
  const { user, updateUser } = useAuth();
  const [planner, setPlanner] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [successMsg, setSuccessMsg] = useState('');

  const [settingsForm, setSettingsForm] = useState({
    dailyGamingLimitMinutes: 120,
    preferredGamingStart: '18:00',
    preferredGamingEnd: '21:00',
  });

  const [planForm, setPlanForm] = useState({
    gameName: 'Valorant',
    planDate: new Date().toISOString().slice(0, 10),
    startTime: '19:00',
    endTime: '20:00',
    notes: '',
  });

  const [savingSettings, setSavingSettings] = useState(false);
  const [savingPlan, setSavingPlan] = useState(false);

  const loadPlanner = useCallback(async () => {
    try {
      const data = await plannerApi.getOverview();
      setPlanner(data);
      setSettingsForm({
        dailyGamingLimitMinutes: data.dailyLimitMinutes,
        preferredGamingStart: data.preferredGamingStart,
        preferredGamingEnd: data.preferredGamingEnd,
      });
      setError('');
    } catch (err) {
      setError(extractErrorMessage(err, 'Unable to load gaming planner.'));
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadPlanner();
  }, [loadPlanner]);

  const handleSaveSettings = async (e) => {
    e.preventDefault();
    setSavingSettings(true);
    setError('');
    setSuccessMsg('');
    try {
      const updated = await plannerApi.updateSettings({
        dailyGamingLimitMinutes: Number(settingsForm.dailyGamingLimitMinutes),
        preferredGamingStart: settingsForm.preferredGamingStart,
        preferredGamingEnd: settingsForm.preferredGamingEnd,
      });
      setPlanner(updated);
      if (user) {
        updateUser({
          ...user,
          dailyGamingLimitMinutes: updated.dailyLimitMinutes,
          preferredGamingStart: updated.preferredGamingStart,
          preferredGamingEnd: updated.preferredGamingEnd,
        });
      }
      setSuccessMsg('Daily gaming limit and preferred gaming window updated.');
    } catch (err) {
      setError(extractErrorMessage(err, 'Unable to save settings.'));
    } finally {
      setSavingSettings(false);
    }
  };

  const handleCreatePlan = async (e) => {
    e.preventDefault();
    setSavingPlan(true);
    setError('');
    setSuccessMsg('');
    try {
      await plannerApi.createPlan(planForm);
      setSuccessMsg('Gaming session scheduled successfully without conflicts.');
      setPlanForm((prev) => ({ ...prev, notes: '' }));
      await loadPlanner();
    } catch (err) {
      setError(extractErrorMessage(err, 'Failed to schedule gaming session.'));
    } finally {
      setSavingPlan(false);
    }
  };

  const handleDeletePlan = async (id) => {
    setError('');
    try {
      await plannerApi.deletePlan(id);
      await loadPlanner();
    } catch (err) {
      setError(extractErrorMessage(err, 'Could not delete planned session.'));
    }
  };

  if (loading) return <LoadingSpinner text="Loading smart gaming planner..." />;

  return (
    <div className="flex-col gap-3">
      {error && <div className="alert alert-danger mb-3">{error}</div>}
      {successMsg && <div className="alert alert-success mb-3">{successMsg}</div>}

      {planner?.exceededLimit && (
        <div className="alert alert-warning mb-3">
          <AlertTriangle size={20} color="var(--warning)" style={{ flexShrink: 0 }} />
          <div>
            <strong>Daily Gaming Target Exceeded:</strong> Tracked gaming today ({planner.todayActualMinutes} mins)
            has passed your {planner.dailyLimitMinutes}-min budget. Protect your sleep and study blocks!
          </div>
        </div>
      )}

      {/* KPIs */}
      <div className="grid grid-cols-4 mb-3">
        <StatCard label="Daily Limit" value={`${planner?.dailyLimitMinutes ?? 120}m`} hint="Customizable target" icon={Settings} tone="accent" />
        <StatCard label="Planned Today" value={`${planner?.todayPlannedMinutes ?? 0}m`} hint="Scheduled slots" icon={CalendarClock} tone="info" />
        <StatCard label="Tracked Today" value={`${planner?.todayActualMinutes ?? 0}m`} hint="Actual playtime" icon={CalendarClock} tone={planner?.exceededLimit ? 'danger' : 'warning'} />
        <StatCard label="Available Today" value={`${planner?.availableMinutesToday ?? 0}m`} hint="Remaining budget" icon={CheckCircle2} tone="success" />
      </div>

      {/* Forms */}
      <div className="grid grid-cols-2 mb-3">
        <div className="card">
          <div className="card-header">
            <div>
              <h2 className="card-title">
                <Settings size={18} color="var(--accent)" />
                Daily Limit & Preferred Window
              </h2>
              <p className="card-subtitle">Set healthy boundaries that fit around your classes</p>
            </div>
          </div>

          <form onSubmit={handleSaveSettings}>
            <div className="form-group">
              <label className="form-label">Daily Gaming Limit (Minutes)</label>
              <input
                type="number"
                min="15"
                max="720"
                className="form-input"
                value={settingsForm.dailyGamingLimitMinutes}
                onChange={(e) => setSettingsForm({ ...settingsForm, dailyGamingLimitMinutes: e.target.value })}
                required
              />
            </div>

            <div className="grid grid-cols-2" style={{ gap: '0.75rem' }}>
              <div className="form-group">
                <label className="form-label">Preferred Start Time</label>
                <input
                  type="time"
                  className="form-input"
                  value={settingsForm.preferredGamingStart}
                  onChange={(e) => setSettingsForm({ ...settingsForm, preferredGamingStart: e.target.value })}
                  required
                />
              </div>
              <div className="form-group">
                <label className="form-label">Preferred End Time</label>
                <input
                  type="time"
                  className="form-input"
                  value={settingsForm.preferredGamingEnd}
                  onChange={(e) => setSettingsForm({ ...settingsForm, preferredGamingEnd: e.target.value })}
                  required
                />
              </div>
            </div>

            <button type="submit" className="btn btn-primary" disabled={savingSettings}>
              {savingSettings ? 'Updating...' : 'Save Gaming Preferences'}
            </button>
          </form>
        </div>

        <div className="card">
          <div className="card-header">
            <div>
              <h2 className="card-title">
                <Plus size={18} color="var(--info)" />
                Schedule Gaming Session
              </h2>
              <p className="card-subtitle">Overlapping time slots on the same date are blocked automatically</p>
            </div>
          </div>

          <form onSubmit={handleCreatePlan}>
            <div className="grid grid-cols-2" style={{ gap: '0.75rem' }}>
              <div className="form-group">
                <label className="form-label">Game Name *</label>
                <input
                  type="text"
                  className="form-input"
                  value={planForm.gameName}
                  onChange={(e) => setPlanForm({ ...planForm, gameName: e.target.value })}
                  required
                />
              </div>
              <div className="form-group">
                <label className="form-label">Date *</label>
                <input
                  type="date"
                  className="form-input"
                  value={planForm.planDate}
                  onChange={(e) => setPlanForm({ ...planForm, planDate: e.target.value })}
                  required
                />
              </div>
            </div>

            <div className="grid grid-cols-2" style={{ gap: '0.75rem' }}>
              <div className="form-group">
                <label className="form-label">Start Time *</label>
                <input
                  type="time"
                  className="form-input"
                  value={planForm.startTime}
                  onChange={(e) => setPlanForm({ ...planForm, startTime: e.target.value })}
                  required
                />
              </div>
              <div className="form-group">
                <label className="form-label">End Time *</label>
                <input
                  type="time"
                  className="form-input"
                  value={planForm.endTime}
                  onChange={(e) => setPlanForm({ ...planForm, endTime: e.target.value })}
                  required
                />
              </div>
            </div>

            <div className="form-group">
              <label className="form-label">Notes (Optional)</label>
              <input
                type="text"
                className="form-input"
                placeholder="e.g. Post-study co-op match"
                value={planForm.notes}
                onChange={(e) => setPlanForm({ ...planForm, notes: e.target.value })}
              />
            </div>

            <button type="submit" className="btn btn-primary" disabled={savingPlan}>
              <CalendarClock size={16} />
              <span>{savingPlan ? 'Scheduling...' : 'Add to Gaming Schedule'}</span>
            </button>
          </form>
        </div>
      </div>

      {/* Chart + Recommendations */}
      <div className="grid grid-cols-2 mb-3">
        <div className="card">
          <div className="card-header">
            <div>
              <h3 className="card-title">Planned vs. Actual Gaming (Minutes)</h3>
              <p className="card-subtitle">7-day comparison of planned slots vs tracked sessions</p>
            </div>
          </div>
          <DualBarChart data={planner?.plannedVsActualChart || []} primaryLabel="Planned (min)" secondaryLabel="Actual (min)" />
        </div>

        <div className="card">
          <div className="card-header">
            <h3 className="card-title">Balanced Study & Gaming Recommendations</h3>
          </div>
          <div className="flex-col gap-2">
            {(planner?.recommendations || []).map((rec, idx) => (
              <div
                key={idx}
                className="flex items-center gap-2 text-sm"
                style={{ padding: '0.65rem 0.85rem', borderRadius: 10, background: 'var(--bg-surface-alt)' }}
              >
                <CheckCircle2 size={16} color="var(--success)" style={{ flexShrink: 0 }} />
                <span>{rec}</span>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* Scheduled Sessions List */}
      <div className="card">
        <div className="card-header">
          <h3 className="card-title">Scheduled Gaming Sessions</h3>
        </div>
        {!planner?.plans?.length ? (
          <div className="empty-state text-sm">
            No gaming sessions scheduled yet. Plan an intentional slot above to balance study and play.
          </div>
        ) : (
          <div className="table-wrap">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Game</th>
                  <th>Date</th>
                  <th>Window</th>
                  <th>Planned Duration</th>
                  <th>Notes</th>
                  <th>Action</th>
                </tr>
              </thead>
              <tbody>
                {planner.plans.map((p) => (
                  <tr key={p.id}>
                    <td className="font-semibold" style={{ color: 'var(--text-primary)' }}>{p.gameName}</td>
                    <td>{p.planDate}</td>
                    <td>{p.startTime?.slice(0, 5)} – {p.endTime?.slice(0, 5)}</td>
                    <td><span className="badge badge-accent">{p.plannedDurationMinutes} mins</span></td>
                    <td>{p.notes || '—'}</td>
                    <td>
                      <button type="button" className="icon-btn" onClick={() => handleDeletePlan(p.id)} title="Remove plan">
                        <Trash2 size={15} color="var(--danger)" />
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
}
