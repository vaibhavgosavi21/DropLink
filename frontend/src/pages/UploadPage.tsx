import { useState } from 'react'
import { uploadFile, uploadFiles } from '../api/fileApi'
import type { UploadedFile, ExpirationOption } from '../types'
import FileDropZone from '../components/FileDropZone'
import FilePreview from '../components/FilePreview'
import UploadProgress from '../components/UploadProgress'
import ShareResult from '../components/ShareResult'
import ErrorMessage from '../components/ErrorMessage'
import Header from '../components/Header'
import Footer from '../components/Footer'
import styles from './UploadPage.module.css'

const EXPIRY_OPTIONS: { value: ExpirationOption; label: string }[] = [
  { value: '1h', label: '1 Hour' },
  { value: '6h', label: '6 Hours' },
  { value: '24h', label: '24 Hours' },
  { value: '7d', label: '7 Days' },
  { value: '30d', label: '30 Days' },
  { value: 'never', label: 'Never' },
]

type Status = 'idle' | 'uploading' | 'done' | 'error'

export default function UploadPage() {
  const [files, setFiles] = useState<File[]>([])
  const [expiresIn, setExpiresIn] = useState<ExpirationOption>('never')
  const [status, setStatus] = useState<Status>('idle')
  const [progress, setProgress] = useState(0)
  const [results, setResults] = useState<UploadedFile[]>([])
  const [error, setError] = useState('')

  const addFiles = (incoming: File[]) => {
    setFiles((prev) => {
      const names = new Set(prev.map((f) => f.name))
      return [...prev, ...incoming.filter((f) => !names.has(f.name))]
    })
    setStatus('idle')
    setError('')
  }

  const removeFile = (index: number) => {
    setFiles((prev) => prev.filter((_, i) => i !== index))
  }

  const upload = async () => {
    if (files.length === 0) return
    setStatus('uploading')
    setProgress(0)
    setError('')
    try {
      if (files.length === 1) {
        const res = await uploadFile(files[0], expiresIn, setProgress)
        setResults(res.file ? [res.file] : [])
      } else {
        const res = await uploadFiles(files, expiresIn, setProgress)
        setResults(res.files ?? [])
      }
      setStatus('done')
      setFiles([])
    } catch (err: unknown) {
      const msg =
        (err as { response?: { data?: { message?: string } } })?.response?.data?.message ??
        'Upload failed. Please try again.'
      setError(msg)
      setStatus('error')
    }
  }

  const reset = () => {
    setStatus('idle')
    setResults([])
    setFiles([])
    setError('')
    setProgress(0)
  }

  return (
    <div className={styles.page}>
      <Header />
      <main className={styles.main}>
        <div className={styles.hero}>
          <h1 className={styles.title}>Share files anywhere</h1>
          <p className={styles.subtitle}>Upload once, share instantly — no account needed</p>
        </div>

        <div className={styles.card}>
          {status === 'done' ? (
            <div className={styles.results}>
              {results.map((r) => (
                <ShareResult key={r.shareId} fileName={r.fileName} shareUrl={r.shareUrl} />
              ))}
              <button className={styles.btnSecondary} onClick={reset}>Upload More Files</button>
            </div>
          ) : (
            <>
              <FileDropZone onFiles={addFiles} multiple />

              {files.length > 0 && (
                <div className={styles.fileList}>
                  {files.map((f, i) => (
                    <FilePreview key={`${f.name}-${i}`} file={f} onRemove={() => removeFile(i)} />
                  ))}
                </div>
              )}

              <div className={styles.options}>
                <label className={styles.label}>
                  Link expires in
                  <select
                    value={expiresIn}
                    onChange={(e) => setExpiresIn(e.target.value as ExpirationOption)}
                    className={styles.select}
                  >
                    {EXPIRY_OPTIONS.map((o) => (
                      <option key={o.value} value={o.value}>{o.label}</option>
                    ))}
                  </select>
                </label>
              </div>

              {status === 'uploading' && <UploadProgress progress={progress} />}
              {status === 'error' && <ErrorMessage message={error} />}

              <button
                className={styles.btnPrimary}
                onClick={upload}
                disabled={files.length === 0 || status === 'uploading'}
              >
                {status === 'uploading' ? 'Uploading…' : `Upload ${files.length > 1 ? `${files.length} Files` : 'File'}`}
              </button>
            </>
          )}
        </div>
      </main>
      <Footer />
    </div>
  )
}
