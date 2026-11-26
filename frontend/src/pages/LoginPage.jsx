import { useState } from 'react';
import { Link, Navigate, useNavigate } from 'react-router-dom';
import { LogIn, Scale } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { extractErrorMessage } from '../services/api';
import ThemeToggle from '../components/ThemeToggle';

export default function LoginPage() {
  const { login, isAuthenticated } = useAuth();
  const navigate = useNavigate();

  const [form, setForm] = useState({ email: '', password: '' });
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  if (isAuthenticated) {
    return <Navigate to="/dashboard" replace />;
  }

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    if (!form.email.trim() || !form.password) {
      setError('Please enter both your email and password.');
      return;
    }

    setSubmitting(true);
    try {
      await login({ email: form.email.trim(), password: form.password });
      navigate('/dashboard');
    } catch (err) {
      setError(extractErrorMessage(err, 'Unable to log in. Please check your credentials.'));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="auth-container">
      <div style={{ position: 'fixed', top: '1rem', right: '1rem' }}>
        <ThemeToggle />
      </div>
      <div className="auth-card">
        <div className="flex items-center gap-2 mb-3">
          <div className="brand-icon">
            <Scale size={20} />
          </div>
          <div>
            <h1 style={{ fontSize: '1.3rem', fontWeight: 800 }}>GameBalance AI</h1>
            <p className="text-xs text-muted">Smart Gaming Monitoring & Student Wellness</p>
          </div>
        </div>

        <h2 style={{ fontSize: '1.25rem', fontWeight: 700, marginBottom: '0.25rem' }}>Welcome back</h2>
        <p className="text-sm text-muted mb-3">Sign in to view your gaming, study, and sleep balance.</p>

        {error && (
          <div className="alert alert-danger mb-3">
            <span>{error}</span>
          </div>
        )}

        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label className="form-label" htmlFor="login-email">Student Email</label>
            <input
              id="login-email"
              type="email"
              className="form-input"
              placeholder="alex.rivera@university.edu"
              value={form.email}
              onChange={(e) => setForm({ ...form, email: e.target.value })}
              required
            />
          </div>

          <div className="form-group">
            <label className="form-label" htmlFor="login-password">Password</label>
            <input
              id="login-password"
              type="password"
              className="form-input"
              placeholder="Enter your password"
              value={form.password}
              onChange={(e) => setForm({ ...form, password: e.target.value })}
              required
            />
          </div>

          <button type="submit" className="btn btn-primary" style={{ width: '100%' }} disabled={submitting}>
            <LogIn size={17} />
            <span>{submitting ? 'Signing in...' : 'Sign In'}</span>
          </button>
        </form>

        <p className="text-sm text-muted mt-3" style={{ textAlign: 'center' }}>
          New to GameBalance AI?{' '}
          <Link to="/register" className="font-semibold">
            Create student account
          </Link>
        </p>
      </div>
    </div>
  );
}
