import { useState } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { publicReportApi } from '../services/api'
import toast from 'react-hot-toast'
import {
  Box, Card, CardContent, Typography, TextField, Button, CircularProgress,
  Chip, Alert, Paper, Divider, List, ListItem, ListItemAvatar, Avatar, ListItemText
} from '@mui/material'
import SecurityIcon from '@mui/icons-material/Security'
import TrackChangesIcon from '@mui/icons-material/TrackChanges'
import SearchIcon from '@mui/icons-material/Search'
import ReportProblemIcon from '@mui/icons-material/ReportProblem'
import LoginIcon from '@mui/icons-material/Login'
import MyLocationIcon from '@mui/icons-material/MyLocation'
import HistoryIcon from '@mui/icons-material/History'
import PictureAsPdfIcon from '@mui/icons-material/PictureAsPdf'
import { statusLabel, statusColor, severityColor, priorityColor, STATUSES } from '../constants/disaster'

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
  background: rgba(255, 255, 255, 0.96);
  backdrop-filter: blur(18px);
  -webkit-backdrop-filter: blur(18px);
  border: 1px solid rgba(255, 255, 255, 0.55);
  box-shadow: 0 20px 60px rgba(4, 28, 51, 0.45);
}
`

export default function PublicTrack() {
  const navigate = useNavigate()
  const [params, setParams] = useSearchParams()
  const initial = params.get('reportId') || ''
  const [reportId, setReportId] = useState(initial)
  const [input, setInput] = useState(initial)
  const [report, setReport] = useState(null)
  const [timeline, setTimeline] = useState([])
  const [loading, setLoading] = useState(false)
  const [notFound, setNotFound] = useState(false)
  const [searched, setSearched] = useState(false)

  const handleSearch = async (e) => {
    e?.preventDefault()
    const id = input.trim().toUpperCase()
    if (!id) {
      toast.error('Please enter your Report ID')
      return
    }
    setLoading(true)
    setNotFound(false)
    setSearched(true)
    try {
      const [repRes, tlRes] = await Promise.all([
        publicReportApi.track(id),
        publicReportApi.trackTimeline(id),
      ])
      setReport(repRes.data)
      setTimeline(tlRes.data)
      setReportId(id)
      setParams({ reportId: id }, { replace: true })
    } catch (err) {
      if (err.response?.status === 404) {
        setNotFound(true)
        setReport(null)
        setTimeline([])
      } else {
        toast.error('Failed to track report')
      }
    } finally {
      setLoading(false)
    }
  }

  return (
    <>
      <style>{GRADIENT_CSS}</style>
      <Box sx={{ minHeight: '100vh', position: 'relative', p: { xs: 2, sm: 4 } }}>
        <Box className="dms-animated-bg" sx={{ position: 'fixed', inset: 0, zIndex: 0 }} />

        <Box sx={{ position: 'relative', zIndex: 1, textAlign: 'center', mb: 3 }}>
          <Box sx={{ display: 'inline-flex', alignItems: 'center', gap: 1.5, mb: 1, px: 2, py: 1, borderRadius: 3, backgroundColor: 'rgba(255,255,255,0.12)', backdropFilter: 'blur(8px)' }}>
            <SecurityIcon sx={{ color: '#fff' }} />
            <Typography variant="h6" sx={{ color: '#fff', fontWeight: 700, letterSpacing: '0.5px' }}>
              National Disaster Management
            </Typography>
          </Box>
          <Typography variant="subtitle1" sx={{ color: 'rgba(255,255,255,0.92)' }}>
            Track Your Disaster Report
          </Typography>
        </Box>

        <Box sx={{ position: 'relative', zIndex: 1, maxWidth: 680, mx: 'auto' }}>
          <Card className="dms-glass" sx={{ borderRadius: 4 }}>
            <CardContent sx={{ p: { xs: 3, sm: 4 } }}>
              <Box component="form" onSubmit={handleSearch}>
                <Typography variant="h5" fontWeight={700} color="#0F4C81" sx={{ mb: 0.5 }}>
                  Check Report Status
                </Typography>
                <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                  Enter the Report ID you received after submitting your disaster report.
                </Typography>
                <Box sx={{ display: 'flex', gap: 1 }}>
                  <TextField
                    fullWidth size="small" placeholder="e.g. DMS-20260801-ABCDEF"
                    value={input}
                    onChange={e => setInput(e.target.value)}
                    autoFocus={!reportId}
                    slotProps={{
                      input: {
                        style: { fontFamily: 'monospace', letterSpacing: 1 },
                        startAdornment: <TrackChangesIcon fontSize="small" sx={{ mr: 1, color: '#636E72' }} />,
                      }
                    }}
                  />
                  <Button type="submit" variant="contained" disabled={loading}
                    startIcon={loading ? <CircularProgress size={16} color="inherit" /> : <SearchIcon />}>
                    Track
                  </Button>
                </Box>
              </Box>

              {notFound && (
                <Alert severity="error" sx={{ mt: 2 }}>
                  No report found with ID <b>{input.trim().toUpperCase()}</b>. Please check the ID and try again.
                </Alert>
              )}

              {report && (
                <>
                  <Paper variant="outlined" sx={{ p: 2.5, mt: 3, backgroundColor: 'rgba(15,76,129,0.03)' }}>
                    <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: 1, mb: 1 }}>
                      <Typography variant="subtitle1" fontWeight={700} color="#0F4C81" sx={{ fontFamily: 'monospace' }}>
                        {report.reportId}
                      </Typography>
                      <Chip label={statusLabel[report.status] || report.status}
                        sx={{ bgcolor: `${statusColor[report.status]}22`, color: statusColor[report.status], fontWeight: 700 }} />
                    </Box>
                    <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1, mb: 1.5 }}>
                      <Chip label={report.disasterType} color="primary" size="small" />
                      <Chip label={report.severity} size="small" sx={{ bgcolor: `${severityColor[report.severity]}22`, color: severityColor[report.severity] }} />
                      <Chip label={`Priority: ${report.priority}`} size="small" sx={{ bgcolor: `${priorityColor[report.priority]}22`, color: priorityColor[report.priority] }} />
                    </Box>
                    <Typography variant="body2" sx={{ mb: 0.5 }}>{report.description}</Typography>
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5, mt: 1 }}>
                      <MyLocationIcon fontSize="small" color="primary" />
                      <Typography variant="body2">{report.address}</Typography>
                      {report.latitude != null && report.longitude != null && (
                        <Typography variant="caption" color="text.secondary">
                          ({report.latitude.toFixed(4)}, {report.longitude.toFixed(4)})
                        </Typography>
                      )}
                    </Box>
                    <Typography variant="caption" color="text.secondary" sx={{ display: 'block', mt: 1 }}>
                      Submitted: {new Date(report.date).toLocaleString()}
                    </Typography>
                  </Paper>

                  {report.attachments?.length > 0 && (
                    <Paper variant="outlined" sx={{ p: 2, mt: 2 }}>
                      <Typography variant="subtitle2" fontWeight={700} sx={{ mb: 1 }}>Submitted Evidence</Typography>
                      <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1.5 }}>
                        {report.attachments.filter(a => a.category === 'IMAGE').map(a => (
                          <Box key={a.id} component="img" src={a.dataUrl} alt={a.filename}
                            sx={{ width: 96, height: 96, objectFit: 'cover', borderRadius: 1.5, border: '1px solid rgba(0,0,0,0.08)' }} />
                        ))}
                        {report.attachments.filter(a => a.category === 'PDF').map(a => (
                          <Box key={a.id} component="a" href={a.dataUrl} target="_blank" rel="noreferrer"
                            sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 0.5, width: 96, p: 1, borderRadius: 1.5, border: '1px solid rgba(198,40,40,0.3)', textDecoration: 'none', color: 'inherit' }}>
                            <PictureAsPdfIcon sx={{ fontSize: 36, color: '#C62828' }} />
                            <Typography variant="caption" sx={{ fontSize: '0.6rem', wordBreak: 'break-all' }}>{a.filename}</Typography>
                          </Box>
                        ))}
                      </Box>
                    </Paper>
                  )}

                  <Divider sx={{ my: 2.5 }} />

                  <Typography variant="h6" sx={{ mb: 1.5, display: 'flex', alignItems: 'center', gap: 1 }}>
                    <HistoryIcon fontSize="small" /> Status Timeline
                  </Typography>
                  {timeline.length === 0 ? (
                    <Typography variant="body2" color="text.secondary">No status updates yet. Your report is awaiting review.</Typography>
                  ) : (
                    <List dense>
                      {[...timeline].reverse().map((entry, i) => (
                        <ListItem key={entry.id || i} alignItems="flex-start" sx={{ px: 0 }}>
                          <ListItemAvatar>
                            <Avatar sx={{ width: 30, height: 30, bgcolor: entry.toStatus ? `${statusColor[entry.toStatus]}30` : '#EEEEEE', color: entry.toStatus ? statusColor[entry.toStatus] : '#757575', fontSize: '0.75rem' }}>
                              {i + 1}
                            </Avatar>
                          </ListItemAvatar>
                          <ListItemText
                            primary={
                              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, flexWrap: 'wrap' }}>
                                <Typography variant="body2" fontWeight={600}>
                                  {entry.toStatus ? (statusLabel[entry.toStatus] || entry.toStatus) : 'Report Received'}
                                </Typography>
                                <Chip label={new Date(entry.changedAt).toLocaleString()} size="small" sx={{ fontSize: '0.6rem', height: 20 }} />
                              </Box>
                            }
                            secondary={entry.comment}
                          />
                        </ListItem>
                      ))}
                    </List>
                  )}

                  <Divider sx={{ my: 2 }} />
                  <Typography variant="caption" color="text.secondary">
                    For updates on your report, keep this ID safe. Emergency helpline: <b>112</b>
                  </Typography>
                </>
              )}
            </CardContent>
          </Card>

          <Box sx={{ mt: 2, display: 'flex', justifyContent: 'center', gap: 1.5, flexWrap: 'wrap' }}>
            <Button
              size="small" color="inherit" startIcon={<ReportProblemIcon fontSize="small" />}
              onClick={() => navigate('/public-report')}
              sx={{ color: 'rgba(255,255,255,0.95)', textTransform: 'none', '&:hover': { bgcolor: 'rgba(255,255,255,0.12)' } }}
            >
              Submit a new report
            </Button>
            <Button
              size="small" color="inherit" startIcon={<LoginIcon fontSize="small" />}
              onClick={() => navigate('/login')}
              sx={{ color: 'rgba(255,255,255,0.95)', textTransform: 'none', '&:hover': { bgcolor: 'rgba(255,255,255,0.12)' } }}
            >
              Authorized Personnel Login
            </Button>
          </Box>
        </Box>
      </Box>
    </>
  )
}
