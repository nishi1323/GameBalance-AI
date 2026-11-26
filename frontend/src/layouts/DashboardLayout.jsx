import { useState } from 'react';
import { Outlet, useLocation, useNavigate } from 'react-router-dom';
import Sidebar from '../components/Sidebar';
import Navbar from '../components/Navbar';
import { useAuth } from '../context/AuthContext';
import { useStudyReminders } from '../hooks/useStudyReminders';

const PAGE_TITLES = {
  '/dashboard': 'Wellness Dashboard',
  '/gaming-tracker': 'Gaming Activity Tracker',
  '/risk-detector': 'AI Gaming Risk Detector',
  '/study-planner': 'Study Reminder & Planner',
  '/gaming-planner': 'Smart Gaming Planner',
  '/sleep-tracker': 'Sleep & Health Tracker',
  '/assistant': 'AI Wellness Assistant',
  '/profile': 'Student Profile & Preferences',
};

export default function DashboardLayout() {
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const { user, logout, isAuthenticated } = useAuth();
  const location = useLocation();
  const navigate = useNavigate();
  const { reminders, permission, requestPermission, refreshReminders } = useStudyReminders(isAuthenticated);

  const title = PAGE_TITLES[location.pathname] || 'GameBalance AI';

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <div className="app-shell">
      <Sidebar open={sidebarOpen} onClose={() => setSidebarOpen(false)} onLogout={handleLogout} />
      <div className="main-area">
        <Navbar
          title={title}
          user={user}
          onOpenSidebar={() => setSidebarOpen(true)}
          remindersCount={reminders.length}
          permission={permission}
          onRequestNotifications={requestPermission}
        />
        <main className="page-content">
          <Outlet context={{ reminders, permission, requestPermission, refreshReminders }} />
        </main>
      </div>
    </div>
  );
}
