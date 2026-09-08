import styles from './FilePreview.module.css'

interface Props {
  file: File
  onRemove: () => void
}

function formatSize(bytes: number): string {
  if (bytes < 1024) return `${bytes} B`
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`
  return `${(bytes / 1024 / 1024).toFixed(2)} MB`
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

export default function FilePreview({ file, onRemove }: Props) {
  const isImage = file.type.startsWith('image/')
  const previewUrl = isImage ? URL.createObjectURL(file) : null

  return (
    <div className={styles.card}>
      <div className={styles.thumb}>
        {previewUrl
          ? <img src={previewUrl} alt={file.name} className={styles.img} onLoad={() => previewUrl && URL.revokeObjectURL(previewUrl)} />
          : <span className={styles.icon}>{fileIcon(file.type)}</span>
        }
      </div>
      <div className={styles.info}>
        <span className={styles.name} title={file.name}>{file.name}</span>
        <span className={styles.meta}>{formatSize(file.size)}</span>
      </div>
      <button className={styles.remove} onClick={onRemove} aria-label="Remove file">✕</button>
    </div>
  )
}
