import { useState, useRef } from 'react'
import { QRCodeSVG } from 'qrcode.react'
import styles from './ShareResult.module.css'

interface Props {
  fileName: string
  shareUrl: string
}

export default function ShareResult({ fileName, shareUrl }: Props) {
  const [copied, setCopied] = useState(false)
  const [showQr, setShowQr] = useState(false)
  const qrRef = useRef<HTMLDivElement>(null)

  const copy = async () => {
    await navigator.clipboard.writeText(shareUrl)
    setCopied(true)
    setTimeout(() => setCopied(false), 2000)
  }

  const downloadQr = () => {
    const svg = qrRef.current?.querySelector('svg')
    if (!svg) return
    const blob = new Blob([svg.outerHTML], { type: 'image/svg+xml' })
    const a = document.createElement('a')
    a.href = URL.createObjectURL(blob)
    a.download = `droplink-qr-${fileName}.svg`
    a.click()
    URL.revokeObjectURL(a.href)
  }

  return (
    <div className={styles.card}>
      <div className={styles.successBadge}>
        <span className={styles.check}>✓</span>
        Upload Successful
      </div>

      <p className={styles.fileName}>{fileName}</p>

      <div className={styles.urlRow}>
        <input readOnly value={shareUrl} className={styles.urlInput} onClick={(e) => (e.target as HTMLInputElement).select()} />
        <button className={`${styles.btn} ${copied ? styles.copied : ''}`} onClick={copy}>
          {copied ? '✓ Copied' : 'Copy'}
        </button>
      </div>

      <div className={styles.actions}>
        <button className={styles.btnOutline} onClick={() => setShowQr((v) => !v)}>
          {showQr ? 'Hide QR' : '⬡ QR Code'}
        </button>
      </div>

      {showQr && (
        <div className={styles.qrSection}>
          <div ref={qrRef} className={styles.qrBox}>
            <QRCodeSVG value={shareUrl} size={180} bgColor="#1a1d27" fgColor="#e8eaf6" level="M" />
          </div>
          <button className={styles.btnOutline} onClick={downloadQr}>⬇ Download QR</button>
        </div>
      )}
    </div>
  )
}
