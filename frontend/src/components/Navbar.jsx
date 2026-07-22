import { NavLink, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext.jsx'

const navClass = ({ isActive }) =>
  `rounded-lg px-3 py-2 text-sm font-semibold transition ${
    isActive ? 'bg-blue-50 text-blue-700' : 'text-slate-600 hover:bg-slate-100 hover:text-slate-950'
  }`

export default function Navbar() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()

  const signOut = () => {
    logout()
    navigate('/login', { replace: true })
  }

  return (
    <header className="sticky top-0 z-30 border-b border-slate-200 bg-white/95 backdrop-blur">
      <div className="mx-auto flex max-w-7xl items-center justify-between gap-4 px-4 py-3 sm:px-6 lg:px-8">
        <div className="flex items-center gap-8">
          <NavLink to="/dashboard" className="flex items-center gap-2 text-lg font-extrabold tracking-tight text-slate-950">
            <span className="grid h-9 w-9 place-items-center rounded-xl bg-blue-600 text-white">CS</span>
            <span className="hidden sm:inline">Cloud Share</span>
          </NavLink>
          <nav className="flex items-center gap-1" aria-label="Primary navigation">
            <NavLink to="/dashboard" className={navClass}>Dashboard</NavLink>
            <NavLink to="/files" className={navClass}>My files</NavLink>
            <NavLink to="/plans" className={navClass}>Plans</NavLink>
          </nav>
        </div>

        <div className="flex items-center gap-3">
          <div className="hidden text-right md:block">
            <p className="text-sm font-semibold text-slate-950">{user?.name}</p>
            <p className="text-xs text-slate-500">{user?.credits} credits · {user?.planType}</p>
          </div>
          <button type="button" onClick={signOut} className="button-secondary px-3 py-2 text-sm">
            Sign out
          </button>
        </div>
      </div>
    </header>
  )
}
