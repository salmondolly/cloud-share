import { formatBytes, formatRupees } from '../utils/format.js'

export default function PlanCard({ plan, currentPlan, busy, onBuy }) {
  const current = plan.planType === currentPlan
  const free = plan.pricePaise === 0

  return (
    <article className={`card relative flex h-full flex-col p-6 ${current ? 'ring-2 ring-blue-500' : ''}`}>
      {current && (
        <span className="absolute right-4 top-4 rounded-full bg-blue-50 px-3 py-1 text-xs font-bold text-blue-700">
          Current plan
        </span>
      )}
      <p className="text-sm font-bold uppercase tracking-widest text-blue-700">{plan.displayName}</p>
      <div className="mt-4 flex items-baseline gap-1">
        <span className="text-4xl font-extrabold tracking-tight text-slate-950">
          {free ? 'Free' : formatRupees(plan.pricePaise)}
        </span>
        {!free && <span className="text-sm text-slate-500">one-time credit pack</span>}
      </div>
      <ul className="mt-6 flex-1 space-y-3 text-sm text-slate-700">
        <li>✓ {plan.uploadCredits} upload credits</li>
        <li>✓ Files up to {formatBytes(plan.maxFileSizeBytes)}</li>
        <li>✓ Private and public sharing</li>
        <li>✓ Download tracking</li>
      </ul>
      <button
        type="button"
        className={current || free ? 'button-secondary mt-7 w-full' : 'button-primary mt-7 w-full'}
        disabled={current || free || busy}
        onClick={() => onBuy(plan)}
      >
        {current ? 'Active' : free ? 'Included on registration' : busy ? 'Opening checkout…' : `Buy ${plan.displayName}`}
      </button>
    </article>
  )
}
