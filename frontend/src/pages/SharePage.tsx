import { useParams } from 'react-router-dom'

export default function SharePage() {
  const { shareId } = useParams()
  return (
    <div style={{ textAlign: 'center', marginTop: '4rem', fontFamily: 'sans-serif' }}>
      <h1>Share Page</h1>
      <p>Share ID: {shareId}</p>
      <p>Phase 1 — Share page coming in Phase 10.</p>
    </div>
  )
}
