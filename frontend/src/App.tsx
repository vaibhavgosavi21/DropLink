import { BrowserRouter, Routes, Route } from 'react-router-dom'
import UploadPage from './pages/UploadPage'
import SharePage from './pages/SharePage'

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<UploadPage />} />
        <Route path="/share/:shareId" element={<SharePage />} />
      </Routes>
    </BrowserRouter>
  )
}
