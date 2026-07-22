import { useState } from 'react'
import apiClient, { apiErrorMessage } from '../api/axios.js'
import { formatBytes, formatDate } from '../utils/format.js'
import Alert from './Alert.jsx'
import EmptyState from './EmptyState.jsx'

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

export default function FileTable({ files, onChanged, compact = false }) {
  const [busyId, setBusyId] = useState(null)
  const [notice, setNotice] = useState(null)
  const [error, setError] = useState(null)

  if (!files?.length) {
    return <EmptyState title="No files yet" message="Upload your first file to see it here." />
  }

  const run = async (fileId, action) => {
    setBusyId(fileId)
    setError(null)
    setNotice(null)
    try {
      await action()
    } catch (actionError) {
      setError(apiErrorMessage(actionError))
    } finally {
      setBusyId(null)
    }
  }

  const download = (file) => run(file.id, async () => {
    const response = await apiClient.get(`/files/${file.id}/download`, { responseType: 'blob' })
    saveBlob(response.data, file.originalFileName)
  })

  const toggleVisibility = (file) => run(file.id, async () => {
    const visibility = file.visibility === 'PUBLIC' ? 'PRIVATE' : 'PUBLIC'
    await apiClient.patch(`/files/${file.id}/visibility`, { visibility })
    setNotice(`${file.originalFileName} is now ${visibility.toLowerCase()}.`)
    await onChanged?.()
  })

  const remove = (file) => {
    if (!window.confirm(`Delete ${file.originalFileName}? This cannot be undone.`)) return
    run(file.id, async () => {
      await apiClient.delete(`/files/${file.id}`)
      setNotice(`${file.originalFileName} was deleted.`)
      await onChanged?.()
    })
  }

  const copyLink = async (file) => {
    try {
      await navigator.clipboard.writeText(file.shareUrl)
      setNotice('Share link copied to the clipboard.')
      setError(null)
    } catch {
      setError('Could not copy automatically. Select and copy the link manually.')
    }
  }

  const visibleFiles = compact ? files.slice(0, 5) : files

  return (
    <div className="space-y-4">
      <Alert type="error">{error}</Alert>
      <Alert type="success">{notice}</Alert>
      <div className="overflow-x-auto rounded-2xl border border-slate-200 bg-white shadow-sm">
        <table className="min-w-full divide-y divide-slate-200 text-left text-sm">
          <thead className="bg-slate-50 text-xs uppercase tracking-wide text-slate-600">
            <tr>
              <th className="px-4 py-3 font-bold">File</th>
              <th className="px-4 py-3 font-bold">Visibility</th>
              <th className="px-4 py-3 font-bold">Uploaded</th>
              <th className="px-4 py-3 font-bold">Downloads</th>
              <th className="px-4 py-3 text-right font-bold">Actions</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100">
            {visibleFiles.map((file) => (
              <tr key={file.id} className="align-top hover:bg-slate-50/70">
                <td className="max-w-xs px-4 py-4">
                  <p className="truncate font-bold text-slate-950" title={file.originalFileName}>{file.originalFileName}</p>
                  <p className="mt-1 text-xs text-slate-500">{formatBytes(file.fileSize)} · {file.contentType}</p>
                  {file.shareUrl && (
                    <p className="mt-2 truncate text-xs text-blue-700" title={file.shareUrl}>{file.shareUrl}</p>
                  )}
                </td>
                <td className="px-4 py-4">
                  <span className={`rounded-full px-2.5 py-1 text-xs font-bold ${
                    file.visibility === 'PUBLIC'
                      ? 'bg-emerald-50 text-emerald-700'
                      : 'bg-slate-100 text-slate-700'
                  }`}>
                    {file.visibility}
                  </span>
                </td>
                <td className="whitespace-nowrap px-4 py-4 text-slate-600">{formatDate(file.uploadedAt)}</td>
                <td className="px-4 py-4 font-semibold text-slate-700">{file.downloadCount}</td>
                <td className="px-4 py-4">
                  <div className="flex flex-wrap justify-end gap-2">
                    <button type="button" className="button-secondary px-3 py-2 text-xs" disabled={busyId === file.id} onClick={() => download(file)}>
                      Download
                    </button>
                    <button type="button" className="button-secondary px-3 py-2 text-xs" disabled={busyId === file.id} onClick={() => toggleVisibility(file)}>
                      Make {file.visibility === 'PUBLIC' ? 'private' : 'public'}
                    </button>
                    {file.shareUrl && (
                      <button type="button" className="button-secondary px-3 py-2 text-xs" onClick={() => copyLink(file)}>
                        Copy link
                      </button>
                    )}
                    {!compact && (
                      <button type="button" className="button-danger text-xs" disabled={busyId === file.id} onClick={() => remove(file)}>
                        Delete
                      </button>
                    )}
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  )
}
