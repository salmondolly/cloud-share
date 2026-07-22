export default function PageLoader({ label = 'Loading…' }) {
  return (
    <div className="flex min-h-[40vh] items-center justify-center" role="status" aria-live="polite">
      <div className="flex items-center gap-3 rounded-2xl border border-slate-200 bg-white px-5 py-4 text-slate-700 shadow-sm">
        <span className="h-5 w-5 animate-spin rounded-full border-2 border-slate-300 border-t-blue-600" />
        <span className="font-medium">{label}</span>
      </div>
    </div>
  )
}
