import styles from './ErrorMessage.module.css'

interface Props {
  message: string
}

export default function ErrorMessage({ message }: Props) {
  return (
    <div className={styles.error} role="alert">
      <span className={styles.icon}>⚠</span>
      {message}
    </div>
  )
}
