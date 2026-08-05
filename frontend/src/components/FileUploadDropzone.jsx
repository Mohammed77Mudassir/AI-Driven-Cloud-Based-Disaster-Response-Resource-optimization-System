import { useRef, useState } from 'react'
import {
  Box, Typography, Chip, IconButton, Tooltip, CircularProgress
} from '@mui/material'
import CloudUploadIcon from '@mui/icons-material/CloudUpload'
import ImageIcon from '@mui/icons-material/Image'
import VideoFileIcon from '@mui/icons-material/VideoFile'
import PictureAsPdfIcon from '@mui/icons-material/PictureAsPdf'
import InsertDriveFileIcon from '@mui/icons-material/InsertDriveFile'
import DeleteIcon from '@mui/icons-material/Delete'
import CloseIcon from '@mui/icons-material/Close'

const ACCEPTED_IMAGES = ['image/jpeg', 'image/png', 'image/gif', 'image/webp', 'image/heic']
const ACCEPTED_VIDEOS = ['video/mp4', 'video/webm', 'video/ogg', 'video/quicktime']
const ACCEPTED_PDFS = ['application/pdf']
const ALL_ACCEPTED = [...ACCEPTED_IMAGES, ...ACCEPTED_VIDEOS, ...ACCEPTED_PDFS]

export function categorizeFile(file) {
  if (ACCEPTED_IMAGES.includes(file.type)) return 'IMAGE'
  if (ACCEPTED_VIDEOS.includes(file.type)) return 'VIDEO'
  if (ACCEPTED_PDFS.includes(file.type) || file.name.toLowerCase().endsWith('.pdf')) return 'PDF'
  return 'IMAGE'
}

export const categoryMeta = {
  IMAGE: { icon: <ImageIcon />, color: '#0F4C81', label: 'Image' },
  VIDEO: { icon: <VideoFileIcon />, color: '#00897B', label: 'Video' },
  PDF: { icon: <PictureAsPdfIcon />, color: '#C62828', label: 'PDF' },
}

const DEFAULT_MAX_MB = 20
const MB = 1024 * 1024

function readFileAsDataURL(file) {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = () => resolve(reader.result)
    reader.onerror = () => reject(reader.error)
    reader.readAsDataURL(file)
  })
}

function formatBytes(bytes) {
  if (!bytes) return '0 B'
  const units = ['B', 'KB', 'MB', 'GB']
  let i = 0
  let value = bytes
  while (value >= 1024 && i < units.length - 1) { value /= 1024; i++ }
  return `${value.toFixed(value >= 10 || i === 0 ? 0 : 1)} ${units[i]}`
}

/**
 * Drag-and-drop file upload supporting multiple images, videos and PDFs.
 * Emits attachment objects compatible with the backend AttachmentRequest DTO:
 * `{ id, category, filename, mimeType, size, dataUrl }`.
 */
