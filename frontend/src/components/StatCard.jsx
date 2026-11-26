export default function StatCard({ label, value, hint, icon: Icon, tone = 'accent', badge }) {
  const toneStyles = {
    accent: { bg: 'var(--accent-soft)', color: 'var(--accent)' },
    success: { bg: 'var(--success-soft)', color: 'var(--success)' },
    warning: { bg: 'var(--warning-soft)', color: 'var(--warning)' },
    danger: { bg: 'var(--danger-soft)', color: 'var(--danger)' },
    info: { bg: 'var(--info-soft)', color: 'var(--info)' },
  }[tone] || { bg: 'var(--accent-soft)', color: 'var(--accent)' };

  return (
    <div className="card card-hover stat-card">
      <div className="stat-top">
        <span className="stat-label">{label}</span>
        {Icon && (
          <div className="stat-icon" style={{ background: toneStyles.bg, color: toneStyles.color }}>
            <Icon size={19} />
          </div>
        )}
      </div>
      <div className="flex items-center justify-between gap-2">
        <span className="stat-value">{value}</span>
        {badge}
      </div>
      {hint && <span className="stat-hint">{hint}</span>}
    </div>
  );
}
