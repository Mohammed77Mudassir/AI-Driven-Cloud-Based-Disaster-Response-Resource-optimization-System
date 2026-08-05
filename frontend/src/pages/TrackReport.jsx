import { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { publicReportApi } from '../services/api'
import toast from 'react-hot-toast'
import {
  Box, Card, CardContent, TextField, Button, Typography, Chip, Stack, Alert,
  CircularProgress, InputAdornment, Divider, Grid
} from '@mui/material'
import SecurityIcon from '@mui/icons-material/Security'
import SearchIcon from '@mui/icons-material/Search'
import TrackChangesIcon from '@mui/icons-material/TrackChanges'
import ArrowBackIcon from '@mui/icons-material/ArrowBack'
import PendingIcon from '@mui/icons-material/HourglassEmpty'
import VerifiedIcon from '@mui/icons-material/VerifiedUser'
import AssignmentIcon from '@mui/icons-material/Assignment'
import LocalShippingIcon from '@mui/icons-material/LocalShipping'
import ConstructionIcon from '@mui/icons-material/Construction'
import CheckCircleIcon from '@mui/icons-material/CheckCircle'
import PictureAsPdfIcon from '@mui/icons-material/PictureAsPdf'
import PlayCircleIcon from '@mui/icons-material/PlayCircle'
import EmergencyIcon from '@mui/icons-material/Emergency'

const GRADIENT_CSS = `
@keyframes dmsGradientShift {
  0%   { background-position: 0% 50%; }
  50%  { background-position: 100% 50%; }
  100% { background-position: 0% 50%; }
}
.dms-animated-bg {
  background: linear-gradient(-45deg, #0A3560, #0F4C81, #1A6BB5, #00897B, #0F4C81, #0A3560);
  background-size: 400% 400%;
  animation: dmsGradientShift 18s ease infinite;
}
.dms-glass {
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(18px);
  -webkit-backdrop-filter: blur(18px);
  border: 1px solid rgba(255, 255, 255, 0.55);
  box-shadow: 0 20px 60px rgba(4, 28, 51, 0.45);
}
.dms-grid-overlay {
  background-image:
    linear-gradient(rgba(255,255,255,0.06) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255,255,255,0.06) 1px, transparent 1px);
  background-size: 44px 44px;
}
`

const STATUS_META = {
  PENDING: { label: 'Reported', color: '#ffd93d', icon: <PendingIcon /> },
  VERIFIED: { label: 'Verified', color: '#4d96ff', icon: <VerifiedIcon /> },
  ASSIGNED: { label: 'Assigned', color: '#4d96ff', icon: <AssignmentIcon /> },
  RESOURCES_DISPATCHED: { label: 'Resources Dispatched', color: '#ff8800', icon: <LocalShippingIcon /> },
  IN_PROGRESS: { label: 'Response In Progress', color: '#ff8800', icon: <ConstructionIcon /> },
  RESOLVED: { label: 'Resolved', color: '#6bcb77', icon: <CheckCircleIcon /> }
}

const STATUS_ORDER = ['PENDING', 'VERIFIED', 'ASSIGNED', 'RESOURCES_DISPATCHED', 'IN_PROGRESS', 'RESOLVED']

const severityColor = { Low: '#6bcb77', Medium: '#ffd93d', High: '#ff8800', Critical: '#ff6b6b' }
const priorityColor = { LOW: '#6bcb77', MEDIUM: '#ffd93d', HIGH: '#ff8800', URGENT: '#ff6b6b' }

export default function TrackReport() {
  const navigate = useNavigate()
  const [reportId, setReportId] = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [report, setReport] = useState(null)
  const [timeline, setTimeline] = useState([])

  const handleSearch = async (e) => {
    e.preventDefault()
    const id = reportId.trim().toUpperCase()
    if (!id) { setError('Please enter your Report ID'); return }
    setError('')
    setLoading(true)
    setReport(null)
    try {
      const [rep, tl] = await Promise.all([
        publicReportApi.track(id),
        publicReportApi.trackTimeline(id)
      ])
      setReport(rep.data)
      setTimeline(tl.data)
    } catch (err) {
      const msg = err.response?.data?.error || err.response?.data?.message || 'Report not found. Check the Report ID and try again.'
      setError(msg)
      toast.error(msg)
    } finally {
      setLoading(false)
    }
  }

  const currentIndex = report ? Math.max(0, STATUS_ORDER.indexOf(report.status)) : 0

  return (
    <>
      <style>{GRADIENT_CSS}</style>
      <Box sx={{ minHeight: '100vh', position: 'relative', display: 'flex', flexDirection: 'column', alignItems: 'center', py: 4, px: 2 }}>
        <Box className="dms-animated-bg" sx={{ position: 'fixed', inset: 0, zIndex: 0 }}>
          <Box className="dms-grid-overlay" sx={{ position: 'absolute', inset: 0 }} />
        </Box>

        <Box sx={{ position: 'relative', zIndex: 1, width: '100%', maxWidth: 720, mb: 3 }}>
          <Stack direction="row" justifyContent="space-between" alignItems="center" sx={{ mb: 2 }}>
            <Button size="small" startIcon={<ArrowBackIcon />} onClick={() => navigate('/login')}
              sx={{ color: 'rgba(255,255,255,0.92)', '&:hover': { bgcolor: 'rgba(255,255,255,0.12)' } }}>
              Back to Login
            </Button>
            <Stack direction="row" spacing={1} alignItems="center" sx={{ color: '#fff' }}>
              <SecurityIcon fontSize="small" />
              <Typography variant="subtitle2" fontWeight={700} letterSpacing="0.5px">
                National Disaster Management
              </Typography>
            </Stack>
          </Stack>

          <Box sx={{ textAlign: 'center', mb: 3 }}>
            <Typography variant="h4" sx={{ color: '#fff', fontWeight: 800, textShadow: '0 2px 12px rgba(0,0,0,0.3)' }}>
              Track Your Report
            </Typography>
            <Typography variant="subtitle1" sx={{ color: 'rgba(255,255,255,0.9)' }}>
              Enter the Report ID you received after submitting your report
            </Typography>
          </Box>

          <Card className="dms-glass" sx={{ borderRadius: 4 }}>
            <CardContent sx={{ p: { xs: 2.5, sm: 4 } }}>
              <Box component="form" onSubmit={handleSearch} noValidate>
                {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
                <TextField
                  fullWidth size="medium" label="Report ID" required placeholder="DMS-20260801-XXXXXX"
                  value={reportId}
                  onChange={e => setReportId(e.target.value.toUpperCase())}
                  sx={{ fontFamily: 'monospace' }}
                  slotProps={{
                    input: {
                      sx: { letterSpacing: '0.5px', textTransform: 'uppercase', fontFamily: 'monospace' },
                      startAdornment: <InputAdornment position="start"><TrackChangesIcon fontSize="small" /></InputAdornment>
                    }
                  }}
                />
                <Button type="submit" fullWidth variant="contained" size="large" disabled={loading}
                  startIcon={loading ? <CircularProgress size={20} color="inherit" /> : <SearchIcon />}
                  sx={{ mt: 2, py: 1.3, fontWeight: 700 }}>
                  {loading ? 'Searching...' : 'Track Report'}
                </Button>
              </Box>

              {report && (
                <>
                  <Divider sx={{ my: 3 }} />
                  <Box sx={{ textAlign: 'center', mb: 2 }}>
                    <Typography variant="caption" color="text.secondary">Report Status</Typography>
                    <Box sx={{ mt: 0.5 }}>
                      <Chip
                        icon={STATUS_META[report.status]?.icon}
                        label={STATUS_META[report.status]?.label || report.status}
                        sx={{ fontWeight: 800, bgcolor: `${STATUS_META[report.status]?.color}22`, color: STATUS_META[report.status]?.color }}
                      />
                    </Box>
                  </Box>

                  <StatusStepper current={currentIndex} timeline={timeline} />

                  <Divider sx={{ my: 2 }} />
                  <Typography variant="subtitle2" fontWeight={700} color="#0F4C81" sx={{ mb: 1.5, textTransform: 'uppercase', letterSpacing: '0.5px' }}>
                    Report Details
                  </Typography>
                  <Grid spacing={1.5} container>
                    <InfoItem label="Report ID" value={report.reportId} mono />
                    <InfoItem label="Disaster Type" value={report.disasterType} />
                    <InfoItem label="Severity" value={report.severity} chip={severityColor[report.severity]} />
                    <InfoItem label="Priority" value={report.priority} chip={priorityColor[report.priority]} />
                    <InfoItem label="Address" value={report.address} wide />
                    <InfoItem label="Submitted" value={new Date(report.date).toLocaleString()} wide />
                  </Grid>

                  <Typography variant="body2" color="text.secondary" sx={{ mt: 2, whiteSpace: 'pre-wrap' }}>
                    {report.description}
                  </Typography>

                  {report.attachments?.length > 0 && (
                    <>
                      <Typography variant="subtitle2" fontWeight={700} color="#0F4C81" sx={{ mt: 2.5, mb: 1, textTransform: 'uppercase', letterSpacing: '0.5px' }}>
                        Your Evidence ({report.attachments.length})
                      </Typography>
                      <Stack direction="row" spacing={1} sx={{ flexWrap: 'wrap', gap: 1 }}>
                        {report.attachments.map(a => (
                          <AttachmentTile key={a.id} a={a} />
                        ))}
                      </Stack>
                    </>
                  )}

                  {timeline.length > 0 && (
                    <>
                      <Typography variant="subtitle2" fontWeight={700} color="#0F4C81" sx={{ mt: 2.5, mb: 1, textTransform: 'uppercase', letterSpacing: '0.5px' }}>
                        Update Log
                      </Typography>
                      <Stack spacing={1.5}>
                        {timeline.map(t => (
                          <Box key={t.id} sx={{ p: 1.5, bgcolor: 'rgba(15,76,129,0.05)', borderRadius: 2 }}>
                            <Typography variant="body2" fontWeight={700}>
                              {t.comment || `${t.fromStatus || 'Reported'} → ${t.toStatus}`}
                            </Typography>
                            <Typography variant="caption" color="text.secondary">
                              {new Date(t.changedAt).toLocaleString()}
                            </Typography>
                          </Box>
                        ))}
                      </Stack>
                    </>
                  )}
                </>
              )}

              <Box sx={{ mt: report ? 3 : 0, textAlign: 'center' }}>
                <Typography variant="caption" color="text.secondary">
                  Haven't submitted yet?{' '}
                  <Link to="/public-report" style={{ color: '#0F4C81', fontWeight: 700 }}>Report a disaster</Link>
                </Typography>
              </Box>
            </CardContent>
          </Card>

          <Stack direction="row" spacing={1} justifyContent="center" sx={{ mt: 2.5, color: 'rgba(255,255,255,0.9)' }}>
            <EmergencyIcon fontSize="small" />
            <Typography variant="caption">
              In an immediate emergency call <b>112</b> · Emergency Helpline <b>1078</b>
            </Typography>
          </Stack>
        </Box>
      </Box>
    </>
  )
}

function StatusStepper({ current, timeline }) {
  return (
    <Stack spacing={0}>
      {STATUS_ORDER.map((status, idx) => {
        const meta = STATUS_META[status]
        const reached = idx <= current
        const last = idx === STATUS_ORDER.length - 1
        return (
          <Box key={status} sx={{ display: 'flex', gap: 1.5 }}>
            <Stack alignItems="center" sx={{ width: 28 }}>
              <Box sx={{
                width: 28, height: 28, borderRadius: '50%', display: 'flex', alignItems: 'center',
                justifyContent: 'center', color: reached ? '#fff' : '#BDBDBD',
                backgroundColor: reached ? meta.color : 'rgba(0,0,0,0.08)',
                boxShadow: reached ? `0 0 0 4px ${meta.color}22` : 'none'
              }}>
                {meta.icon}
              </Box>
              {!last && (
                <Box sx={{
                  width: 3, flex: 1, minHeight: 28, borderRadius: 2,
                  backgroundColor: idx < current ? meta.color : 'rgba(0,0,0,0.1)'
                }} />
              )}
            </Stack>
            <Box sx={{ pb: reached && !last ? 1 : 0, pt: 0.25 }}>
              <Typography variant="body2" fontWeight={700} color={reached ? 'text.primary' : 'text.disabled'}>
                {meta.label}
              </Typography>
              {timeline.filter(t => t.toStatus === status).map(t => (
                <Typography key={t.id} variant="caption" color="text.secondary" display="block">
                  {new Date(t.changedAt).toLocaleString()}
                </Typography>
              ))}
            </Box>
          </Box>
        )
      })}
    </Stack>
  )
}

function InfoItem({ label, value, wide = false, chip, mono = false }) {
  return (
    <Grid size={{ xs: 12, sm: wide ? 12 : 6 }}>
      <Typography variant="caption" color="text.secondary">{label}</Typography>
      {chip ? (
        <Chip label={value} size="small" sx={{ fontWeight: 700, bgcolor: `${chip}22`, color: chip }} />
      ) : (
        <Typography variant="body2" fontWeight={600} sx={{ fontFamily: mono ? 'monospace' : undefined, letterSpacing: mono ? '0.5px' : undefined }}>
          {value || '—'}
        </Typography>
      )}
    </Grid>
  )
}

function AttachmentTile({ a }) {
  if (a.category === 'IMAGE') {
    return (
      <Box sx={{ width: 72, height: 72, overflow: 'hidden', borderRadius: 2, border: '1px solid rgba(0,0,0,0.08)' }}>
        <img src={a.dataUrl} alt={a.filename} style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
      </Box>
    )
  }
  const Icon = a.category === 'VIDEO' ? PlayCircleIcon : PictureAsPdfIcon
  return (
    <Stack direction="row" spacing={0.5} alignItems="center" sx={{ p: 1, border: '1px solid rgba(0,0,0,0.1)', borderRadius: 2 }}>
      <Icon fontSize="small" color="primary" />
      <Typography variant="caption" fontWeight={600}>{a.filename}</Typography>
    </Stack>
  )
}
