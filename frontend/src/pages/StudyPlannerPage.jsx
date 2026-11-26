import { useCallback, useEffect, useState } from 'react';
import { useOutletContext } from 'react-router-dom';
import {
  Bell,
  BellRing,
  BookOpenCheck,
  CheckCircle2,
  Circle,
  Edit3,
  Plus,
  Trash2,
} from 'lucide-react';
import { extractErrorMessage, studyApi } from '../services/api';
import StatCard from '../components/StatCard';
import { BarChart } from '../components/Charts';
import Modal from '../components/Modal';
import LoadingSpinner from '../components/LoadingSpinner';
import ProgressBar from '../components/ProgressBar';

const EMPTY_TASK = {
  subjectName: '',
  title: '',
  description: '',
  dueDateTime: '',
  estimatedHours: 1.5,
  priority: 'MEDIUM',
};

export default function StudyPlannerPage() {
  const { reminders, permission, requestPermission, refreshReminders } = useOutletContext() || {};
  const [summary, setSummary] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [filter, setFilter] = useState('ALL');
  const [modalOpen, setModalOpen] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [form, setForm] = useState(EMPTY_TASK);
  const [saving, setSaving] = useState(false);

  const loadStudy = useCallback(async () => {
    try {
      const data = await studyApi.getSummary();
      setSummary(data);
      setError('');
      if (refreshReminders) refreshReminders();
    } catch (err) {
      setError(extractErrorMessage(err, 'Unable to load study tasks.'));
    } finally {
      setLoading(false);
    }
  }, [refreshReminders]);

  useEffect(() => {
    loadStudy();
  }, [loadStudy]);

  const openCreateModal = () => {
    setEditingId(null);
    const defaultDue = new Date(Date.now() + 2 * 3600 * 1000);
    const localIso = new Date(defaultDue.getTime() - defaultDue.getTimezoneOffset() * 60000)
      .toISOString()
      .slice(0, 16);
    setForm({ ...EMPTY_TASK, dueDateTime: localIso });
    setModalOpen(true);
  };

  const openEditModal = (task) => {
    setEditingId(task.id);
    setForm({
      subjectName: task.subjectName,
      title: task.title,
      description: task.description || '',
      dueDateTime: task.dueDateTime ? task.dueDateTime.slice(0, 16) : '',
      estimatedHours: task.estimatedHours || 1,
      priority: task.priority || 'MEDIUM',
    });
    setModalOpen(true);
  };

  const handleSave = async (e) => {
    e.preventDefault();
    setSaving(true);
    setError('');
    try {
      const payload = {
        ...form,
        estimatedHours: Number(form.estimatedHours),
      };
      if (editingId) {
        await studyApi.updateTask(editingId, payload);
      } else {
        await studyApi.createTask(payload);
      }
      setModalOpen(false);
      await loadStudy();
    } catch (err) {
      setError(extractErrorMessage(err, 'Failed to save study task.'));
    } finally {
      setSaving(false);
    }
  };

  const handleToggle = async (id) => {
    try {
      await studyApi.toggleTask(id);
      await loadStudy();
    } catch (err) {
      setError(extractErrorMessage(err, 'Failed to update task status.'));
    }
  };

  const handleDelete = async (id) => {
    try {
      await studyApi.deleteTask(id);
      await loadStudy();
    } catch (err) {
      setError(extractErrorMessage(err, 'Failed to delete task.'));
    }
  };

  const triggerTestNotification = async () => {
    let perm = permission;
    if (perm === 'default' && requestPermission) {
      perm = await requestPermission();
    }
    if (perm === 'granted' && 'Notification' in window) {
      new Notification('GameBalance AI Study Reminder', {
        body: 'Browser study reminders are active! You will be alerted ahead of upcoming deadlines.',
      });
    }
  };

  if (loading) return <LoadingSpinner text="Loading study planner..." />;

  const tasks = summary?.tasks || [];
  const filteredTasks = tasks.filter((t) => {
    if (filter === 'PENDING') return !t.completed;
    if (filter === 'COMPLETED') return t.completed;
    return true;
  });

  const totalTasks = tasks.length;
  const completedCount = summary?.completedCount || 0;

  return (
    <div className="flex-col gap-3">
      {error && <div className="alert alert-danger mb-3">{error}</div>}

      {/* Reminders Banner */}
      <div className="card mb-3">
        <div className="card-header" style={{ marginBottom: reminders?.length ? '0.75rem' : 0 }}>
          <div className="flex items-center gap-2">
            <BellRing size={19} color="var(--warning)" />
            <div>
              <h2 className="card-title">Study Reminders & Browser Alerts</h2>
              <p className="card-subtitle">
                Notification status:{' '}
                <strong>
                  {permission === 'granted'
                    ? 'Enabled'
                    : permission === 'denied'
                    ? 'Blocked in browser settings'
                    : permission === 'unsupported'
                    ? 'Not supported in this browser'
                    : 'Not enabled yet'}
                </strong>
              </p>
            </div>
          </div>
          <div className="flex items-center gap-2">
            {permission !== 'granted' && permission !== 'unsupported' && (
              <button type="button" className="btn btn-secondary btn-sm" onClick={requestPermission}>
                <Bell size={15} />
                <span>Enable Browser Alerts</span>
              </button>
            )}
            {permission === 'granted' && (
              <button type="button" className="btn btn-secondary btn-sm" onClick={triggerTestNotification}>
                <Bell size={15} />
                <span>Test Alert</span>
              </button>
            )}
            <button type="button" className="btn btn-primary btn-sm" onClick={openCreateModal}>
              <Plus size={15} />
              <span>Add Study Task</span>
            </button>
          </div>
        </div>

        {(summary?.upcomingReminders?.length || 0) > 0 && (
          <div className="flex-col gap-2">
            {summary.upcomingReminders.map((task) => (
              <div key={task.id} className="alert alert-warning">
                <BellRing size={17} color="var(--warning)" style={{ flexShrink: 0, marginTop: 2 }} />
                <div className="text-sm">
                  <strong>{task.title}</strong> ({task.subjectName}) is due at{' '}
                  {new Date(task.dueDateTime).toLocaleString()}
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Stats */}
      <div className="grid grid-cols-4 mb-3">
        <StatCard label="Pending Tasks" value={summary?.pendingCount ?? 0} hint="To complete" icon={BookOpenCheck} tone="warning" />
        <StatCard label="Completed Tasks" value={completedCount} hint="Great progress" icon={CheckCircle2} tone="success" />
        <StatCard label="Today's Study" value={`${summary?.todayStudyHours ?? 0}h`} hint="Completed today" icon={BookOpenCheck} tone="accent" />
        <StatCard label="Weekly Study" value={`${summary?.weeklyStudyHours ?? 0}h`} hint="Last 7 days" icon={BookOpenCheck} tone="info" />
      </div>

      {/* Progress & Chart */}
      <div className="grid grid-cols-2 mb-3">
        <div className="card">
          <div className="card-header">
            <h3 className="card-title">Task Completion Progress</h3>
          </div>
          <ProgressBar
            value={completedCount}
            max={Math.max(1, totalTasks)}
            color="var(--success)"
            label={`${completedCount} of ${totalTasks} tasks completed`}
          />
          <div className="mt-3">
            <h4 className="font-semibold text-sm mb-2">Upcoming Deadlines</h4>
            {!summary?.upcomingDeadlines?.length ? (
              <div className="text-sm text-muted">No pending deadlines. Add a task to get started!</div>
            ) : (
              <div className="flex-col gap-2">
                {summary.upcomingDeadlines.map((t) => (
                  <div
                    key={t.id}
                    className="flex items-center justify-between"
                    style={{ padding: '0.55rem 0.75rem', borderRadius: 10, background: 'var(--bg-surface-alt)' }}
                  >
                    <div>
                      <div className="text-sm font-semibold">{t.title}</div>
                      <div className="text-xs text-muted">{t.subjectName} · {new Date(t.dueDateTime).toLocaleString()}</div>
                    </div>
                    <span className={`badge ${t.priority === 'HIGH' ? 'badge-danger' : t.priority === 'MEDIUM' ? 'badge-warning' : 'badge-info'}`}>
                      {t.priority}
                    </span>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>

        <div className="card">
          <div className="card-header">
            <div>
              <h3 className="card-title">Weekly Study Hours</h3>
              <p className="card-subtitle">Hours earned from completed study tasks</p>
            </div>
          </div>
          <BarChart data={summary?.weeklyChart || []} unit="h" barColor="var(--success)" />
        </div>
      </div>

      {/* Task List */}
      <div className="card">
        <div className="card-header">
          <h2 className="card-title">All Study Tasks</h2>
          <div className="flex items-center gap-2">
            {['ALL', 'PENDING', 'COMPLETED'].map((tab) => (
              <button
                key={tab}
                type="button"
                className={`btn btn-sm ${filter === tab ? 'btn-primary' : 'btn-secondary'}`}
                onClick={() => setFilter(tab)}
              >
                {tab.charAt(0) + tab.slice(1).toLowerCase()}
              </button>
            ))}
          </div>
        </div>

        {filteredTasks.length === 0 ? (
          <div className="empty-state text-sm">
            No study tasks in this view. Click "Add Study Task" to organize your coursework.
          </div>
        ) : (
          <div className="flex-col gap-2">
            {filteredTasks.map((task) => (
              <div
                key={task.id}
                className="flex items-center justify-between gap-2"
                style={{
                  padding: '0.85rem 1rem',
                  borderRadius: 12,
                  background: 'var(--bg-surface-alt)',
                  border: '1px solid var(--border-color)',
                  opacity: task.completed ? 0.75 : 1,
                }}
              >
                <div className="flex items-center gap-3" style={{ flex: 1 }}>
                  <button
                    type="button"
                    onClick={() => handleToggle(task.id)}
                    style={{ background: 'none', border: 'none', cursor: 'pointer', color: task.completed ? 'var(--success)' : 'var(--text-muted)' }}
                    title={task.completed ? 'Mark as pending' : 'Mark as completed'}
                  >
                    {task.completed ? <CheckCircle2 size={22} /> : <Circle size={22} />}
                  </button>
                  <div>
                    <div
                      className="font-semibold text-sm"
                      style={{ textDecoration: task.completed ? 'line-through' : 'none' }}
                    >
                      {task.title}
                    </div>
                    <div className="text-xs text-muted">
                      {task.subjectName} · {task.estimatedHours}h · Due {new Date(task.dueDateTime).toLocaleString()}
                    </div>
                    {task.description && <div className="text-xs text-muted mt-1">{task.description}</div>}
                  </div>
                </div>

                <div className="flex items-center gap-2">
                  <span
                    className={`badge ${
                      task.priority === 'HIGH'
                        ? 'badge-danger'
                        : task.priority === 'MEDIUM'
                        ? 'badge-warning'
                        : 'badge-info'
                    }`}
                  >
                    {task.priority}
                  </span>
                  <button type="button" className="icon-btn" onClick={() => openEditModal(task)} title="Edit task">
                    <Edit3 size={15} />
                  </button>
                  <button type="button" className="icon-btn" onClick={() => handleDelete(task.id)} title="Delete task">
                    <Trash2 size={15} color="var(--danger)" />
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Add / Edit Modal */}
      <Modal
        open={modalOpen}
        title={editingId ? 'Edit Study Task' : 'New Study Task'}
        onClose={() => setModalOpen(false)}
      >
        <form onSubmit={handleSave}>
          <div className="form-group">
            <label className="form-label">Subject Name *</label>
            <input
              type="text"
              className="form-input"
              placeholder="e.g. Data Structures & Algorithms"
              value={form.subjectName}
              onChange={(e) => setForm({ ...form, subjectName: e.target.value })}
              required
            />
          </div>

          <div className="form-group">
            <label className="form-label">Task Title *</label>
            <input
              type="text"
              className="form-input"
              placeholder="e.g. Revise Graph Traversal Lab"
              value={form.title}
              onChange={(e) => setForm({ ...form, title: e.target.value })}
              required
            />
          </div>

          <div className="form-group">
            <label className="form-label">Description</label>
            <textarea
              className="form-textarea"
              placeholder="Chapters, exercises, or submission checklist..."
              value={form.description}
              onChange={(e) => setForm({ ...form, description: e.target.value })}
            />
          </div>

          <div className="grid grid-cols-3" style={{ gap: '0.75rem' }}>
            <div className="form-group" style={{ gridColumn: 'span 2' }}>
              <label className="form-label">Due Date & Time *</label>
              <input
                type="datetime-local"
                className="form-input"
                value={form.dueDateTime}
                onChange={(e) => setForm({ ...form, dueDateTime: e.target.value })}
                required
              />
            </div>
            <div className="form-group">
              <label className="form-label">Est. Hours *</label>
              <input
                type="number"
                step="0.25"
                min="0.25"
                max="24"
                className="form-input"
                value={form.estimatedHours}
                onChange={(e) => setForm({ ...form, estimatedHours: e.target.value })}
                required
              />
            </div>
          </div>

          <div className="form-group">
            <label className="form-label">Priority</label>
            <select
              className="form-select"
              value={form.priority}
              onChange={(e) => setForm({ ...form, priority: e.target.value })}
            >
              <option value="LOW">Low</option>
              <option value="MEDIUM">Medium</option>
              <option value="HIGH">High</option>
            </select>
          </div>

          <div className="flex items-center justify-between gap-2 mt-2">
            <button type="button" className="btn btn-secondary" onClick={() => setModalOpen(false)}>
              Cancel
            </button>
            <button type="submit" className="btn btn-primary" disabled={saving}>
              {saving ? 'Saving...' : editingId ? 'Save Changes' : 'Create Task'}
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
}
