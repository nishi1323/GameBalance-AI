export default function LoadingSpinner({ text = 'Loading...' }) {
  return (
    <div className="flex flex-col items-center justify-between gap-2" style={{ padding: '2.5rem 1rem', justifyContent: 'center' }}>
      <div className="spinner" />
      <span className="text-sm text-muted">{text}</span>
    </div>
  );
}