export default function FileUploadDropzone({
  value = [],
  onChange,
  disabled = false,
  maxFiles = 10,
  maxSizeMB = DEFAULT_MAX_MB,
}) {
  const inputRef = useRef(null)
  const [dragging, setDragging] = useState(false)
  const [error, setError] = useState('')
  const [processing, setProcessing] = useState(false)

  const handleFiles = async (fileList) => {
    const files = Array.from(fileList)
    if (files.length === 0) return
    setError('')
    setProcessing(true)

    const next = [...value]
    for (const file of files) {
      if (next.length >= maxFiles) {
        setError(`Maximum of ${maxFiles} files allowed`)
        break
      }
      if (!ALL_ACCEPTED.includes(file.type)) {
        setError(`Unsupported file type: ${file.name}`)
        continue
      }
      if (file.size > maxSizeMB * MB) {
        setError(`${file.name} exceeds the ${maxSizeMB} MB size limit`)
        continue
      }
      try {
        const dataUrl = await readFileAsDataURL(file)
        next.push({
          id: `${Date.now()}-${Math.random().toString(36).slice(2, 8)}`,
          category: categorizeFile(file),
          filename: file.name,
          mimeType: file.type,
          size: file.size,
          dataUrl,
        })
      } catch {
        setError(`Failed to read ${file.name}`)
      }
    }
    onChange(next)
    setProcessing(false)
  }

  const removeFile = (id) => {
    onChange(value.filter(f => f.id !== id))
  }

  return (
    <Box>
      <Box
        onDragOver={(e) => { e.preventDefault(); if (!disabled) setDragging(true) }}
        onDragLeave={() => setDragging(false)}
        onDrop={(e) => {
          e.preventDefault()
          setDragging(false)
          if (!disabled) handleFiles(e.dataTransfer.files)
        }}
        onClick={() => { if (!disabled && !processing) inputRef.current?.click() }}
        sx={{
          border: '2px dashed',
          borderColor: dragging ? '#0F4C81' : 'rgba(15,76,129,0.35)',
          borderRadius: 3,
          p: 3,
          textAlign: 'center',
          cursor: disabled ? 'not-allowed' : 'pointer',
          backgroundColor: dragging ? 'rgba(15,76,129,0.06)' : 'rgba(15,76,129,0.02)',
          transition: 'all 0.2s ease',
          '&:hover': { borderColor: '#0F4C81', backgroundColor: 'rgba(15,76,129,0.05)' },
        }}
      >
        <input
          ref={inputRef}
          type="file"
          multiple
          accept={ALL_ACCEPTED.join(',')}
          hidden
          disabled={disabled || processing}
          onChange={(e) => { handleFiles(e.target.files); e.target.value = '' }}
        />
        <CloudUploadIcon sx={{ fontSize: 42, color: '#0F4C81', mb: 1, opacity: 0.85 }} />
        <Typography variant="body2" fontWeight={600} color="#0F4C81">
          {processing ? 'Reading files...' : 'Drag & drop files here, or click to browse'}
        </Typography>
        <Typography variant="caption" color="text.secondary" sx={{ display: 'block', mt: 0.5 }}>
          Images (JPG, PNG, GIF, WebP) · Videos (MP4, WebM) · PDF documents · Max {maxSizeMB} MB each
        </Typography>
        {processing && <CircularProgress size={18} sx={{ mt: 1, color: '#0F4C81' }} />}
      </Box>

      {error && (
        <Typography variant="caption" color="error" sx={{ display: 'block', mt: 0.5 }}>
          {error}
        </Typography>
      )}

      {value.length > 0 && (
        <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1.5, mt: 2 }}>
          {value.map((file) => (
            <Box key={file.id} sx={{ position: 'relative', width: 96, height: 96 }}>
              {file.category === 'IMAGE' ? (
                <Box
                  component="img"
                  src={file.dataUrl}
                  alt={file.filename}
                  sx={{ width: '100%', height: '100%', objectFit: 'cover', borderRadius: 2, border: '1px solid rgba(0,0,0,0.1)' }}
                />
              ) : (
                <Box sx={{
                  width: '100%', height: '100%', borderRadius: 2, border: '1px solid rgba(0,0,0,0.1)',
                  display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center',
                  backgroundColor: 'rgba(15,76,129,0.05)', gap: 0.5, p: 0.5, textAlign: 'center',
                }}>
                  {categoryMeta[file.category]?.icon || <InsertDriveFileIcon sx={{ fontSize: 32, color: '#636E72' }} />}
                  <Typography variant="caption" sx={{ fontSize: '0.6rem', color: '#636E72', wordBreak: 'break-all', lineHeight: 1.1 }}>
                    {file.filename}
                  </Typography>
                </Box>
              )}
              <Tooltip title="Remove file">
                <IconButton
                  size="small"
                  onClick={(e) => { e.stopPropagation(); removeFile(file.id) }}
                  sx={{
                    position: 'absolute', top: -6, right: -6,
                    bgcolor: '#C62828', color: '#fff', width: 20, height: 20,
                    '&:hover': { bgcolor: '#8E0000' },
                  }}
                >
                  <CloseIcon sx={{ fontSize: 14 }} />
                </IconButton>
              </Tooltip>
              <Chip
                icon={categoryMeta[file.category]?.icon}
                label={formatBytes(file.size)}
                size="small"
                sx={{
                  position: 'absolute', bottom: 4, left: '50%', transform: 'translateX(-50%)',
                  fontSize: '0.6rem', height: 20, bgcolor: 'rgba(0,0,0,0.65)', color: '#fff',
                  '& .MuiChip-icon': { color: '#fff', fontSize: 14, marginLeft: '4px' },
                }}
              />
            </Box>
          ))}
          {value.length >= maxFiles && (
            <Box sx={{ display: 'flex', alignItems: 'center' }}>
              <Tooltip title={`Remove files to add more (${maxFiles} max)`}>
                <DeleteIcon color="disabled" />
              </Tooltip>
            </Box>
          )}
        </Box>
      )}
    </Box>
  )
}
