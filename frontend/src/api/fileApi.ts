import axios from 'axios'
import type { UploadResponse, FileMetadata, ExpirationOption } from '../types'

const api = axios.create({ baseURL: '/api' })

export function uploadFile(
  file: File,
  expiresIn: ExpirationOption,
  onProgress: (pct: number) => void
): Promise<UploadResponse> {
  const form = new FormData()
  form.append('file', file)
  form.append('expiresIn', expiresIn)
  return api
    .post<UploadResponse>('/files/upload', form, {
      onUploadProgress: (e) => {
        if (e.total) onProgress(Math.round((e.loaded * 100) / e.total))
      },
    })
    .then((r) => r.data)
}

export function uploadFiles(
  files: File[],
  expiresIn: ExpirationOption,
  onProgress: (pct: number) => void
): Promise<UploadResponse> {
  const form = new FormData()
  files.forEach((f) => form.append('files', f))
  form.append('expiresIn', expiresIn)
  return api
    .post<UploadResponse>('/files/upload/multiple', form, {
      onUploadProgress: (e) => {
        if (e.total) onProgress(Math.round((e.loaded * 100) / e.total))
      },
    })
    .then((r) => r.data)
}

export function getFileMetadata(shareId: string): Promise<FileMetadata> {
  return api.get<FileMetadata>(`/files/share/${shareId}`).then((r) => r.data)
}

export function getDownloadUrl(shareId: string): string {
  return `/api/files/share/${shareId}/download`
}
