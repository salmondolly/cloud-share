import { useState } from 'react'
import { Link, Navigate, useLocation, useNavigate } from 'react-router-dom'
import apiClient, { apiErrorMessage } from '../api/axios.js'
import Alert from '../components/Alert.jsx'
import { useAuth } from '../context/AuthContext.jsx'

export default function Login() {
  const { user, authenticate } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const [form, setForm] = useState({ email: '', password: '' })
  const [error, setError] = useState(null)
  const [submitting, setSubmitting] = useState(false)

  if (user) return <Navigate to="/dashboard" replace />

  const submit = async (event) => {
    event.preventDefault()
    setSubmitting(true)
    setError(null)
    try {
      const response = await apiClient.post('/auth/login', form)
      authenticate(response.data)
      navigate(location.state?.from?.pathname || '/dashboard', { replace: true })
    } catch (loginError) {
      setError(apiErrorMessage(loginError, 'Login failed'))
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <main className="grid min-h-screen place-items-center px-4 py-10">
      <section className="card w-full max-w-md p-7 sm:p-8">
        <Link to="/" className="mb-8 flex items-center gap-2 text-xl font-extrabold text-slate-950">
          <span className="grid h-10 w-10 place-items-center rounded-xl bg-blue-600 text-sm text-white">CS</span>
          Cloud Share
        </Link>
        <h1 className="text-3xl font-bold tracking-tight text-slate-950">Welcome back</h1>
        <p className="mt-2 text-slate-600">Sign in to upload, manage, and share your files.</p>

        <form className="mt-7 space-y-4" onSubmit={submit}>
          <label className="block">
            <span className="mb-1.5 block text-sm font-semibold text-slate-800">Email</span>
            <input
              className="input-field"
              type="email"
              autoComplete="email"
              required
              value={form.email}
              onChange={(event) => setForm({ ...form, email: event.target.value })}
            />
          </label>
          <label className="block">
            <span className="mb-1.5 block text-sm font-semibold text-slate-800">Password</span>
            <input
              className="input-field"
              type="password"
              autoComplete="current-password"
              required
              value={form.password}
              onChange={(event) => setForm({ ...form, password: event.target.value })}
            />
          </label>
          <Alert type="error">{error}</Alert>
          <button type="submit" className="button-primary w-full" disabled={submitting}>
            {submitting ? 'Signing in…' : 'Sign in'}
          </button>
        </form>

        <p className="mt-6 text-center text-sm text-slate-600">
          New here? <Link to="/register" className="font-bold text-blue-700 hover:underline">Create an account</Link>
        </p>
      </section>
    </main>
  )
}
