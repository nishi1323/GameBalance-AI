import { Bell, BellRing, Menu } from 'lucide-react';
import { Link } from 'react-router-dom';
import ThemeToggle from './ThemeToggle';

export default function Navbar({
  title,
  user,
  onOpenSidebar,
  remindersCount = 0,
  permission,
  onRequestNotifications,
}) {
  return (
    <header className="navbar">
      <div className="navbar-left">
        <button
          type="button"
          className="icon-btn mobile-only"
          onClick={onOpenSidebar}
          aria-label="Open navigation menu"
        >
          <Menu size={19} />
        </button>
        <div>
          <h1 style={{ fontSize: '1.12rem', fontWeight: 700 }}>{title}</h1>
        </div>
      </div>

      <div className="navbar-right">
        {permission === 'default' && (
          <button
            type="button"
            className="btn btn-secondary btn-sm"
            onClick={onRequestNotifications}
            title="Enable browser notifications for study reminders"
          >
            <Bell size={15} />
            <span>Enable Alerts</span>
          </button>
        )}

        <Link
          to="/study-planner"
          className="icon-btn"
          title={`${remindersCount} upcoming study reminder(s)`}
          style={{ position: 'relative' }}
        >
          {remindersCount > 0 ? <BellRing size={18} color="var(--warning)" /> : <Bell size={18} />}
          {remindersCount > 0 && (
            <span
              style={{
                position: 'absolute',
                top: -4,
                right: -4,
                background: 'var(--danger)',
                color: '#fff',
                fontSize: '0.68rem',
                fontWeight: 700,
                borderRadius: '999px',
                minWidth: '18px',
                height: '18px',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                padding: '0 4px',
              }}
            >
              {remindersCount}
            </span>
          )}
        </Link>

        <ThemeToggle />

        <Link
          to="/profile"
          className="flex items-center gap-2"
          style={{
            padding: '0.35rem 0.7rem',
            borderRadius: '10px',
            border: '1px solid var(--border-color)',
            background: 'var(--bg-surface-alt)',
            color: 'var(--text-primary)',
          }}
        >
          <div
            style={{
              width: 28,
              height: 28,
              borderRadius: '8px',
              background: 'var(--accent)',
              color: '#fff',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              fontWeight: 700,
              fontSize: '0.82rem',
            }}
          >
            {user?.fullName?.charAt(0)?.toUpperCase() || 'S'}
          </div>
          <div style={{ lineHeight: 1.15 }}>
            <div className="text-xs font-semibold">{user?.fullName || 'Student'}</div>
            <div className="text-xs text-muted">{user?.collegeName || 'Student Account'}</div>
          </div>
        </Link>
      </div>
    </header>
  );
}
