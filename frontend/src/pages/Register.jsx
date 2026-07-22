import { useState } from 'react'
import { Link, Navigate, useNavigate } from 'react-router-dom'
import apiClient, { apiErrorMessage } from '../api/axios.js'
import Alert from '../components/Alert.jsx'
import { useAuth } from '../context/AuthContext.jsx'

export default function Register() {
  const { user, authenticate } = useAuth()
  const navigate = useNavigate()
  const [form, setForm] = useState({ name: '', email: '', password: '' })
  const [error, setError] = useState(null)
  const [submitting, setSubmitting] = useState(false)

  if (user) return <Navigate to="/dashboard" replace />

  const submit = async (event) => {
    event.preventDefault()
    setSubmitting(true)
    setError(null)
    try {
      const response = await apiClient.post('/auth/register', form)
      authenticate(response.data)
      navigate('/dashboard', { replace: true })
    } catch (registerError) {
      setError(apiErrorMessage(registerError, 'Registration failed'))
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
        <h1 className="text-3xl font-bold tracking-tight text-slate-950">Create your account</h1>
        <p className="mt-2 text-slate-600">Start with 10 upload credits and private file sharing.</p>

        <form className="mt-7 space-y-4" onSubmit={submit}>
          <label className="block">
            <span className="mb-1.5 block text-sm font-semibold text-slate-800">Name</span>
            <input
              className="input-field"
              type="text"
              minLength="2"
              maxLength="80"
              autoComplete="name"
              required
              value={form.name}
              onChange={(event) => setForm({ ...form, name: event.target.value })}
            />
          </label>
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
              minLength="8"
              maxLength="72"
              autoComplete="new-password"
              required
              value={form.password}
              onChange={(event) => setForm({ ...form, password: event.target.value })}
            />
            <span className="mt-1 block text-xs text-slate-500">Use at least 8 characters.</span>
          </label>
          <Alert type="error">{error}</Alert>
          <button type="submit" className="button-primary w-full" disabled={submitting}>
            {submitting ? 'Creating account…' : 'Create account'}
          </button>
        </form>

        <p className="mt-6 text-center text-sm text-slate-600">
          Already registered? <Link to="/login" className="font-bold text-blue-700 hover:underline">Sign in</Link>
        </p>
      </section>
    </main>
  )
}
