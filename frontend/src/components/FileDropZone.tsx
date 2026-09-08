import { useRef, useState } from 'react'
import styles from './FileDropZone.module.css'

interface Props {
  onFiles: (files: File[]) => void
  multiple?: boolean
}

export default function FileDropZone({ onFiles, multiple = true }: Props) {
  const [dragging, setDragging] = useState(false)
  const inputRef = useRef<HTMLInputElement>(null)

  const handle = (files: FileList | null) => {
    if (!files || files.length === 0) return
    onFiles(Array.from(files))
  }

  return (
    <div
      className={`${styles.zone} ${dragging ? styles.dragging : ''}`}
      onClick={() => inputRef.current?.click()}
      onDragOver={(e) => { e.preventDefault(); setDragging(true) }}
      onDragLeave={() => setDragging(false)}
      onDrop={(e) => { e.preventDefault(); setDragging(false); handle(e.dataTransfer.files) }}
      role="button"
      tabIndex={0}
      onKeyDown={(e) => e.key === 'Enter' && inputRef.current?.click()}
      aria-label="File drop zone"
    >
      <div className={styles.icon}>📂</div>
      <p className={styles.primary}>Drag &amp; Drop your file{multiple ? 's' : ''} here</p>
      <p className={styles.secondary}>or</p>
      <span className={styles.browse}>Browse Files</span>
      <p className={styles.hint}>Images, Videos, PDF, DOCX, XLSX, ZIP, TXT and more</p>
      <input
        ref={inputRef}
        type="file"
        multiple={multiple}
        className={styles.hidden}
        onChange={(e) => handle(e.target.files)}
        accept=".jpg,.jpeg,.png,.gif,.webp,.bmp,.mp4,.mov,.avi,.mkv,.webm,.pdf,.doc,.docx,.xls,.xlsx,.zip,.tar,.gz,.rar,.txt,.csv,.json,.xml,.md"
      />
    </div>
  )
}
