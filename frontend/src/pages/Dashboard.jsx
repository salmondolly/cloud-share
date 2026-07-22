import { useCallback, useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import apiClient, { apiErrorMessage } from '../api/axios.js'
import Alert from '../components/Alert.jsx'
import FileTable from '../components/FileTable.jsx'
import FileUpload from '../components/FileUpload.jsx'
import PageLoader from '../components/PageLoader.jsx'
import { useAuth } from '../context/AuthContext.jsx'
import { formatBytes } from '../utils/format.js'

export default function Dashboard() {
  const { user } = useAuth()
  const [files, setFiles] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  const loadFiles = useCallback(async () => {
    try {
      setError(null)
      const response = await apiClient.get('/files')
      setFiles(response.data)
    } catch (loadError) {
      setError(apiErrorMessage(loadError, 'Could not load files'))
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    loadFiles()
  }, [loadFiles])

  const totalBytes = files.reduce((sum, file) => sum + file.fileSize, 0)
  const publicFiles = files.filter((file) => file.visibility === 'PUBLIC').length

  return (
    <div className="space-y-8">
      <section>
        <p className="text-sm font-bold uppercase tracking-widest text-blue-700">Overview</p>
        <h1 className="page-title mt-2">Hello, {user.name}</h1>
        <p className="mt-2 text-slate-600">Manage uploads, credits, and public share links from one place.</p>
      </section>

      <section className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <Metric label="Plan" value={user.planType} />
        <Metric label="Credits remaining" value={user.credits} />
        <Metric label="Files stored" value={files.length} />
        <Metric label="Stored data" value={formatBytes(totalBytes)} detail={`${publicFiles} public`} />
      </section>

      <FileUpload onUploaded={loadFiles} />

      <section className="space-y-4">
        <div className="flex items-center justify-between gap-4">
          <div>
            <h2 className="text-xl font-bold text-slate-950">Recent files</h2>
            <p className="mt-1 text-sm text-slate-600">Your five most recent uploads.</p>
          </div>
          <Link to="/files" className="button-secondary">View all files</Link>
        </div>
        <Alert type="error">{error}</Alert>
        {loading ? <PageLoader label="Loading files…" /> : <FileTable files={files} compact onChanged={loadFiles} />}
      </section>
    </div>
  )
}

function Metric({ label, value, detail }) {
  return (
    <article className="card p-5">
      <p className="text-sm font-semibold text-slate-500">{label}</p>
      <p className="mt-2 text-2xl font-extrabold text-slate-950">{value}</p>
      {detail && <p className="mt-1 text-xs text-slate-500">{detail}</p>}
    </article>
  )
}
