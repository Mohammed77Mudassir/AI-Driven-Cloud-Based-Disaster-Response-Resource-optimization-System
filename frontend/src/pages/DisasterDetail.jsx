import { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { disasterApi } from '../services/api'
import { useAuth } from '../context/AuthContext'
import Sidebar from '../components/Sidebar'
import Footer from '../components/Footer'
import PageHeader from '../components/PageHeader'
import SectionCard from '../components/SectionCard'
import StatusBadge from '../components/StatusBadge'
import toast from 'react-hot-toast'
import {
  Box, Card, CardContent, Typography, Chip, Button, Grid, Paper, TextField,
  Dialog, DialogTitle, DialogContent, DialogActions, CircularProgress, IconButton,
  Divider, Stepper, Step, StepLabel, Avatar, List, ListItem, ListItemAvatar,
  ListItemText, Alert, Tooltip, Select, MenuItem, FormControl, InputLabel, Menu, useTheme
} from '@mui/material'
import ArrowBackIcon from '@mui/icons-material/ArrowBack'
import CommentIcon from '@mui/icons-material/Comment'
import HistoryIcon from '@mui/icons-material/History'
import GroupsIcon from '@mui/icons-material/Groups'
import DeleteIcon from '@mui/icons-material/Delete'
import EditIcon from '@mui/icons-material/Edit'
import SendIcon from '@mui/icons-material/Send'
import AttachFileIcon from '@mui/icons-material/AttachFile'
import ImageIcon from '@mui/icons-material/Image'
import VideoFileIcon from '@mui/icons-material/VideoFile'
import PictureAsPdfIcon from '@mui/icons-material/PictureAsPdf'
import MyLocationIcon from '@mui/icons-material/MyLocation'
import MoreVertIcon from '@mui/icons-material/MoreVert'
import { STATUSES, STATUS_TRANSITIONS, statusLabel, statusColor } from '../constants/disaster'

export default function DisasterDetail() {
  const { id } = useParams()
  const navigate = useNavigate()
  const { user, hasPermission } = useAuth()
  const theme = useTheme()
  const [detail, setDetail] = useState(null)
  const [loading, setLoading] = useState(true)
  const [notFound, setNotFound] = useState(false)

  const [commentText, setCommentText] = useState('')
  const [submittingComment, setSubmittingComment] = useState(false)

  const [statusDialog, setStatusDialog] = useState(null)
  const [statusComment, setStatusComment] = useState('')
  const [submittingStatus, setSubmittingStatus] = useState(false)

  const [editOpen, setEditOpen] = useState(false)
  const [editForm, setEditForm] = useState({})
  const [savingEdit, setSavingEdit] = useState(false)

  const [imageViewer, setImageViewer] = useState(null)

  const canUpdate = hasPermission('DISASTER_UPDATE')
  const canDelete = hasPermission('DISASTER_DELETE')

  const load = async () => {
    setLoading(true)
    setNotFound(false)
    try {
      const res = await disasterApi.getDetail(id)
      setDetail(res.data)
    } catch (err) {
      if (err.response?.status === 404) setNotFound(true)
      else toast.error('Failed to load disaster details')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { load() }, [id])

  const handleStatusChange = async () => {
    setSubmittingStatus(true)
    try {
      await disasterApi.updateStatus(id, {
        status: statusDialog,
        comment: statusComment.trim() || undefined,
      })
      toast.success(`Status updated to ${statusLabel[statusDialog]}`)
      setStatusDialog(null)
      setStatusComment('')
      load()
    } catch (err) {
      toast.error(err.response?.data?.error || 'Failed to update status')
    } finally {
      setSubmittingStatus(false)
    }
  }

  const handlePriorityChange = async (priority) => {
    if (!priority || priority === detail.priority) return
    try {
      await disasterApi.updatePriority(id, priority)
      toast.success('Priority updated')
      load()
    } catch {
      toast.error('Failed to update priority')
    }
  }

  const handleAddComment = async () => {
    if (!commentText.trim()) return
    setSubmittingComment(true)
    try {
      await disasterApi.addComment(id, { text: commentText.trim() })
      setCommentText('')
      toast.success('Comment added')
      load()
    } catch {
      toast.error('Failed to add comment')
    } finally {
      setSubmittingComment(false)
    }
  }

  const handleDeleteComment = async (commentId) => {
    try {
      await disasterApi.deleteComment(commentId)
      toast.success('Comment deleted')
      load()
    } catch {
      toast.error('Failed to delete comment')
    }
  }

  const handleDeleteDisaster = async () => {
    if (!window.confirm('Delete this disaster permanently?')) return
    try {
      await disasterApi.delete(id)
      toast.success('Disaster deleted')
      navigate('/disasters')
    } catch {
      toast.error('Failed to delete disaster')
    }
  }

  const openEdit = () => {
    setEditForm({
      disasterType: detail.disasterType,
      severity: detail.severity,
      priority: detail.priority,
      location: detail.location,
      address: detail.address || '',
      description: detail.description,
      latitude: detail.latitude,
      longitude: detail.longitude,
    })
    setEditOpen(true)
  }

  const handleSaveEdit = async () => {
    setSavingEdit(true)
    try {
      await disasterApi.update(id, {
        ...editForm,
        latitude: parseFloat(editForm.latitude) || 0,
        longitude: parseFloat(editForm.longitude) || 0,
      })
      toast.success('Disaster updated')
      setEditOpen(false)
      load()
    } catch (err) {
      const data = err.response?.data
      toast.error(data && typeof data === 'object'
        ? Object.values(data).filter(Boolean).join('. ')
        : 'Failed to update disaster')
    } finally {
      setSavingEdit(false)
    }
  }

  if (loading) return (
    <Box sx={{ display: 'flex' }}>
      <Sidebar />
      <Box sx={{ flex: 1, p: { xs: 2, md: 3 }, backgroundColor: 'background.default', minHeight: '100vh', display: 'flex', justifyContent: 'center', alignItems: 'center' }}>
        <CircularProgress />
      </Box>
    </Box>
  )

  if (notFound) return (
    <Box sx={{ display: 'flex' }}>
      <Sidebar />
      <Box component="main" sx={{ flex: 1, p: { xs: 2, md: 3 }, backgroundColor: 'background.default', minHeight: '100vh' }}>
        <Alert severity="warning" sx={{ mt: 4 }}>Disaster not found or you do not have access to it.</Alert>
        <Button startIcon={<ArrowBackIcon />} onClick={() => navigate('/disasters')} sx={{ mt: 2 }}>Back to reports</Button>
      </Box>
    </Box>
  )

  const timelineEntries = detail.timeline
  const currentIndex = STATUSES.indexOf(detail.status)
  const nextTransitions = STATUS_TRANSITIONS[detail.status] || []

  return (
    <Box sx={{ display: 'flex' }}>
      <Sidebar />
      <Box component="main" sx={{ flex: 1, p: { xs: 2, md: 3 }, backgroundColor: 'background.default', minHeight: '100vh' }}>
        <Box sx={{ mb: 1, display: 'flex', alignItems: 'center' }}>
          <IconButton onClick={() => navigate('/disasters')} size="small" aria-label="Back to reports"><ArrowBackIcon /></IconButton>
        </Box>
        <PageHeader
          title={`Disaster #${detail.id}`}
          subtitle={detail.reportId ? `Report ID: ${detail.reportId}` : detail.location}
          actions={
            <>
              {canUpdate && (
                <Button startIcon={<EditIcon />} size="small" variant="outlined" onClick={openEdit}>Edit</Button>
              )}
              {canDelete && (
                <Button startIcon={<DeleteIcon />} size="small" color="error" variant="outlined" onClick={handleDeleteDisaster}>
                  Delete
                </Button>
              )}
            </>
          }
        />

        <Grid container spacing={3}>
          {/* Left column */}
          <Grid size={{ xs: 12, lg: 8 }}>
            <Card sx={{ mb: 3 }}>
              <CardContent>
                <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1, mb: 2 }}>
                  <Chip label={detail.disasterType} color="primary" />
                  <StatusBadge status={detail.severity} />
                  <StatusBadge status={detail.status} />
                  <StatusBadge status={detail.priority} />
                  {detail.source && <Chip label={`Source: ${detail.source}`} variant="outlined" size="small" />}
                </Box>

                <Typography variant="body1" sx={{ mb: 1 }}>{detail.description}</Typography>

                <Divider sx={{ my: 2 }} />

                <Typography variant="subtitle2" fontWeight={700} color="text.secondary" sx={{ mb: 1.5 }}>
                  Location & Coordinates
                </Typography>
                <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 3 }}>
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
                    <MyLocationIcon fontSize="small" color="primary" />
                    <Typography variant="body2">{detail.location}{detail.address && detail.address !== detail.location ? ` · ${detail.address}` : ''}</Typography>
                  </Box>
                  <Typography variant="body2" color="text.secondary">
                    {detail.latitude?.toFixed?.(4) ?? detail.latitude}, {detail.longitude?.toFixed?.(4) ?? detail.longitude}
                  </Typography>
                </Box>

                <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 3, mt: 1 }}>
                  <Typography variant="caption" color="text.secondary">
                    Reported by: <b>{detail.reportedBy || 'Anonymous'}</b>
                  </Typography>
                  <Typography variant="caption" color="text.secondary">
                    Reported: <b>{new Date(detail.createdAt || detail.date).toLocaleString()}</b>
                  </Typography>
                </Box>

                {detail.reporterName && (
                  <Alert severity="info" sx={{ mt: 2 }}>
                    Citizen reporter: {detail.reporterName}
                    {detail.reporterMobile && ` · ${detail.reporterMobile}`}
                    {detail.reporterEmail && ` · ${detail.reporterEmail}`}
                  </Alert>
                )}
              </CardContent>
            </Card>

            <SectionCard title="Evidence Attachments" icon={AttachFileIcon} sx={{ mb: 3 }}>
              {detail.attachments.length === 0 ? (
                <Typography variant="body2" color="text.secondary">No attachments uploaded.</Typography>
              ) : (
                <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 2 }}>
                  {detail.attachments.map((att) => (
                    <Box key={att.id}>
                      {att.category === 'IMAGE' && (
                        <Tooltip title={att.filename}>
                          <Box
                            component="img"
                            src={att.dataUrl}
                            alt={att.filename}
                            onClick={() => setImageViewer(att)}
                            sx={{ width: 140, height: 140, objectFit: 'cover', borderRadius: 2, cursor: 'zoom-in', border: `1px solid ${theme.palette.divider}` }}
                          />
                        </Tooltip>
                      )}
                      {att.category === 'VIDEO' && (
                        <Box sx={{ width: 260 }}>
                          <video controls preload="metadata" src={att.dataUrl} style={{ width: '100%', borderRadius: 8 }} />
                          <Typography variant="caption" color="text.secondary">{att.filename}</Typography>
                        </Box>
                      )}
                      {att.category === 'PDF' && (
                        <Box
                          component="a"
                          href={att.dataUrl}
                          target="_blank"
                          rel="noreferrer"
                          sx={{
                            display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 0.5,
                            width: 140, p: 2, borderRadius: 2,
                            border: `1px solid ${theme.palette.error.main}4D`,
                            backgroundColor: `${theme.palette.error.main}0A`,
                            textDecoration: 'none', color: 'inherit',
                          }}
                        >
                          <PictureAsPdfIcon sx={{ fontSize: 44, color: theme.palette.error.main }} />
                          <Typography variant="caption" sx={{ textAlign: 'center', wordBreak: 'break-all' }}>{att.filename}</Typography>
                        </Box>
                      )}
                    </Box>
                  ))}
                </Box>
              )}
            </SectionCard>

            <SectionCard title="Status Timeline" icon={HistoryIcon}>
              <Stepper activeStep={Math.max(currentIndex, 0)} alternativeLabel orientation="horizontal" sx={{ flexWrap: 'wrap', '& .MuiStep-root': { '&:last-of-type': { minWidth: 0 } } }}>
                {STATUSES.map((s) => (
                  <Step key={s}>
                    <StepLabel>
                      <Typography variant="caption" sx={{ fontSize: '0.65rem' }}>{statusLabel[s]}</Typography>
                    </StepLabel>
                  </Step>
                ))}
              </Stepper>
              <Divider sx={{ my: 2 }} />
              <List dense>
                {timelineEntries.length === 0 && (
                  <Typography variant="body2" color="text.secondary">No timeline entries yet.</Typography>
                )}
                {[...timelineEntries].reverse().map((entry, i) => (
                  <ListItem key={entry.id || i} alignItems="flex-start" sx={{ px: 0 }}>
                    <ListItemAvatar>
                      <Avatar sx={{ width: 32, height: 32, bgcolor: entry.toStatus ? `${statusColor[entry.toStatus]}30` : theme.palette.action.hover, color: entry.toStatus ? statusColor[entry.toStatus] : theme.palette.text.secondary }}>
                        {i + 1}
                      </Avatar>
                    </ListItemAvatar>
                    <ListItemText
                      primary={
                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, flexWrap: 'wrap' }}>
                          <Typography variant="body2" fontWeight={600}>
                            {entry.toStatus ? statusLabel[entry.toStatus] : 'Reported'}
                          </Typography>
                          {entry.fromStatus && entry.toStatus && entry.fromStatus !== entry.toStatus && (
                            <Typography variant="caption" color="text.secondary">from {statusLabel[entry.fromStatus] || entry.fromStatus}</Typography>
                          )}
                          <Chip label={new Date(entry.changedAt).toLocaleString()} size="small" sx={{ fontSize: '0.6rem', height: 20 }} />
                        </Box>
                      }
                      secondary={entry.comment}
                      slotProps={{ secondary: { sx: { mt: 0.5 } } }}
                    />
                    <Typography variant="caption" color="text.secondary" sx={{ alignSelf: 'center', whiteSpace: 'nowrap' }}>
                      {entry.changedBy}
                    </Typography>
                  </ListItem>
                ))}
              </List>
            </SectionCard>
          </Grid>

          {/* Right column */}
          <Grid size={{ xs: 12, lg: 4 }}>
            <SectionCard title="Workflow" sx={{ mb: 3 }}>
              <Typography variant="subtitle2" color="text.secondary" sx={{ mb: 1 }}>Priority</Typography>
              {canUpdate ? (
                <FormControl fullWidth size="small">
                  <InputLabel>Priority</InputLabel>
                  <Select
                    value={detail.priority}
                    onChange={(e) => handlePriorityChange(e.target.value)}
                    label="Priority"
                  >
                    {['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'].map(p => (
                      <MenuItem key={p} value={p}>{p.charAt(0) + p.slice(1).toLowerCase()}</MenuItem>
                    ))}
                  </Select>
                </FormControl>
              ) : (
                <StatusBadge status={detail.priority} />
              )}

              <Divider sx={{ my: 2 }} />

              <Typography variant="subtitle2" color="text.secondary" sx={{ mb: 1 }}>
                Update Status (current: {statusLabel[detail.status]})
              </Typography>
              {nextTransitions.length === 0 ? (
                <Alert severity="success">This report is resolved.</Alert>
              ) : canUpdate ? (
                <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1 }}>
                  {nextTransitions.map(s => (
                    <Button key={s} size="small" variant="contained" onClick={() => setStatusDialog(s)}>
                      {statusLabel[s]}
                    </Button>
                  ))}
                </Box>
              ) : (
                <Alert severity="info" sx={{ fontSize: '0.8rem' }}>You have read-only access to status updates.</Alert>
              )}
            </SectionCard>

            <SectionCard title="Assignment History" icon={GroupsIcon} sx={{ mb: 3 }}>
              {detail.assignments.length === 0 ? (
                <Typography variant="body2" color="text.secondary">No rescue teams assigned yet.</Typography>
              ) : (
                <List dense>
                  {detail.assignments.map(a => (
                    <ListItem key={a.id} sx={{ px: 0 }}>
                      <ListItemText
                        primary={
                          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                            <Typography variant="body2" fontWeight={600}>{a.teamName}</Typography>
                            <Chip label={a.action} size="small" color={a.action === 'RELEASED' ? 'default' : 'primary'} sx={{ fontSize: '0.6rem', height: 20 }} />
                          </Box>
                        }
                        secondary={`By ${a.assignedBy} · ${new Date(a.assignedAt).toLocaleString()}`}
                      />
                    </ListItem>
                  ))}
                </List>
              )}
            </SectionCard>

            <SectionCard title={`Comments (${detail.comments.length})`} icon={CommentIcon}>
              <Box sx={{ display: 'flex', gap: 1, mb: 2 }}>
                <TextField
                  size="small" fullWidth multiline minRows={2} placeholder="Add an update for other responders..."
                  value={commentText}
                  onChange={e => setCommentText(e.target.value)}
                />
                <Button
                  variant="contained" size="small" disabled={!commentText.trim() || submittingComment}
                  onClick={handleAddComment} sx={{ alignSelf: 'flex-end' }}
                >
                  {submittingComment ? <CircularProgress size={16} color="inherit" /> : <SendIcon />}
                </Button>
              </Box>
              <List dense>
                {detail.comments.length === 0 && (
                  <Typography variant="body2" color="text.secondary">No comments yet.</Typography>
                )}
                {detail.comments.map(c => (
                  <ListItem key={c.id} alignItems="flex-start" sx={{ px: 0 }}>
                    <ListItemAvatar>
                      <Avatar sx={{ width: 32, height: 32, bgcolor: theme.palette.primary.main, fontSize: '0.8rem' }}>
                        {c.author?.[0]?.toUpperCase()}
                      </Avatar>
                    </ListItemAvatar>
                    <ListItemText
                      primary={
                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                          <Typography variant="body2" fontWeight={600}>{c.author}</Typography>
                          <Typography variant="caption" color="text.secondary">{new Date(c.createdAt).toLocaleString()}</Typography>
                          <Box sx={{ flex: 1 }} />
                          {(c.authorId === user?.id || user?.role === 'ADMIN') && (
                            <IconButton size="small" onClick={() => handleDeleteComment(c.id)}>
                              <DeleteIcon sx={{ fontSize: 16, color: theme.palette.error.main }} />
                            </IconButton>
                          )}
                        </Box>
                      }
                      secondary={c.text}
                    />
                  </ListItem>
                ))}
              </List>
            </SectionCard>
          </Grid>
        </Grid>

        {/* Status transition dialog */}
        <Dialog open={!!statusDialog} onClose={() => setStatusDialog(null)} maxWidth="xs" fullWidth>
          <DialogTitle>Update status to {statusDialog ? statusLabel[statusDialog] : ''}?</DialogTitle>
          <DialogContent>
            <TextField
              fullWidth multiline minRows={3} size="small" sx={{ mt: 1 }}
              label="Comment (optional)"
              placeholder="Reason / note for this status change"
              value={statusComment}
              onChange={e => setStatusComment(e.target.value)}
            />
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setStatusDialog(null)}>Cancel</Button>
            <Button variant="contained" onClick={handleStatusChange} disabled={submittingStatus}>
              {submittingStatus ? <CircularProgress size={18} color="inherit" /> : 'Confirm Update'}
            </Button>
          </DialogActions>
        </Dialog>

        {/* Edit dialog */}
        <Dialog open={editOpen} onClose={() => setEditOpen(false)} maxWidth="sm" fullWidth>
          <DialogTitle>Edit Disaster</DialogTitle>
          <DialogContent>
            <Box component="form" sx={{ display: 'flex', flexDirection: 'column', gap: 2, mt: 1 }}>
              <TextField label="Disaster Type" size="small" required
                value={editForm.disasterType}
                onChange={e => setEditForm({ ...editForm, disasterType: e.target.value })} />
              <Box sx={{ display: 'flex', gap: 2 }}>
                <FormControl fullWidth size="small">
                  <InputLabel>Severity</InputLabel>
                  <Select label="Severity" value={editForm.severity}
                    onChange={e => setEditForm({ ...editForm, severity: e.target.value })}>
                    {['Low', 'Medium', 'High', 'Critical'].map(s => <MenuItem key={s} value={s}>{s}</MenuItem>)}
                  </Select>
                </FormControl>
                <FormControl fullWidth size="small">
                  <InputLabel>Priority</InputLabel>
                  <Select label="Priority" value={editForm.priority}
                    onChange={e => setEditForm({ ...editForm, priority: e.target.value })}>
                    {['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'].map(p => <MenuItem key={p} value={p}>{p.charAt(0) + p.slice(1).toLowerCase()}</MenuItem>)}
                  </Select>
                </FormControl>
              </Box>
              <TextField label="Location" size="small" required
                value={editForm.location}
                onChange={e => setEditForm({ ...editForm, location: e.target.value })} />
              <TextField label="Address (optional)" size="small"
                value={editForm.address}
                onChange={e => setEditForm({ ...editForm, address: e.target.value })} />
              <TextField label="Description" size="small" required multiline minRows={3}
                value={editForm.description}
                onChange={e => setEditForm({ ...editForm, description: e.target.value })} />
              <Box sx={{ display: 'flex', gap: 2 }}>
                <TextField label="Latitude" size="small" type="number" fullWidth
                  value={editForm.latitude}
                  onChange={e => setEditForm({ ...editForm, latitude: e.target.value })} />
                <TextField label="Longitude" size="small" type="number" fullWidth
                  value={editForm.longitude}
                  onChange={e => setEditForm({ ...editForm, longitude: e.target.value })} />
              </Box>
            </Box>
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setEditOpen(false)}>Cancel</Button>
            <Button variant="contained" onClick={handleSaveEdit} disabled={savingEdit}>
              {savingEdit ? <CircularProgress size={18} color="inherit" /> : 'Save Changes'}
            </Button>
          </DialogActions>
        </Dialog>

        {/* Image viewer */}
        <Dialog open={!!imageViewer} onClose={() => setImageViewer(null)} maxWidth="md" fullWidth>
          <DialogContent sx={{ p: 0, backgroundColor: theme.palette.mode === 'dark' ? '#000' : '#111' }}>
            {imageViewer && (
              <Box component="img" src={imageViewer.dataUrl} alt={imageViewer.filename}
                sx={{ width: '100%', maxHeight: '75vh', objectFit: 'contain' }} />
            )}
          </DialogContent>
        </Dialog>

        <Footer />
      </Box>
    </Box>
  )
}
