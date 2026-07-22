import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import apiClient, { apiErrorMessage } from '../api/axios.js'
import Alert from '../components/Alert.jsx'
import PageLoader from '../components/PageLoader.jsx'
import { formatBytes, formatDate } from '../utils/format.js'

function saveBlob(blob, fileName) {
  const url = URL.createObjectURL(blob)
  const anchor = document.createElement('a')
  anchor.href = url
  anchor.download = fileName
  document.body.appendChild(anchor)
  anchor.click()
  anchor.remove()
  URL.revokeObjectURL(url)
}

export default function SharedFile() {
  const { token } = useParams()
  const [file, setFile] = useState(null)
  const [loading, setLoading] = useState(true)
  const [downloading, setDownloading] = useState(false)
  const [error, setError] = useState(null)

  useEffect(() => {
    apiClient.get(`/share/${token}`)
      .then((response) => setFile(response.data))
      .catch((loadError) => setError(apiErrorMessage(loadError, 'Shared file not found')))
      .finally(() => setLoading(false))
  }, [token])

  const download = async () => {
    setDownloading(true)
    setError(null)
    try {
      const response = await apiClient.get(`/share/${token}/download`, { responseType: 'blob' })
      saveBlob(response.data, file.originalFileName)
    } catch (downloadError) {
      setError(apiErrorMessage(downloadError, 'Download failed'))
    } finally {
      setDownloading(false)
    }
  }

  if (loading) return <PageLoader label="Opening shared file…" />

  return (
    <main className="grid min-h-screen place-items-center px-4 py-10">
      <section className="card w-full max-w-xl p-7 sm:p-9">
        <Link to="/" className="flex items-center gap-2 text-xl font-extrabold text-slate-950">
          <span className="grid h-10 w-10 place-items-center rounded-xl bg-blue-600 text-sm text-white">CS</span>
          Cloud Share
        </Link>
        <Alert type="error">{error}</Alert>
        {file && (
          <div className="mt-8">
            <p className="text-sm font-bold uppercase tracking-widest text-emerald-700">Public shared file</p>
            <h1 className="mt-3 break-words text-3xl font-bold text-slate-950">{file.originalFileName}</h1>
            <dl className="mt-6 grid gap-4 rounded-2xl bg-slate-50 p-5 sm:grid-cols-2">
              <div>
                <dt className="text-xs font-bold uppercase tracking-wide text-slate-500">Size</dt>
                <dd className="mt-1 font-semibold text-slate-950">{formatBytes(file.fileSize)}</dd>
              </div>
              <div>
                <dt className="text-xs font-bold uppercase tracking-wide text-slate-500">Type</dt>
                <dd className="mt-1 break-all font-semibold text-slate-950">{file.contentType}</dd>
              </div>
              <div>
                <dt className="text-xs font-bold uppercase tracking-wide text-slate-500">Uploaded</dt>
                <dd className="mt-1 font-semibold text-slate-950">{formatDate(file.uploadedAt)}</dd>
              </div>
              <div>
                <dt className="text-xs font-bold uppercase tracking-wide text-slate-500">Downloads</dt>
                <dd className="mt-1 font-semibold text-slate-950">{file.downloadCount}</dd>
              </div>
            </dl>
            <button type="button" className="button-primary mt-6 w-full" onClick={download} disabled={downloading}>
              {downloading ? 'Downloading…' : 'Download file'}
            </button>
          </div>
        )}
      </section>
    </main>
  )
}
