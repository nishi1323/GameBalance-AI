import { useCallback, useEffect, useState } from 'react';
import { AlertTriangle, Clock, Gamepad2, Play, Square, Trash2 } from 'lucide-react';
import { extractErrorMessage, gamingApi } from '../services/api';
import StatCard from '../components/StatCard';
import { BarChart } from '../components/Charts';
import LoadingSpinner from '../components/LoadingSpinner';
import ProgressBar from '../components/ProgressBar';

const POPULAR_GAMES = [
  'Valorant',
  'League of Legends',
  'CS2',
  'Minecraft',
  'EA FC / FIFA',
  'Call of Duty',
  'Genshin Impact',
  'Dota 2',
  'Rocket League',
  'Custom Game',
];

const formatMinutes = (mins = 0) => {
  const h = Math.floor(mins / 60);
  const m = mins % 60;
  return h > 0 ? `${h}h ${m}m` : `${m}m`;
};

const formatStopwatch = (seconds = 0) => {
  const hrs = Math.floor(seconds / 3600);
  const mins = Math.floor((seconds % 3600) / 60);
  const secs = seconds % 60;
  return [hrs, mins, secs].map((n) => String(n).padStart(2, '0')).join(':');
};

export default function GamingTrackerPage() {
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [selectedGame, setSelectedGame] = useState('Valorant');
  const [customGame, setCustomGame] = useState('');
  const [notes, setNotes] = useState('');
  const [busy, setBusy] = useState(false);
  const [elapsedSeconds, setElapsedSeconds] = useState(0);

  const loadStats = useCallback(async () => {
    try {
      const data = await gamingApi.getStats();
      setStats(data);
      setError('');
    } catch (err) {
      setError(extractErrorMessage(err, 'Unable to load gaming activity.'));
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadStats();
  }, [loadStats]);

  // Live timer persisted via backend activeSession.startTime across refreshes
  useEffect(() => {
    const active = stats?.activeSession;
    if (!active?.startTime) {
      setElapsedSeconds(0);
      return;
    }
    const updateTimer = () => {
      const diff = Math.max(0, Math.floor((Date.now() - new Date(active.startTime).getTime()) / 1000));
      setElapsedSeconds(diff);
    };
    updateTimer();
    const timer = setInterval(updateTimer, 1000);
    return () => clearInterval(timer);
  }, [stats?.activeSession]);

  const handleStart = async (e) => {
    e.preventDefault();
    setError('');
    const gameName = selectedGame === 'Custom Game' ? customGame.trim() : selectedGame;
    if (!gameName) {
      setError('Please select or enter a game name.');
      return;
    }

    setBusy(true);
    try {
      await gamingApi.startSession({ gameName, notes });
      setNotes('');
      setCustomGame('');
      await loadStats();
    } catch (err) {
      setError(extractErrorMessage(err, 'Could not start gaming session.'));
    } finally {
      setBusy(false);
    }
  };

  const handleStop = async () => {
    if (!stats?.activeSession) return;
    setBusy(true);
    setError('');
    try {
      await gamingApi.stopSession(stats.activeSession.id, { notes });
      setNotes('');
      await loadStats();
    } catch (err) {
      setError(extractErrorMessage(err, 'Could not stop gaming session.'));
    } finally {
      setBusy(false);
    }
  };

  const handleDelete = async (id) => {
    try {
      await gamingApi.deleteSession(id);
      await loadStats();
    } catch (err) {
      setError(extractErrorMessage(err, 'Failed to delete session.'));
    }
  };

  if (loading) return <LoadingSpinner text="Loading gaming tracker..." />;

  const active = stats?.activeSession;

  return (
    <div className="flex-col gap-3">
      {error && <div className="alert alert-danger mb-3">{error}</div>}

      {stats?.exceededDailyLimit && (
        <div className="alert alert-warning mb-3">
          <AlertTriangle size={20} color="var(--warning)" style={{ flexShrink: 0 }} />
          <div>
            <strong>Daily Limit Exceeded:</strong> You have played{' '}
            {formatMinutes(stats.todayMinutes)} today, exceeding your{' '}
            {formatMinutes(stats.dailyLimitMinutes)} daily target. Consider wrapping up and taking a recovery break!
          </div>
        </div>
      )}

      {/* Summary Cards */}
      <div className="grid grid-cols-4 mb-3">
        <StatCard
          label="Today's Gaming"
          value={formatMinutes(stats?.todayMinutes)}
          hint={`Limit: ${formatMinutes(stats?.dailyLimitMinutes)}`}
          icon={Gamepad2}
          tone={stats?.exceededDailyLimit ? 'danger' : 'accent'}
        />
        <StatCard
          label="Weekly Gaming"
          value={formatMinutes(stats?.weeklyMinutes)}
          hint="Last 7 days"
          icon={Clock}
          tone="info"
        />
        <StatCard
          label="Monthly Gaming"
          value={formatMinutes(stats?.monthlyMinutes)}
          hint="Current month"
          icon={Clock}
          tone="warning"
        />
        <StatCard
          label="All-Time Total"
          value={`${((stats?.totalMinutes || 0) / 60).toFixed(1)} hrs`}
          hint={`${stats?.history?.length || 0} recorded session(s)`}
          icon={Gamepad2}
          tone="success"
        />
      </div>

      {/* Active Session Control + Chart */}
      <div className="grid grid-cols-2 mb-3">
        <div className="card">
          <div className="card-header">
            <div>
              <h2 className="card-title">
                <Gamepad2 size={18} color="var(--accent)" />
                {active ? 'Active Gaming Session' : 'Start Gaming Session'}
              </h2>
              <p className="card-subtitle">
                {active
                  ? 'Your active session persists automatically across page refreshes.'
                  : 'Select a game and track your session duration in real time.'}
              </p>
            </div>
            {active && <span className="badge badge-warning">LIVE</span>}
          </div>

          {active ? (
            <div className="flex-col gap-3">
              <div
                style={{
                  padding: '1.25rem',
                  borderRadius: 14,
                  background: 'var(--bg-surface-alt)',
                  border: '1px solid var(--border-color)',
                  textAlign: 'center',
                }}
              >
                <div className="text-sm text-muted">Playing Now</div>
                <div style={{ fontSize: '1.2rem', fontWeight: 700, margin: '0.25rem 0' }}>{active.gameName}</div>
                <div
                  style={{
                    fontSize: '2.4rem',
                    fontWeight: 800,
                    fontFamily: 'monospace',
                    color: 'var(--accent)',
                    margin: '0.35rem 0',
                  }}
                >
                  {formatStopwatch(elapsedSeconds)}
                </div>
                <div className="text-xs text-muted">
                  Started at {new Date(active.startTime).toLocaleTimeString()}
                </div>
              </div>

              <div className="form-group">
                <label className="form-label">Session Notes (Optional)</label>
                <input
                  type="text"
                  className="form-input"
                  placeholder="e.g. Played 2 ranked matches with friends"
                  value={notes}
                  onChange={(e) => setNotes(e.target.value)}
                />
              </div>

              <button type="button" className="btn btn-danger" onClick={handleStop} disabled={busy}>
                <Square size={16} />
                <span>{busy ? 'Stopping...' : 'Stop & Save Session'}</span>
              </button>
            </div>
          ) : (
            <form onSubmit={handleStart}>
              <div className="form-group">
                <label className="form-label" htmlFor="game-select">Game Name</label>
                <select
                  id="game-select"
                  className="form-select"
                  value={selectedGame}
                  onChange={(e) => setSelectedGame(e.target.value)}
                >
                  {POPULAR_GAMES.map((g) => (
                    <option key={g} value={g}>{g}</option>
                  ))}
                </select>
              </div>

              {selectedGame === 'Custom Game' && (
                <div className="form-group">
                  <label className="form-label" htmlFor="custom-game">Enter Custom Game Name</label>
                  <input
                    id="custom-game"
                    type="text"
                    className="form-input"
                    placeholder="e.g. Elden Ring"
                    value={customGame}
                    onChange={(e) => setCustomGame(e.target.value)}
                    required
                  />
                </div>
              )}

              <div className="form-group">
                <label className="form-label" htmlFor="session-notes">Notes (Optional)</label>
                <input
                  id="session-notes"
                  type="text"
                  className="form-input"
                  placeholder="Quick unwind session after study"
                  value={notes}
                  onChange={(e) => setNotes(e.target.value)}
                />
              </div>

              <button type="submit" className="btn btn-primary" style={{ width: '100%' }} disabled={busy}>
                <Play size={16} />
                <span>{busy ? 'Starting...' : 'Start Gaming Session'}</span>
              </button>
            </form>
          )}

          <div className="mt-3">
            <ProgressBar
              value={stats?.todayMinutes || 0}
              max={stats?.dailyLimitMinutes || 120}
              color={stats?.exceededDailyLimit ? 'var(--danger)' : 'var(--accent)'}
              label="Today's Limit Usage"
              rightText={`${stats?.todayMinutes || 0} / ${stats?.dailyLimitMinutes || 120} mins`}
            />
          </div>
        </div>

        <div className="card">
          <div className="card-header">
            <div>
              <h2 className="card-title">7-Day Gaming Activity</h2>
              <p className="card-subtitle">Daily minutes tracked vs. your daily limit line</p>
            </div>
          </div>
          <BarChart
            data={stats?.dailyChart || []}
            unit="m"
            barColor="var(--accent)"
            targetLine={stats?.dailyLimitMinutes || 120}
            height={250}
          />
        </div>
      </div>

      {/* History Table */}
      <div className="card">
        <div className="card-header">
          <div>
            <h2 className="card-title">Gaming Session History</h2>
            <p className="card-subtitle">All sessions persisted in MongoDB</p>
          </div>
        </div>

        {!stats?.history?.length ? (
          <div className="empty-state text-sm">
            No gaming sessions recorded yet. Start your first session above!
          </div>
        ) : (
          <div className="table-wrap">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Game</th>
                  <th>Date</th>
                  <th>Start Time</th>
                  <th>End Time</th>
                  <th>Duration</th>
                  <th>Flags</th>
                  <th>Action</th>
                </tr>
              </thead>
              <tbody>
                {stats.history.map((session) => (
                  <tr key={session.id}>
                    <td className="font-semibold" style={{ color: 'var(--text-primary)' }}>
                      {session.gameName}
                      {session.notes && (
                        <div className="text-xs text-muted" style={{ fontWeight: 400 }}>{session.notes}</div>
                      )}
                    </td>
                    <td>{session.sessionDate}</td>
                    <td>{new Date(session.startTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}</td>
                    <td>
                      {session.endTime
                        ? new Date(session.endTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
                        : 'In progress'}
                    </td>
                    <td>
                      {session.active ? (
                        <span className="badge badge-warning">Running</span>
                      ) : (
                        formatMinutes(session.totalDurationMinutes)
                      )}
                    </td>
                    <td>
                      {session.lateNight ? (
                        <span className="badge badge-warning">Late Night</span>
                      ) : (
                        <span className="badge badge-neutral">Daytime</span>
                      )}
                    </td>
                    <td>
                      <button
                        type="button"
                        className="icon-btn"
                        title="Delete session"
                        onClick={() => handleDelete(session.id)}
                      >
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
