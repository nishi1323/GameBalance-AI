import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import { ThemeProvider } from './context/ThemeContext';
import ProtectedRoute from './components/ProtectedRoute';
import DashboardLayout from './layouts/DashboardLayout';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import DashboardPage from './pages/DashboardPage';
import GamingTrackerPage from './pages/GamingTrackerPage';
import RiskDetectorPage from './pages/RiskDetectorPage';
import StudyPlannerPage from './pages/StudyPlannerPage';
import GamingPlannerPage from './pages/GamingPlannerPage';
import SleepTrackerPage from './pages/SleepTrackerPage';
import WellnessAssistantPage from './pages/WellnessAssistantPage';
import ProfilePage from './pages/ProfilePage';

export default function App() {
  return (
    <ThemeProvider>
      <AuthProvider>
        <BrowserRouter>
          <Routes>
            <Route path="/login" element={<LoginPage />} />
            <Route path="/register" element={<RegisterPage />} />

            <Route element={<ProtectedRoute />}>
              <Route element={<DashboardLayout />}>
                <Route path="/dashboard" element={<DashboardPage />} />
                <Route path="/gaming-tracker" element={<GamingTrackerPage />} />
                <Route path="/risk-detector" element={<RiskDetectorPage />} />
                <Route path="/study-planner" element={<StudyPlannerPage />} />
                <Route path="/gaming-planner" element={<GamingPlannerPage />} />
                <Route path="/sleep-tracker" element={<SleepTrackerPage />} />
                <Route path="/assistant" element={<WellnessAssistantPage />} />
                <Route path="/profile" element={<ProfilePage />} />
              </Route>
            </Route>

            <Route path="*" element={<Navigate to="/dashboard" replace />} />
          </Routes>
        </BrowserRouter>
      </AuthProvider>
    </ThemeProvider>
  );
}
