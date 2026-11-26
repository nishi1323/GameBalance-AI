import { useState } from 'react';

export function BarChart({
  data = [],
  unit = '',
  barColor = 'var(--accent)',
  targetLine,
  height = 210,
  emptyText = 'No chart data recorded yet.',
}) {
  const [hoveredIdx, setHoveredIdx] = useState(null);

  if (!data || data.length === 0) {
    return <div className="empty-state text-sm">{emptyText}</div>;
  }

  const maxVal = Math.max(
    1,
    targetLine || 0,
    ...data.map((d) => Number(d.value) || 0)
  );

  const chartHeight = height - 42;
  const width = 560;
  const padX = 28;
  const usableW = width - padX * 2;
  const slotW = usableW / data.length;
  const barW = Math.min(38, slotW * 0.56);

  return (
    <div style={{ width: '100%', overflowX: 'auto' }}>
      <svg viewBox={`0 0 ${width} ${height}`} style={{ width: '100%', minWidth: '320px', height: 'auto' }}>
        {[0.25, 0.5, 0.75, 1].map((ratio) => {
          const y = 14 + chartHeight * (1 - ratio);
          return (
            <line
              key={ratio}
              x1={padX}
              x2={width - padX}
              y1={y}
              y2={y}
              stroke="var(--border-color)"
              strokeDasharray="4 4"
            />
          );
        })}

        {targetLine > 0 && (
          <g>
            <line
              x1={padX}
              x2={width - padX}
              y1={14 + chartHeight * (1 - targetLine / maxVal)}
              y2={14 + chartHeight * (1 - targetLine / maxVal)}
              stroke="var(--warning)"
              strokeWidth="1.75"
              strokeDasharray="6 4"
            />
            <text
              x={width - padX}
              y={14 + chartHeight * (1 - targetLine / maxVal) - 5}
              textAnchor="end"
              fill="var(--warning)"
              fontSize="11"
              fontWeight="600"
            >
              Target ({targetLine}{unit})
            </text>
          </g>
        )}

        {data.map((item, idx) => {
          const val = Number(item.value) || 0;
          const barH = Math.max(val > 0 ? 4 : 0, (val / maxVal) * chartHeight);
          const x = padX + idx * slotW + (slotW - barW) / 2;
          const y = 14 + chartHeight - barH;
          const isHovered = hoveredIdx === idx;

          return (
            <g
              key={item.date || idx}
              onMouseEnter={() => setHoveredIdx(idx)}
              onMouseLeave={() => setHoveredIdx(null)}
              style={{ cursor: 'pointer' }}
            >
              <rect
                x={x}
                y={y}
                width={barW}
                height={barH}
                rx="6"
                fill={barColor}
                opacity={isHovered ? 1 : 0.85}
              />
              {(isHovered || val > 0) && (
                <text
                  x={x + barW / 2}
                  y={Math.max(11, y - 5)}
                  textAnchor="middle"
                  fill="var(--text-secondary)"
                  fontSize="11"
                  fontWeight="600"
                >
                  {val}{unit}
                </text>
              )}
              <text
                x={x + barW / 2}
                y={height - 10}
                textAnchor="middle"
                fill="var(--text-muted)"
                fontSize="12"
              >
                {item.label}
              </text>
            </g>
          );
        })}
      </svg>
    </div>
  );
}

export function DualBarChart({
  data = [],
  primaryLabel = 'Planned',
  secondaryLabel = 'Actual',
  unit = 'm',
  height = 220,
}) {
  if (!data || data.length === 0) {
    return <div className="empty-state text-sm">No comparison data yet.</div>;
  }

  const maxVal = Math.max(
    1,
    ...data.map((d) => Math.max(Number(d.value) || 0, Number(d.secondaryValue) || 0))
  );

  const chartHeight = height - 48;
  const width = 560;
  const padX = 24;
  const slotW = (width - padX * 2) / data.length;
  const barW = Math.min(20, slotW * 0.32);

  return (
    <div>
      <div className="flex items-center gap-3 mb-2 text-xs">
        <span className="flex items-center gap-1">
          <span style={{ width: 10, height: 10, borderRadius: 3, background: 'var(--accent)', display: 'inline-block' }} />
          {primaryLabel}
        </span>
        <span className="flex items-center gap-1">
          <span style={{ width: 10, height: 10, borderRadius: 3, background: 'var(--info)', display: 'inline-block' }} />
          {secondaryLabel}
        </span>
      </div>
      <div style={{ width: '100%', overflowX: 'auto' }}>
        <svg viewBox={`0 0 ${width} ${height}`} style={{ width: '100%', minWidth: '320px', height: 'auto' }}>
          {data.map((item, idx) => {
            const v1 = Number(item.value) || 0;
            const v2 = Number(item.secondaryValue) || 0;
            const h1 = Math.max(v1 > 0 ? 4 : 0, (v1 / maxVal) * chartHeight);
            const h2 = Math.max(v2 > 0 ? 4 : 0, (v2 / maxVal) * chartHeight);
            const center = padX + idx * slotW + slotW / 2;

            return (
              <g key={item.date || idx}>
                <rect x={center - barW - 2} y={16 + chartHeight - h1} width={barW} height={h1} rx="4" fill="var(--accent)" />
                <rect x={center + 2} y={16 + chartHeight - h2} width={barW} height={h2} rx="4" fill="var(--info)" />
                <text x={center} y={height - 12} textAnchor="middle" fill="var(--text-muted)" fontSize="12">
                  {item.label}
                </text>
              </g>
            );
          })}
        </svg>
      </div>
    </div>
  );
}

export function RiskGauge({ score = 0, category = 'LOW' }) {
  const clamped = Math.min(100, Math.max(0, Number(score) || 0));
  const color =
    category === 'LOW'
      ? 'var(--success)'
      : category === 'MODERATE'
      ? 'var(--warning)'
      : 'var(--danger)';

  const radius = 64;
  const circumference = Math.PI * radius;
  const offset = circumference * (1 - clamped / 100);

  return (
    <div className="flex flex-col items-center" style={{ padding: '0.5rem 0' }}>
      <svg viewBox="0 0 180 110" style={{ width: '210px', height: 'auto' }}>
        <path
          d="M 26 90 A 64 64 0 0 1 154 90"
          fill="none"
          stroke="var(--border-color)"
          strokeWidth="14"
          strokeLinecap="round"
        />
        <path
          d="M 26 90 A 64 64 0 0 1 154 90"
          fill="none"
          stroke={color}
          strokeWidth="14"
          strokeLinecap="round"
          strokeDasharray={circumference}
          strokeDashoffset={offset}
          style={{ transition: 'stroke-dashoffset 0.5s ease' }}
        />
        <text x="90" y="76" textAnchor="middle" fill="var(--text-primary)" fontSize="28" fontWeight="800">
          {clamped}
        </text>
        <text x="90" y="96" textAnchor="middle" fill="var(--text-muted)" fontSize="12">
          out of 100
        </text>
      </svg>
      <span
        className={`badge ${
          category === 'LOW'
            ? 'badge-success'
            : category === 'MODERATE'
            ? 'badge-warning'
            : 'badge-danger'
        }`}
      >
        {category.replace('_', ' ')} RISK
      </span>
    </div>
  );
}
