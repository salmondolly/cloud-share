import { useRef, useState } from 'react'
import apiClient, { apiErrorMessage } from '../api/axios.js'
import { useAuth } from '../context/AuthContext.jsx'
import { formatBytes } from '../utils/format.js'
import Alert from './Alert.jsx'

export default function FileUpload({ onUploaded }) {
  const { user, refreshUser } = useAuth()
  const inputRef = useRef(null)
  const [file, setFile] = useState(null)
  const [visibility, setVisibility] = useState('PRIVATE')
  const [dragging, setDragging] = useState(false)
  const [uploading, setUploading] = useState(false)
  const [progress, setProgress] = useState(0)
  const [message, setMessage] = useState(null)
  const [error, setError] = useState(null)

  const chooseFile = (selected) => {
    setError(null)
    setMessage(null)
    if (!selected) return
    if (selected.size > user.maxFileSizeBytes) {
      setFile(null)
      setError(`Your ${user.planType} plan allows files up to ${formatBytes(user.maxFileSizeBytes)}.`)
      return
    }
    setFile(selected)
  }

  const onDrop = (event) => {
    event.preventDefault()
    setDragging(false)
    chooseFile(event.dataTransfer.files?.[0])
  }

  const submit = async (event) => {
    event.preventDefault()
    if (!file) {
      setError('Choose a file before uploading.')
      return
    }

    setUploading(true)
    setProgress(0)
    setError(null)
    setMessage(null)
    const formData = new FormData()
    formData.append('file', file)
    formData.append('visibility', visibility)

    try {
      const response = await apiClient.post('/files/upload', formData, {
        onUploadProgress: (progressEvent) => {
          if (progressEvent.total) {
            setProgress(Math.round((progressEvent.loaded * 100) / progressEvent.total))
          }
        },
      })
      setMessage(`${response.data.originalFileName} uploaded successfully.`)
      setFile(null)
      if (inputRef.current) inputRef.current.value = ''
      await refreshUser()
      await onUploaded?.(response.data)
    } catch (uploadError) {
      setError(apiErrorMessage(uploadError, 'Upload failed'))
    } finally {
      setUploading(false)
    }
  }

  return (
    <section className="card p-5 sm:p-6">
      <div className="mb-5 flex flex-wrap items-start justify-between gap-3">
        <div>
          <h2 className="text-xl font-bold text-slate-950">Upload a file</h2>
          <p className="mt-1 text-sm text-slate-600">
            1 upload uses 1 credit. Maximum size: {formatBytes(user.maxFileSizeBytes)}.
          </p>
        </div>
        <span className="rounded-full bg-blue-50 px-3 py-1 text-sm font-bold text-blue-700">
          {user.credits} credits left
        </span>
      </div>

      <form onSubmit={submit} className="space-y-4">
        <div
          className={`rounded-2xl border-2 border-dashed p-8 text-center transition ${
            dragging ? 'border-blue-500 bg-blue-50' : 'border-slate-300 bg-slate-50 hover:border-slate-400'
          }`}
          onDragEnter={(event) => { event.preventDefault(); setDragging(true) }}
          onDragOver={(event) => event.preventDefault()}
          onDragLeave={() => setDragging(false)}
          onDrop={onDrop}
        >
          <input
            ref={inputRef}
            type="file"
            className="hidden"
            onChange={(event) => chooseFile(event.target.files?.[0])}
          />
          <p className="font-bold text-slate-950">Drag and drop one file here</p>
          <p className="mt-1 text-sm text-slate-600">or select it from your computer</p>
          <button type="button" className="button-secondary mt-4" onClick={() => inputRef.current?.click()}>
            Browse files
          </button>
          {file && (
            <div className="mx-auto mt-5 max-w-lg rounded-xl border border-blue-200 bg-white px-4 py-3 text-left">
              <p className="truncate font-semibold text-slate-950">{file.name}</p>
              <p className="text-sm text-slate-500">{formatBytes(file.size)} · {file.type || 'Unknown type'}</p>
            </div>
          )}
        </div>

        <div className="grid gap-4 sm:grid-cols-[1fr_auto] sm:items-end">
          <label className="block">
            <span className="mb-1.5 block text-sm font-semibold text-slate-800">Initial visibility</span>
            <select className="input-field" value={visibility} onChange={(event) => setVisibility(event.target.value)}>
              <option value="PRIVATE">Private — only you can download it</option>
              <option value="PUBLIC">Public — create a share link</option>
            </select>
          </label>
          <button type="submit" className="button-primary min-w-36" disabled={uploading || !file || user.credits < 1}>
            {uploading ? `Uploading ${progress}%` : 'Upload file'}
          </button>
        </div>

        {uploading && (
          <div className="h-2 overflow-hidden rounded-full bg-slate-200" aria-label={`Upload progress ${progress}%`}>
            <div className="h-full rounded-full bg-blue-600 transition-all" style={{ width: `${progress}%` }} />
          </div>
        )}
        <Alert type="error">{error}</Alert>
        <Alert type="success">{message}</Alert>
      </form>
    </section>
  )
}
