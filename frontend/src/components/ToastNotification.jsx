import { AlertCircle, CheckCircle2, Info, X } from 'lucide-react';

export default function ToastNotification({ notification, onDismiss }) {
  if (!notification) return null;

  const type = notification.type || 'info';
  const Icon = type === 'success' ? CheckCircle2 : type === 'danger' ? AlertCircle : Info;

  return (
    <div
      style={{
        position: 'fixed',
        bottom: '1.25rem',
        right: '1.25rem',
        zIndex: 70,
        maxWidth: '380px',
        boxShadow: 'var(--shadow-lg)',
      }}
      className={`alert alert-${type}`}
    >
      <Icon size={18} style={{ flexShrink: 0, marginTop: '2px' }} />
      <div style={{ flex: 1 }}>
        {notification.title && <div className="font-semibold text-sm">{notification.title}</div>}
        <div className="text-sm">{notification.message}</div>
      </div>
      {onDismiss && (
        <button
          type="button"
          onClick={onDismiss}
          style={{ background: 'none', border: 'none', color: 'inherit', cursor: 'pointer' }}
          aria-label="Dismiss"
        >
          <X size={16} />
        </button>
      )}
    </div>
  );
}
