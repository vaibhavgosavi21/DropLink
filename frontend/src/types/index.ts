export interface UploadedFile {
  fileName: string;
  size: number;
  contentType: string;
  shareId: string;
  shareUrl: string;
}

export interface UploadResponse {
  success: boolean;
  message: string;
  file?: UploadedFile;
  files?: UploadedFile[];
}

export interface FileMetadata {
  fileName: string;
  contentType: string;
  fileSize: number;
  shareId: string;
  createdAt: string;
  expiresAt: string | null;
  downloadCount: number;
}

export interface ApiError {
  success: false;
  message: string;
  timestamp: string;
}

export type ExpirationOption = '1h' | '6h' | '24h' | '7d' | '30d' | 'never';
