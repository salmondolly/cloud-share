const styles = {
  error: 'border-red-200 bg-red-50 text-red-800',
  success: 'border-emerald-200 bg-emerald-50 text-emerald-800',
  info: 'border-blue-200 bg-blue-50 text-blue-800',
}

export default function Alert({ type = 'info', children }) {
  if (!children) return null
  return (
    <div className={`rounded-xl border px-4 py-3 text-sm font-medium ${styles[type] || styles.info}`} role="alert">
      {children}
    </div>
  )
}
