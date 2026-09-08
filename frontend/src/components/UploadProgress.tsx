import styles from './UploadProgress.module.css'

interface Props {
  progress: number
}

export default function UploadProgress({ progress }: Props) {
  return (
    <div className={styles.wrapper}>
      <div className={styles.label}>
        <span>Uploading…</span>
        <span>{progress}%</span>
      </div>
      <div className={styles.track}>
        <div className={styles.bar} style={{ width: `${progress}%` }} />
      </div>
    </div>
  )
}
