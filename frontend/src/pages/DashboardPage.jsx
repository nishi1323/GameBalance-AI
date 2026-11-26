import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import {
  Activity,
  BookOpenCheck,
  Clock,
  Gamepad2,
  MoonStar,
  ShieldAlert,
  Sparkles,
  Timer,
} from 'lucide-react';
import { dashboardApi, extractErrorMessage } from '../services/api';
import StatCard from '../components/StatCard';
import ProgressBar from '../components/ProgressBar';
import { BarChart } from '../components/Charts';
import LoadingSpinner from '../components/LoadingSpinner';

const formatMinutes = (mins = 0) => {
  const h = Math.floor(mins / 60);
  const m = mins % 60;
  return h > 0 ? `${h}h ${m}m` : `${m}m`;
};

export default function DashboardPage() {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    let active = true;
    dashboardApi
      .getOverview()
      .then((res) => {
        if (active) setData(res);
      })
      .catch((err) => {
        if (active) setError(extractErrorMessage(err, 'Failed to load dashboard overview.'));
      })
      .finally(() => {
        if (active) setLoading(false);
      });
    return () => {
      active = false;
    };
  }, []);

  if (loading) return <LoadingSpinner text="Loading your daily wellness overview..." />;
  if (error) return <div className="alert alert-danger">{error}</div>;
  if (!data) return null;

  const riskBadgeClass =
    data.riskLevel === 'LOW'
      ? 'badge-success'
      : data.riskLevel === 'MODERATE'
      ? 'badge-warning'
      : data.riskLevel === 'HIGH' || data.riskLevel === 'VERY_HIGH'
      ? 'badge-danger'
      : 'badge-neutral';

  return (
    <div className="flex-col gap-3">
      {/* AI Daily Wellness Suggestion */}
      <div className="alert alert-info mb-3" style={{ alignItems: 'center' }}>
        <Sparkles size={22} color="var(--accent)" style={{ flexShrink: 0 }} />
        <div style={{ flex: 1 }}>
          <div className="font-semibold" style={{ marginBottom: '0.2rem' }}>
            AI Daily Wellness Suggestion
          </div>
          <div className="text-sm">{data.aiWellnessSuggestion}</div>
        </div>
        <Link to="/assistant" className="btn btn-primary btn-sm">
          Ask AI Coach
        </Link>
      </div>

      {/* Top Statistic Cards */}
      <div className="grid grid-cols-5 mb-3">
        <StatCard
          label="Today's Gaming Time"
          value={formatMinutes(data.todayGamingMinutes)}
          hint={data.gamingLimitExceeded ? 'Daily limit exceeded!' : 'Tracked today'}
          icon={Gamepad2}
          tone={data.gamingLimitExceeded ? 'danger' : 'accent'}
        />
        <StatCard
          label="Daily Gaming Limit"
          value={formatMinutes(data.dailyGamingLimitMinutes)}
          hint="Customizable in Planner"
          icon={Timer}
          tone="info"
        />
        <StatCard
          label="Remaining Budget"
          value={formatMinutes(data.remainingGamingMinutes)}
          hint={data.gamingLimitExceeded ? 'Take a healthy break' : 'Left for today'}
          icon={Clock}
          tone={data.remainingGamingMinutes === 0 ? 'warning' : 'success'}
        />
        <StatCard
          label="Weekly Study Hours"
          value={`${data.totalStudyHours}h`}
          hint={`Today: ${data.todayStudyHours}h completed`}
          icon={BookOpenCheck}
          tone="success"
        />
        <StatCard
          label="Sleep & Risk Level"
          value={data.latestSleepHours > 0 ? `${data.latestSleepHours}h` : 'No logs'}
          hint={
            data.riskScore !== null
              ? `Wellness Risk Score: ${data.riskScore}/100`
              : 'Run AI Risk Detector'
          }
          icon={ShieldAlert}
          tone="warning"
          badge={<span className={`badge ${riskBadgeClass}`}>{data.riskLevel.replace('_', ' ')}</span>}
        />
      </div>

      {/* Daily Gaming Budget Progress */}
      <div className="card mb-3">
        <div className="card-header">
          <div>
            <h2 className="card-title">
              <Activity size={18} color="var(--accent)" />
              Daily Gaming Allowance Progress
            </h2>
            <p className="card-subtitle">
              Keeping daily gaming within your limit protects sleep and academic focus.
            </p>
          </div>
          <Link to="/gaming-tracker" className="btn btn-secondary btn-sm">
            Open Tracker
          </Link>
        </div>
        <ProgressBar
          value={data.todayGamingMinutes}
          max={data.dailyGamingLimitMinutes}
          color={data.gamingLimitExceeded ? 'var(--danger)' : 'var(--accent)'}
          label={`${formatMinutes(data.todayGamingMinutes)} used of ${formatMinutes(data.dailyGamingLimitMinutes)}`}
          rightText={
            data.gamingLimitExceeded
              ? 'Limit Exceeded'
              : `${formatMinutes(data.remainingGamingMinutes)} remaining`
          }
        />
      </div>

      {/* Weekly Charts */}
      <div className="grid grid-cols-2 mb-3">
        <div className="card">
          <div className="card-header">
            <div>
              <h3 className="card-title">Weekly Gaming Activity (Minutes)</h3>
              <p className="card-subtitle">Last 7 days compared against your daily limit</p>
            </div>
          </div>
          <BarChart
            data={data.weeklyGamingChart}
            unit="m"
            barColor="var(--accent)"
            targetLine={data.dailyGamingLimitMinutes}
          />
        </div>

        <div className="card">
          <div className="card-header">
            <div>
              <h3 className="card-title">Weekly Study Activity (Hours)</h3>
              <p className="card-subtitle">Completed study hours over the last 7 days</p>
            </div>
          </div>
          <BarChart
            data={data.weeklyStudyChart}
            unit="h"
            barColor="var(--success)"
          />
        </div>
      </div>

      {/* Bottom Details */}
      <div className="grid grid-cols-3">
        <div className="card">
          <div className="card-header">
            <h3 className="card-title">Recent Gaming Sessions</h3>
            <Link to="/gaming-tracker" className="text-xs font-semibold">View all</Link>
          </div>
          {data.recentGamingSessions.length === 0 ? (
            <div className="empty-state text-sm">
              No gaming sessions tracked yet. Start a session in the Gaming Tracker.
            </div>
          ) : (
            <div className="flex-col gap-2">
              {data.recentGamingSessions.map((s) => (
                <div
                  key={s.id}
                  className="flex items-center justify-between"
                  style={{ padding: '0.6rem 0.75rem', borderRadius: 10, background: 'var(--bg-surface-alt)' }}
                >
                  <div>
                    <div className="font-semibold text-sm">{s.gameName}</div>
                    <div className="text-xs text-muted">{s.sessionDate}</div>
                  </div>
                  <span className={`badge ${s.active ? 'badge-warning' : 'badge-accent'}`}>
                    {s.active ? 'Active' : formatMinutes(s.totalDurationMinutes)}
                  </span>
                </div>
              ))}
            </div>
          )}
        </div>

        <div className="card">
          <div className="card-header">
            <h3 className="card-title">Upcoming Study Tasks</h3>
            <Link to="/study-planner" className="text-xs font-semibold">Manage</Link>
          </div>
          {data.upcomingStudyTasks.length === 0 ? (
            <div className="empty-state text-sm">
              All caught up! Add upcoming assignments in the Study Planner.
            </div>
          ) : (
            <div className="flex-col gap-2">
              {data.upcomingStudyTasks.map((t) => (
                <div
                  key={t.id}
                  className="flex items-center justify-between"
                  style={{ padding: '0.6rem 0.75rem', borderRadius: 10, background: 'var(--bg-surface-alt)' }}
                >
                  <div>
                    <div className="font-semibold text-sm">{t.title}</div>
                    <div className="text-xs text-muted">
                      {t.subjectName} · {new Date(t.dueDateTime).toLocaleString()}
                    </div>
                  </div>
                  <span
                    className={`badge ${
                      t.priority === 'HIGH'
                        ? 'badge-danger'
                        : t.priority === 'MEDIUM'
                        ? 'badge-warning'
                        : 'badge-info'
                    }`}
                  >
                    {t.priority}
                  </span>
                </div>
              ))}
            </div>
          )}
        </div>

        <div className="card">
          <div className="card-header">
            <h3 className="card-title">
              <MoonStar size={17} color="var(--info)" />
              Sleep Summary
            </h3>
            <Link to="/sleep-tracker" className="text-xs font-semibold">Log Sleep</Link>
          </div>
          <p className="text-sm text-muted mb-2">{data.sleepSummaryText}</p>
          {data.latestSleepRecord ? (
            <div
              style={{ padding: '0.85rem', borderRadius: 12, background: 'var(--bg-surface-alt)' }}
              className="flex-col gap-1"
            >
              <div className="flex items-center justify-between text-sm">
                <span className="text-muted">Latest Duration</span>
                <span className="font-semibold">{data.latestSleepRecord.totalDurationHours} hrs</span>
              </div>
              <div className="flex items-center justify-between text-sm">
                <span className="text-muted">7-Night Avg</span>
                <span className="font-semibold">{data.avgSleepHours} hrs</span>
              </div>
              <div className="flex items-center justify-between text-sm">
                <span className="text-muted">Late-Night Gaming</span>
                <span className={`badge ${data.latestSleepRecord.hadLateNightGaming ? 'badge-warning' : 'badge-success'}`}>
                  {data.latestSleepRecord.hadLateNightGaming ? 'Yes' : 'No'}
                </span>
              </div>
            </div>
          ) : (
            <div className="empty-state text-sm">No sleep records yet.</div>
          )}
        </div>
      </div>
    </div>
  );
}
