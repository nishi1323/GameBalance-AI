export default function ProgressBar({ value = 0, max = 100, color = 'var(--accent)', label, rightText }) {
  const pct = max > 0 ? Math.min(100, Math.max(0, Math.round((value / max) * 100))) : 0;

  return (
    <div>
      {(label || rightText) && (
        <div className="flex items-center justify-between text-xs mb-1" style={{ marginBottom: '0.35rem' }}>
          <span className="text-muted">{label}</span>
          <span className="font-semibold">{rightText ?? `${pct}%`}</span>
        </div>
      )}
      <div className="progress-track">
        <div
          className="progress-fill"
          style={{
            width: `${pct}%`,
            background: color,
          }}
        />
      </div>
    </div>
  );
}
