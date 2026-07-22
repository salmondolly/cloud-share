import { useCallback, useEffect, useState } from 'react'
import apiClient, { apiErrorMessage } from '../api/axios.js'
import Alert from '../components/Alert.jsx'
import FileTable from '../components/FileTable.jsx'
import PageLoader from '../components/PageLoader.jsx'

export default function MyFiles() {
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

  return (
    <div className="space-y-6">
      <section>
        <p className="text-sm font-bold uppercase tracking-widest text-blue-700">Storage</p>
        <h1 className="page-title mt-2">My files</h1>
        <p className="mt-2 text-slate-600">Download, delete, or change the visibility of your uploads.</p>
      </section>
      <Alert type="error">{error}</Alert>
      {loading ? <PageLoader label="Loading your files…" /> : <FileTable files={files} onChanged={loadFiles} />}
    </div>
  )
}
