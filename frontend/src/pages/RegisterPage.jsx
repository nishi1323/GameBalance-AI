import { useState } from 'react';
import { Link, Navigate, useNavigate } from 'react-router-dom';
import { Scale, UserPlus } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { extractErrorMessage } from '../services/api';
import ThemeToggle from '../components/ThemeToggle';

export default function RegisterPage() {
  const { register, isAuthenticated } = useAuth();
  const navigate = useNavigate();

  const [form, setForm] = useState({
    fullName: '',
    email: '',
    password: '',
    confirmPassword: '',
    age: '',
    collegeName: '',
  });
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  if (isAuthenticated) {
    return <Navigate to="/dashboard" replace />;
  }

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    if (form.password.length < 6) {
      setError('Password must be at least 6 characters long.');
      return;
    }
    if (form.password !== form.confirmPassword) {
      setError('Password and confirm password do not match.');
      return;
    }
    const numericAge = Number(form.age);
    if (!numericAge || numericAge < 13 || numericAge > 100) {
      setError('Please enter a valid age between 13 and 100.');
      return;
    }

    setSubmitting(true);
    try {
      await register({
        fullName: form.fullName.trim(),
        email: form.email.trim(),
        password: form.password,
        confirmPassword: form.confirmPassword,
        age: numericAge,
        collegeName: form.collegeName.trim(),
      });
      navigate('/dashboard');
    } catch (err) {
      setError(extractErrorMessage(err, 'Registration failed. Please verify your details.'));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="auth-container">
      <div style={{ position: 'fixed', top: '1rem', right: '1rem' }}>
        <ThemeToggle />
      </div>
      <div className="auth-card" style={{ maxWidth: '520px' }}>
        <div className="flex items-center gap-2 mb-3">
          <div className="brand-icon">
            <Scale size={20} />
          </div>
          <div>
            <h1 style={{ fontSize: '1.3rem', fontWeight: 800 }}>GameBalance AI</h1>
            <p className="text-xs text-muted">Student Wellness & Gaming Balance System</p>
          </div>
        </div>

        <h2 style={{ fontSize: '1.25rem', fontWeight: 700, marginBottom: '0.25rem' }}>Create your account</h2>
        <p className="text-sm text-muted mb-3">Track gaming time, protect study goals, and improve sleep habits.</p>

        {error && (
          <div className="alert alert-danger mb-3">
            <span>{error}</span>
          </div>
        )}

        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label className="form-label" htmlFor="reg-name">Full Name *</label>
            <input
              id="reg-name"
              type="text"
              className="form-input"
              placeholder="Alex Rivera"
              value={form.fullName}
              onChange={(e) => setForm({ ...form, fullName: e.target.value })}
              required
            />
          </div>

          <div className="grid grid-cols-2" style={{ gap: '0.75rem' }}>
            <div className="form-group">
              <label className="form-label" htmlFor="reg-email">Email *</label>
              <input
                id="reg-email"
                type="email"
                className="form-input"
                placeholder="alex@college.edu"
                value={form.email}
                onChange={(e) => setForm({ ...form, email: e.target.value })}
                required
              />
            </div>
            <div className="form-group">
              <label className="form-label" htmlFor="reg-age">Age *</label>
              <input
                id="reg-age"
                type="number"
                min="13"
                max="100"
                className="form-input"
                placeholder="20"
                value={form.age}
                onChange={(e) => setForm({ ...form, age: e.target.value })}
                required
              />
            </div>
          </div>

          <div className="form-group">
            <label className="form-label" htmlFor="reg-college">Student / College Name (Optional)</label>
            <input
              id="reg-college"
              type="text"
              className="form-input"
              placeholder="State Institute of Technology"
              value={form.collegeName}
              onChange={(e) => setForm({ ...form, collegeName: e.target.value })}
            />
          </div>

          <div className="grid grid-cols-2" style={{ gap: '0.75rem' }}>
            <div className="form-group">
              <label className="form-label" htmlFor="reg-password">Password *</label>
              <input
                id="reg-password"
                type="password"
                className="form-input"
                placeholder="Min 6 characters"
                value={form.password}
                onChange={(e) => setForm({ ...form, password: e.target.value })}
                required
              />
            </div>
            <div className="form-group">
              <label className="form-label" htmlFor="reg-confirm">Confirm Password *</label>
              <input
                id="reg-confirm"
                type="password"
                className="form-input"
                placeholder="Repeat password"
                value={form.confirmPassword}
                onChange={(e) => setForm({ ...form, confirmPassword: e.target.value })}
                required
              />
            </div>
          </div>

          <button type="submit" className="btn btn-primary" style={{ width: '100%' }} disabled={submitting}>
            <UserPlus size={17} />
            <span>{submitting ? 'Creating Account...' : 'Create Student Account'}</span>
          </button>
        </form>

        <p className="text-sm text-muted mt-3" style={{ textAlign: 'center' }}>
          Already registered?{' '}
          <Link to="/login" className="font-semibold">
            Sign in instead
          </Link>
        </p>
      </div>
    </div>
  );
}
