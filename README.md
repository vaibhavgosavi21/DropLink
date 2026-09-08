# DropLink — Secure File Sharing

Upload files and share them instantly via a secure link. No account needed.

---

## Architecture

```
User → React Frontend (Vite + nginx)
              ↓ /api/*
      Spring Boot Backend
              ↓
          PostgreSQL
              ↓
   Local Storage / Amazon S3
```

---

## Technology Stack

| Layer     | Technology |
|-----------|------------|
| Backend   | Java 21, Spring Boot 3.4, Spring Security, Spring Data JPA |
| Database  | PostgreSQL 16 |
| Storage   | Local filesystem (dev) / Amazon S3 (prod) |
| Frontend  | React 19, TypeScript, Vite, CSS Modules |
| Container | Docker, Docker Compose |

---

## Quick Start with Docker

```bash
cd DropLink
cp .env.example .env
# Edit .env — set DB_PASSWORD at minimum
docker compose up --build
```

| Service  | URL |
|----------|-----|
| Frontend | http://localhost |
| Backend  | http://localhost:8080 |
| Health   | http://localhost:8080/api/health |

---

## Running Locally (without Docker)

### 1. Database
```sql
CREATE DATABASE fileshare;
```

### 2. Backend
```bash
cd backend

# Windows
powershell -Command "& { $env:JAVA_HOME='C:\Program Files\Java\jdk-21'; $env:DB_URL='jdbc:postgresql://localhost:5432/fileshare'; $env:DB_USERNAME='postgres'; $env:DB_PASSWORD='postgres'; & '.\mvnw.cmd' spring-boot:run }"

# macOS / Linux
DB_URL=jdbc:postgresql://localhost:5432/fileshare \
DB_USERNAME=postgres DB_PASSWORD=postgres \
./mvnw spring-boot:run
```

### 3. Frontend
```bash
cd frontend
npm install
npm run dev   # → http://localhost:5173
```

---

## Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `DB_URL` | `jdbc:postgresql://localhost:5432/fileshare` | PostgreSQL JDBC URL |
| `DB_USERNAME` | `postgres` | DB username |
| `DB_PASSWORD` | `postgres` | DB password |
| `APP_BASE_URL` | `http://localhost:5173` | Base URL for share links |
| `FILE_MAX_SIZE` | `500MB` | Max upload size |
| `AWS_ACCESS_KEY_ID` | — | S3 (prod only) |
| `AWS_SECRET_ACCESS_KEY` | — | S3 (prod only) |
| `AWS_REGION` | `us-east-1` | S3 region |
| `S3_BUCKET_NAME` | — | S3 bucket name |

---

## API Reference

### Upload single file
```
POST /api/files/upload
Content-Type: multipart/form-data

file=<file>
expiresIn=never|1h|6h|24h|7d|30d
```

**Response 201:**
```json
{
  "success": true,
  "message": "File uploaded successfully",
  "file": {
    "fileName": "photo.jpg",
    "size": 245678,
    "contentType": "image/jpeg",
    "shareId": "Ab72Xk9LmPqRsTuVwXyZ12",
    "shareUrl": "http://localhost/share/Ab72Xk9LmPqRsTuVwXyZ12"
  }
}
```

### Upload multiple files
```
POST /api/files/upload/multiple
files=<file1>&files=<file2>&expiresIn=never
```

### Get file metadata
```
GET /api/files/share/{shareId}
```

### Download file
```
GET /api/files/share/{shareId}/download
```

### Delete file
```
DELETE /api/files/{id}
```

### Health check
```
GET /api/health
```

---

## Running Tests

```bash
cd backend

# Windows
powershell -Command "& { $env:JAVA_HOME='C:\Program Files\Java\jdk-21'; & '.\mvnw.cmd' test }"

# macOS / Linux
./mvnw test
```

**73 tests** — unit, slice, and full integration.

---

## Supported File Types

Images (jpg, png, gif, webp), Videos (mp4, mov, avi, mkv), PDF, DOC/DOCX, XLS/XLSX, ZIP/TAR/GZ/RAR, TXT, CSV, JSON, XML, MD

Blocked: `.exe .bat .sh .ps1 .php .py .rb .js .ts .dll` and more.

---

## Security

- Cryptographically random share IDs (SecureRandom, 128-bit)
- Filename sanitization — strips path components and unsafe characters
- Path traversal protection on all storage operations
- Share ID format validation on all endpoints
- File extension allowlist + blocklist
- Configurable file size limit
- CORS restricted to configured origin
- No internal paths exposed in API responses
- Soft-delete — files deactivated before purge
- Hourly scheduled cleanup of expired files

---

## Storage Configuration

### Development (local)
```properties
file.storage.type=local
file.storage.local.upload-dir=storage/uploads
```

### Production (S3)
```
STORAGE_TYPE=s3
AWS_ACCESS_KEY_ID=...
AWS_SECRET_ACCESS_KEY=...
AWS_REGION=us-east-1
S3_BUCKET_NAME=my-droplink-bucket
```

`S3FileStorageService` stub is ready in `service/storage/` — wire in the AWS SDK and activate with `file.storage.type=s3`.

---

## Production Deployment

1. Set `SPRING_PROFILES_ACTIVE=prod`
2. Configure all environment variables
3. Set `APP_BASE_URL` to your real domain
4. Switch storage to S3
5. Use CloudFront or nginx in front of the frontend
6. Use managed PostgreSQL (RDS, Supabase, etc.)

---

## Future Improvements

- S3 presigned upload/download URLs
- Password-protected links
- File preview in browser (images, PDFs)
- Admin dashboard
- Rate limiting per IP
- Virus scanning
- Email notifications
