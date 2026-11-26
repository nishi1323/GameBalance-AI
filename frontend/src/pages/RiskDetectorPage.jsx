import { useEffect, useState } from 'react';
import { AlertCircle, CheckCircle2, Info, ShieldAlert, Sparkles } from 'lucide-react';
import { extractErrorMessage, riskApi } from '../services/api';
import { RiskGauge } from '../components/Charts';
import ProgressBar from '../components/ProgressBar';
import LoadingSpinner from '../components/LoadingSpinner';

const INITIAL_FORM = {
  avgDailyGamingHours: 2.5,
  weekendGamingHours: 4.0,
  avgSleepDuration: 7.0,
  lateNightGamingSessions: 1,
  missedStudyTasks: 1,
  affectsAcademics: false,
  difficultToStop: false,
  affectsPhysicalOrSocial: false,
};

export default function RiskDetectorPage() {
  const [form, setForm] = useState(INITIAL_FORM);
  const [result, setResult] = useState(null);
  const [history, setHistory] = useState([]);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    Promise.all([riskApi.getLatest().catch(() => null), riskApi.getHistory().catch(() => [])])
      .then(([latest, hist]) => {
        if (latest) setResult(latest);
        if (Array.isArray(hist)) setHistory(hist);
      })
      .finally(() => setLoading(false));
  }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSubmitting(true);
    try {
      const payload = {
        avgDailyGamingHours: Number(form.avgDailyGamingHours),
        weekendGamingHours: Number(form.weekendGamingHours),
        avgSleepDuration: Number(form.avgSleepDuration),
        lateNightGamingSessions: Number(form.lateNightGamingSessions),
        missedStudyTasks: Number(form.missedStudyTasks),
        affectsAcademics: Boolean(form.affectsAcademics),
        difficultToStop: Boolean(form.difficultToStop),
        affectsPhysicalOrSocial: Boolean(form.affectsPhysicalOrSocial),
      };
      const saved = await riskApi.analyze(payload);
      setResult(saved);
      setHistory((prev) => [saved, ...prev]);
    } catch (err) {
      setError(extractErrorMessage(err, 'Unable to analyze gaming risk right now.'));
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) return <LoadingSpinner text="Loading AI Risk Detector..." />;

  return (
    <div className="flex-col gap-3">
      <div className="alert alert-info mb-3">
        <Info size={20} color="var(--info)" style={{ flexShrink: 0 }} />
        <div className="text-sm">
          <strong>Educational Wellness Estimate Only:</strong> This module combines rule-based habit scoring with Groq AI
          to help students reflect on gaming balance. It does <strong>not</strong> provide a medical or clinical diagnosis.
        </div>
      </div>

      {error && <div className="alert alert-danger mb-3">{error}</div>}

      <div className="grid grid-cols-2 mb-3">
        {/* Questionnaire Form */}
        <div className="card">
          <div className="card-header">
            <div>
              <h2 className="card-title">
                <ShieldAlert size={18} color="var(--accent)" />
                Habit & Wellness Self-Check
              </h2>
              <p className="card-subtitle">Answer honestly for the most helpful personalized feedback</p>
            </div>
          </div>

          <form onSubmit={handleSubmit}>
            <div className="grid grid-cols-2" style={{ gap: '0.75rem' }}>
              <div className="form-group">
                <label className="form-label">Avg Daily Gaming (Hours)</label>
                <input
                  type="number"
                  step="0.5"
                  min="0"
                  max="24"
                  className="form-input"
                  value={form.avgDailyGamingHours}
                  onChange={(e) => setForm({ ...form, avgDailyGamingHours: e.target.value })}
                  required
                />
              </div>
              <div className="form-group">
                <label className="form-label">Weekend Gaming (Hours/Day)</label>
                <input
                  type="number"
                  step="0.5"
                  min="0"
                  max="24"
                  className="form-input"
                  value={form.weekendGamingHours}
                  onChange={(e) => setForm({ ...form, weekendGamingHours: e.target.value })}
                  required
                />
              </div>
            </div>

            <div className="grid grid-cols-3" style={{ gap: '0.75rem' }}>
              <div className="form-group">
                <label className="form-label">Avg Sleep (Hours)</label>
                <input
                  type="number"
                  step="0.5"
                  min="0"
                  max="24"
                  className="form-input"
                  value={form.avgSleepDuration}
                  onChange={(e) => setForm({ ...form, avgSleepDuration: e.target.value })}
                  required
                />
              </div>
              <div className="form-group">
                <label className="form-label">Late-Night Sessions/Wk</label>
                <input
                  type="number"
                  min="0"
                  max="30"
                  className="form-input"
                  value={form.lateNightGamingSessions}
                  onChange={(e) => setForm({ ...form, lateNightGamingSessions: e.target.value })}
                  required
                />
              </div>
              <div className="form-group">
                <label className="form-label">Missed Study Tasks</label>
                <input
                  type="number"
                  min="0"
                  max="100"
                  className="form-input"
                  value={form.missedStudyTasks}
                  onChange={(e) => setForm({ ...form, missedStudyTasks: e.target.value })}
                  required
                />
              </div>
            </div>

            <div className="flex-col gap-2 mb-3" style={{ marginTop: '0.5rem' }}>
              {[
                { key: 'affectsAcademics', label: 'Gaming currently affects my academic performance or study focus' },
                { key: 'difficultToStop', label: 'I often find it difficult to stop gaming once I start' },
                { key: 'affectsPhysicalOrSocial', label: 'Gaming reduces my physical activity or offline social time' },
              ].map((item) => (
                <label
                  key={item.key}
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
                    checked={form[item.key]}
                    onChange={(e) => setForm({ ...form, [item.key]: e.target.checked })}
                  />
                  <span>{item.label}</span>
                </label>
              ))}
            </div>

            <button type="submit" className="btn btn-primary" style={{ width: '100%' }} disabled={submitting}>
              <Sparkles size={16} />
              <span>{submitting ? 'Analyzing with AI...' : 'Analyze Wellness Risk'}</span>
            </button>
          </form>
        </div>

        {/* Assessment Result */}
        <div className="card">
          <div className="card-header">
            <div>
              <h2 className="card-title">Assessment Result</h2>
              <p className="card-subtitle">
                {result
                  ? `Evaluated on ${new Date(result.createdAt).toLocaleString()}`
                  : 'Submit the form to see your educational wellness score'}
              </p>
            </div>
            {result && (
              <span className={`badge ${result.aiPowered ? 'badge-accent' : 'badge-neutral'}`}>
                {result.aiPowered ? 'Groq AI + Rule Engine' : 'Rule-Based Estimate'}
              </span>
            )}
          </div>

          {!result ? (
            <div className="empty-state">
              No risk assessment yet. Complete the habit check on the left to generate your personalized report.
            </div>
          ) : (
            <div className="flex-col gap-3">
              <RiskGauge score={result.riskScore} category={result.riskCategory} />

              <ProgressBar
                value={result.riskScore}
                max={100}
                color={
                  result.riskCategory === 'LOW'
                    ? 'var(--success)'
                    : result.riskCategory === 'MODERATE'
                    ? 'var(--warning)'
                    : 'var(--danger)'
                }
                label="Wellness Risk Indicator"
                rightText={`${result.riskScore} / 100`}
              />

              <div>
                <h4 className="font-semibold text-sm mb-1">Personalized Explanation</h4>
                <p className="text-sm text-secondary" style={{ color: 'var(--text-secondary)' }}>
                  {result.explanation}
                </p>
              </div>

              <div>
                <h4 className="font-semibold text-sm mb-1">Identified Gaming Patterns</h4>
                <div className="flex-col gap-1">
                  {(result.unhealthyPatterns || []).map((p, idx) => (
                    <div key={idx} className="flex items-center gap-2 text-sm text-muted">
                      <AlertCircle size={15} color="var(--warning)" style={{ flexShrink: 0 }} />
                      <span>{p}</span>
                    </div>
                  ))}
                </div>
              </div>

              <div>
                <h4 className="font-semibold text-sm mb-1">Possible Sleep & Academic Impact</h4>
                <p className="text-sm text-muted">{result.sleepAndAcademicImpact}</p>
              </div>

              <div>
                <h4 className="font-semibold text-sm mb-1">Recommended Next Steps</h4>
                <div className="flex-col gap-1">
                  {(result.recommendations || []).map((r, idx) => (
                    <div key={idx} className="flex items-center gap-2 text-sm">
                      <CheckCircle2 size={15} color="var(--success)" style={{ flexShrink: 0 }} />
                      <span>{r}</span>
                    </div>
                  ))}
                </div>
              </div>

              <div className="text-xs text-muted" style={{ borderTop: '1px solid var(--border-color)', paddingTop: '0.75rem' }}>
                {result.disclaimer}
              </div>
            </div>
          )}
        </div>
      </div>

      {/* History */}
      {history.length > 0 && (
        <div className="card">
          <div className="card-header">
            <h3 className="card-title">Past Wellness Risk Assessments</h3>
          </div>
          <div className="table-wrap">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Date</th>
                  <th>Score</th>
                  <th>Category</th>
                  <th>Daily Gaming</th>
                  <th>Avg Sleep</th>
                  <th>Late-Night/Wk</th>
                </tr>
              </thead>
              <tbody>
                {history.map((item) => (
                  <tr key={item.id}>
                    <td>{new Date(item.createdAt).toLocaleString()}</td>
                    <td className="font-semibold">{item.riskScore}/100</td>
                    <td>
                      <span
                        className={`badge ${
                          item.riskCategory === 'LOW'
                            ? 'badge-success'
                            : item.riskCategory === 'MODERATE'
                            ? 'badge-warning'
                            : 'badge-danger'
                        }`}
                      >
                        {item.riskCategory.replace('_', ' ')}
                      </span>
                    </td>
                    <td>{item.avgDailyGamingHours} hrs</td>
                    <td>{item.avgSleepDuration} hrs</td>
                    <td>{item.lateNightGamingSessions}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  );
}
