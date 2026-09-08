import styles from './DownloadButton.module.css'

interface Props {
  downloadUrl: string
  fileName: string
}

export default function DownloadButton({ downloadUrl, fileName }: Props) {
  return (
    <a href={downloadUrl} download={fileName} className={styles.btn}>
      ⬇ Download File
    </a>
  )
}
