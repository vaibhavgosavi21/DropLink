import { useEffect, useState } from 'react'
import { useParams, Link } from 'react-router-dom'
import { getFileMetadata, getDownloadUrl } from '../api/fileApi'
import type { FileMetadata } from '../types'
import DownloadButton from '../components/DownloadButton'
import Header from '../components/Header'
import Footer from '../components/Footer'
import styles from './SharePage.module.css'

function formatSize(bytes: number): string {
  if (bytes < 1024) return `${bytes} B`
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`
  if (bytes < 1024 * 1024 * 1024) return `${(bytes / 1024 / 1024).toFixed(2)} MB`
  return `${(bytes / 1024 / 1024 / 1024).toFixed(2)} GB`
}

function formatDate(iso: string): string {
  return new Date(iso).toLocaleDateString('en-US', { day: 'numeric', month: 'short', year: 'numeric' })
}

function fileIcon(type: string): string {
  if (type.startsWith('image/')) return '🖼️'
  if (type.startsWith('video/')) return '🎬'
  if (type === 'application/pdf') return '📄'
  if (type.includes('zip') || type.includes('tar') || type.includes('gz')) return '🗜️'
  if (type.includes('word') || type.includes('doc')) return '📝'
  if (type.includes('sheet') || type.includes('xls')) return '📊'
  return '📁'
}

type Status = 'loading' | 'ready' | 'expired' | 'notfound' | 'error'

export default function SharePage() {
  const { shareId } = useParams<{ shareId: string }>()
  const [meta, setMeta] = useState<FileMetadata | null>(null)
  const [status, setStatus] = useState<Status>('loading')

  useEffect(() => {
    if (!shareId) { setStatus('notfound'); return }
    getFileMetadata(shareId)
      .then((data) => { setMeta(data); setStatus('ready') })
      .catch((err: { response?: { status?: number } }) => {
        const code = err?.response?.status
        if (code === 410) setStatus('expired')
        else if (code === 404) setStatus('notfound')
        else setStatus('error')
      })
  }, [shareId])

  return (
    <div className={styles.page}>
      <Header />
      <main className={styles.main}>
        {status === 'loading' && (
          <div className={styles.card}>
            <div className={styles.spinner} />
            <p className={styles.loadingText}>Loading file info…</p>
          </div>
        )}

        {status === 'ready' && meta && (
          <div className={styles.card}>
            <div className={styles.iconWrap}>
              <span className={styles.fileIcon}>{fileIcon(meta.contentType)}</span>
            </div>
            <h1 className={styles.fileName}>{meta.fileName}</h1>

            <div className={styles.metaGrid}>
              <div className={styles.metaItem}>
                <span className={styles.metaLabel}>Size</span>
                <span className={styles.metaValue}>{formatSize(meta.fileSize)}</span>
              </div>
              <div className={styles.metaItem}>
                <span className={styles.metaLabel}>Type</span>
                <span className={styles.metaValue}>{meta.contentType}</span>
              </div>
              <div className={styles.metaItem}>
                <span className={styles.metaLabel}>Uploaded</span>
                <span className={styles.metaValue}>{formatDate(meta.createdAt)}</span>
              </div>
              <div className={styles.metaItem}>
                <span className={styles.metaLabel}>Downloads</span>
                <span className={styles.metaValue}>{meta.downloadCount}</span>
              </div>
              {meta.expiresAt && (
                <div className={styles.metaItem}>
                  <span className={styles.metaLabel}>Expires</span>
                  <span className={styles.metaValue}>{formatDate(meta.expiresAt)}</span>
                </div>
              )}
              {!meta.expiresAt && (
                <div className={styles.metaItem}>
                  <span className={styles.metaLabel}>Expires</span>
                  <span className={styles.metaValue}>Never</span>
                </div>
              )}
            </div>

            <DownloadButton
              downloadUrl={getDownloadUrl(meta.shareId)}
              fileName={meta.fileName}
            />
          </div>
        )}

        {(status === 'expired' || status === 'notfound' || status === 'error') && (
          <div className={styles.card}>
            <div className={styles.errorIcon}>⚠</div>
            <h1 className={styles.errorTitle}>File Not Available</h1>
            <p className={styles.errorDesc}>
              {status === 'expired'
                ? 'This link has expired and the file is no longer available.'
                : status === 'notfound'
                ? 'The requested file does not exist or the link is invalid.'
                : 'Something went wrong. Please try again later.'}
            </p>
            <Link to="/" className={styles.homeBtn}>Go Home</Link>
          </div>
        )}
      </main>
      <Footer />
    </div>
  )
}
