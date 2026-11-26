import { useCallback, useEffect, useState } from 'react';
import { AlertTriangle, CheckCircle2, MoonStar, Sparkles, Trash2 } from 'lucide-react';
import { extractErrorMessage, sleepApi } from '../services/api';
import StatCard from '../components/StatCard';
import { BarChart } from '../components/Charts';
import LoadingSpinner from '../components/LoadingSpinner';

const defaultTimes = () => {
  const wake = new Date();
  wake.setHours(7, 0, 0, 0);
  const sleep = new Date(wake.getTime() - 7.5 * 3600 * 1000);
  const toLocalInput = (d) =>
    new Date(d.getTime() - d.getTimezoneOffset() * 60000).toISOString().slice(0, 16);
  return { sleepTime: toLocalInput(sleep), wakeUpTime: toLocalInput(wake) };
};

export default function SleepTrackerPage() {
  const [summary, setSummary] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [saving, setSaving] = useState(false);

  const initialTimes = defaultTimes();
  const [form, setForm] = useState({
    sleepTime: initialTimes.sleepTime,
    wakeUpTime: initialTimes.wakeUpTime,
    qualityRating: 4,
    hadLateNightGaming: false,
    notes: '',
  });

  const loadSleep = useCallback(async () => {
    try {
      const data = await sleepApi.getSummary();
      setSummary(data);
      setError('');
    } catch (err) {
      setError(extractErrorMessage(err, 'Unable to load sleep records.'));
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadSleep();
  }, [loadSleep]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSaving(true);
    setError('');
    try {
      await sleepApi.createRecord({
        sleepTime: form.sleepTime,
        wakeUpTime: form.wakeUpTime,
        qualityRating: Number(form.qualityRating),
        hadLateNightGaming: form.hadLateNightGaming,
        notes: form.notes,
      });
      setForm((prev) => ({ ...prev, notes: '' }));
      await loadSleep();
    } catch (err) {
      setError(extractErrorMessage(err, 'Could not save sleep record.'));
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async (id) => {
    try {
      await sleepApi.deleteRecord(id);
      await loadSleep();
    } catch (err) {
      setError(extractErrorMessage(err, 'Could not delete sleep record.'));
    }
  };

  if (loading) return <LoadingSpinner text="Loading sleep & wellness tracker..." />;

  return (
    <div className="flex-col gap-3">
      {error && <div className="alert alert-danger mb-3">{error}</div>}

      {/* AI Suggestion */}
      {summary?.aiSuggestion && (
        <div className="alert alert-info mb-3">
          <Sparkles size={20} color="var(--accent)" style={{ flexShrink: 0 }} />
          <div>
            <div className="font-semibold text-sm">AI Sleep & Recovery Insight</div>
            <div className="text-sm">{summary.aiSuggestion}</div>
          </div>
        </div>
      )}

      {/* KPIs */}
      <div className="grid grid-cols-4 mb-3">
        <StatCard
          label="Latest Sleep"
          value={summary?.latestSleepHours ? `${summary.latestSleepHours}h` : '—'}
          hint="Most recent night"
          icon={MoonStar}
          tone="accent"
        />
        <StatCard
          label="Average Sleep"
          value={summary?.avgSleepHours ? `${summary.avgSleepHours}h` : '—'}
          hint="Target: 7–9 hours"
          icon={MoonStar}
          tone="info"
        />
        <StatCard
          label="Irregular Nights"
          value={summary?.irregularNightsCount ?? 0}
          hint="< 6.5h or late sleep onset"
          icon={AlertTriangle}
          tone={summary?.irregularNightsCount > 0 ? 'warning' : 'success'}
        />
        <StatCard
          label="Late-Night Gaming Impact"
          value={
            summary?.lateNightGamingSleepAvg > 0
              ? `${summary.lateNightGamingSleepAvg}h`
              : 'No data'
          }
          hint={
            summary?.regularSleepAvg > 0
              ? `vs ${summary.regularSleepAvg}h without late gaming`
              : 'Compare rest quality'
          }
          icon={MoonStar}
          tone="warning"
        />
      </div>

      {/* Form + Chart */}
      <div className="grid grid-cols-2 mb-3">
        <div className="card">
          <div className="card-header">
            <div>
              <h2 className="card-title">
                <MoonStar size={18} color="var(--info)" />
                Log Sleep Session
              </h2>
              <p className="card-subtitle">Duration and irregular patterns are calculated automatically</p>
            </div>
          </div>

          <form onSubmit={handleSubmit}>
            <div className="grid grid-cols-2" style={{ gap: '0.75rem' }}>
              <div className="form-group">
                <label className="form-label">Sleep Time *</label>
                <input
                  type="datetime-local"
                  className="form-input"
                  value={form.sleepTime}
                  onChange={(e) => setForm({ ...form, sleepTime: e.target.value })}
                  required
                />
              </div>
              <div className="form-group">
                <label className="form-label">Wake-Up Time *</label>
                <input
                  type="datetime-local"
                  className="form-input"
                  value={form.wakeUpTime}
                  onChange={(e) => setForm({ ...form, wakeUpTime: e.target.value })}
                  required
                />
              </div>
            </div>

            <div className="grid grid-cols-2" style={{ gap: '0.75rem' }}>
              <div className="form-group">
                <label className="form-label">Sleep Quality (1–5)</label>
                <select
                  className="form-select"
                  value={form.qualityRating}
                  onChange={(e) => setForm({ ...form, qualityRating: e.target.value })}
                >
                  <option value={5}>5 — Very Rested</option>
                  <option value={4}>4 — Good</option>
                  <option value={3}>3 — Fair</option>
                  <option value={2}>2 — Restless</option>
                  <option value={1}>1 — Exhausted</option>
                </select>
              </div>
              <div className="form-group" style={{ justifyContent: 'flex-end' }}>
                <label
                  className="flex items-center gap-2 text-sm"
                  style={{
                    padding: '0.65rem 0.85rem',
                    borderRadius: 10,
                    background: 'var(--bg-surface-alt)',
                    border: '1px solid var(--border-color)',
                    cursor: 'pointer',
                  }}
                >
                  <input
                    type="checkbox"
                    checked={form.hadLateNightGaming}
                    onChange={(e) => setForm({ ...form, hadLateNightGaming: e.target.checked })}
                  />
                  <span>Gamed late before bed</span>
                </label>
              </div>
            </div>

            <div className="form-group">
              <label className="form-label">Notes (Optional)</label>
              <input
                type="text"
                className="form-input"
                placeholder="e.g. Stopped gaming at 10:30 PM, felt refreshed"
                value={form.notes}
                onChange={(e) => setForm({ ...form, notes: e.target.value })}
              />
            </div>

            <button type="submit" className="btn btn-primary" style={{ width: '100%' }} disabled={saving}>
              {saving ? 'Saving...' : 'Save Sleep Record'}
            </button>
          </form>
        </div>

        <div className="card">
          <div className="card-header">
            <div>
              <h3 className="card-title">Weekly Sleep Duration (Hours)</h3>
              <p className="card-subtitle">Last 7 days compared against an 8-hour wellness target</p>
            </div>
          </div>
          <BarChart data={summary?.weeklyChart || []} unit="h" barColor="var(--info)" targetLine={8} />

          <div className="mt-3">
            <h4 className="font-semibold text-sm mb-2">Daily Student Wellness Habits</h4>
            <div className="flex-col gap-1">
              {(summary?.dailyRecommendations || []).map((tip, idx) => (
                <div key={idx} className="flex items-center gap-2 text-xs text-muted">
                  <CheckCircle2 size={14} color="var(--success)" style={{ flexShrink: 0 }} />
                  <span>{tip}</span>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>

      {/* Sleep History */}
      <div className="card">
        <div className="card-header">
          <h3 className="card-title">Sleep History</h3>
        </div>
        {!summary?.records?.length ? (
          <div className="empty-state text-sm">No sleep logs yet. Record your sleep above to monitor patterns.</div>
        ) : (
          <div className="table-wrap">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Date</th>
                  <th>Sleep Time</th>
                  <th>Wake-Up Time</th>
                  <th>Duration</th>
                  <th>Quality</th>
                  <th>Pattern & Late Gaming</th>
                  <th>Action</th>
                </tr>
              </thead>
              <tbody>
                {summary.records.map((rec) => (
                  <tr key={rec.id}>
                    <td className="font-semibold" style={{ color: 'var(--text-primary)' }}>{rec.recordDate}</td>
                    <td>{new Date(rec.sleepTime).toLocaleString()}</td>
                    <td>{new Date(rec.wakeUpTime).toLocaleString()}</td>
                    <td className="font-semibold">{rec.totalDurationHours} hrs</td>
                    <td>{rec.qualityRating}/5</td>
                    <td>
                      <div className="flex items-center gap-1">
                        <span className={`badge ${rec.irregularPattern ? 'badge-warning' : 'badge-success'}`}>
                          {rec.irregularPattern ? 'Irregular' : 'Steady'}
                        </span>
                        {rec.hadLateNightGaming && <span className="badge badge-danger">Late Gaming</span>}
                      </div>
                    </td>
                    <td>
                      <button type="button" className="icon-btn" onClick={() => handleDelete(rec.id)} title="Delete record">
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
