import { Link } from 'react-router-dom'

export default function NotFound() {
  return (
    <main className="grid min-h-screen place-items-center px-4 text-center">
      <div>
        <p className="text-sm font-bold uppercase tracking-widest text-blue-700">404</p>
        <h1 className="mt-3 text-4xl font-extrabold text-slate-950">Page not found</h1>
        <p className="mt-3 text-slate-600">The requested page does not exist or is no longer available.</p>
        <Link to="/dashboard" className="button-primary mt-6">Return to dashboard</Link>
      </div>
    </main>
  )
}
