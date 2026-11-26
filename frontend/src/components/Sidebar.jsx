import { NavLink } from 'react-router-dom';
import {
  LayoutDashboard,
  Gamepad2,
  ShieldAlert,
  BookOpenCheck,
  CalendarClock,
  MoonStar,
  Bot,
  UserCircle,
  LogOut,
  X,
  Scale,
} from 'lucide-react';

const NAV_ITEMS = [
  { to: '/dashboard', label: 'Dashboard', icon: LayoutDashboard },
  { to: '/gaming-tracker', label: 'Gaming Tracker', icon: Gamepad2 },
  { to: '/risk-detector', label: 'AI Risk Detector', icon: ShieldAlert },
  { to: '/study-planner', label: 'Study Planner', icon: BookOpenCheck },
  { to: '/gaming-planner', label: 'Gaming Planner', icon: CalendarClock },
  { to: '/sleep-tracker', label: 'Sleep Tracker', icon: MoonStar },
  { to: '/assistant', label: 'AI Wellness Assistant', icon: Bot },
  { to: '/profile', label: 'Profile', icon: UserCircle },
];

export default function Sidebar({ open, onClose, onLogout }) {
  return (
    <>
      <div className={`sidebar-overlay ${open ? 'open' : ''}`} onClick={onClose} />
      <aside className={`sidebar ${open ? 'open' : ''}`}>
        <div className="sidebar-header">
          <div className="brand">
            <div className="brand-icon">
              <Scale size={20} />
            </div>
            <div>
              <div className="brand-title">GameBalance AI</div>
              <div className="brand-subtitle">Student Wellness Suite</div>
            </div>
          </div>
          <button
            type="button"
            className="icon-btn mobile-only"
            onClick={onClose}
            aria-label="Close navigation menu"
          >
            <X size={18} />
          </button>
        </div>

        <nav className="sidebar-nav">
          {NAV_ITEMS.map(({ to, label, icon: Icon }) => (
            <NavLink
              key={to}
              to={to}
              onClick={onClose}
              className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}
            >
              <Icon size={18} />
              <span>{label}</span>
            </NavLink>
          ))}
        </nav>

        <div className="sidebar-footer">
          <button type="button" className="nav-item" onClick={onLogout} style={{ color: 'var(--danger)' }}>
            <LogOut size={18} />
            <span>Logout</span>
          </button>
        </div>
      </aside>
    </>
  );
}
