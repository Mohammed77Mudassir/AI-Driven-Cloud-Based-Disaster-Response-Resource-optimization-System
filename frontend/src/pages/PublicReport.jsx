import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { publicReportApi } from '../services/api'
import FileUploadDropzone from '../components/FileUploadDropzone'
import toast from 'react-hot-toast'
import {
  Box, Card, CardContent, Typography, TextField, MenuItem,
  Button, CircularProgress, Grid, InputAdornment, Paper, Divider, Stack
} from '@mui/material'
import { ThemeProvider } from '@mui/material/styles'
import { lightTheme } from '../theme'
import SecurityIcon from '@mui/icons-material/Security'
import CrisisAlertIcon from '@mui/icons-material/CrisisAlert'
import CategoryIcon from '@mui/icons-material/Category'
import WarningAmberIcon from '@mui/icons-material/WarningAmber'
import PriorityHighIcon from '@mui/icons-material/PriorityHigh'
import BadgeIcon from '@mui/icons-material/Badge'
import PersonIcon from '@mui/icons-material/Person'
import PhoneIcon from '@mui/icons-material/Phone'
import EmailIcon from '@mui/icons-material/Email'
import MyLocationIcon from '@mui/icons-material/MyLocation'
import LocationOnIcon from '@mui/icons-material/LocationOn'
import DescriptionIcon from '@mui/icons-material/Description'
import SendIcon from '@mui/icons-material/Send'
import RestartAltIcon from '@mui/icons-material/RestartAlt'
import TrackChangesIcon from '@mui/icons-material/TrackChanges'
import ReportProblemIcon from '@mui/icons-material/ReportProblem'
import CheckCircleIcon from '@mui/icons-material/CheckCircle'
import LoginIcon from '@mui/icons-material/Login'
import GppGoodIcon from '@mui/icons-material/GppGood'
import VerifiedUserIcon from '@mui/icons-material/VerifiedUser'
import SpeedIcon from '@mui/icons-material/Speed'
import PhotoCameraIcon from '@mui/icons-material/PhotoCamera'
import ScheduleIcon from '@mui/icons-material/Schedule'
import PhoneEnabledIcon from '@mui/icons-material/PhoneEnabled'
import EmergencyIcon from '@mui/icons-material/Emergency'
import LockIcon from '@mui/icons-material/Lock'
import LocalPoliceIcon from '@mui/icons-material/LocalPolice'
import { DISASTER_TYPES, SEVERITIES, PRIORITIES } from '../constants/disaster'

const PAGE_CSS = `
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap');

.prd-page {
  min-height: 100vh;
  position: relative;
  padding: 28px 16px 40px;
  background: #F8FAFC;
  overflow-x: hidden;
  font-family: 'Inter', 'Roboto', 'Segoe UI', sans-serif;
}
.prd-bg { position: fixed; inset: 0; z-index: 0; background: #F8FAFC; }
.prd-bg__grid {
  position: absolute; inset: 0;
  background-image:
    linear-gradient(rgba(37, 99, 235, 0.055) 1px, transparent 1px),
    linear-gradient(90deg, rgba(37, 99, 235, 0.055) 1px, transparent 1px);
  background-size: 44px 44px;
  -webkit-mask-image: radial-gradient(ellipse 90% 70% at 50% 0%, #000 35%, transparent 100%);
  mask-image: radial-gradient(ellipse 90% 70% at 50% 0%, #000 35%, transparent 100%);
}
.prd-blob { position: absolute; border-radius: 50%; filter: blur(90px); opacity: 0.55; pointer-events: none; }
.prd-blob--one { width: 540px; height: 540px; top: -180px; left: -160px; background: #DBEAFE; }
.prd-blob--two { width: 480px; height: 480px; top: 28%; right: -200px; background: #CCFBF1; }
.prd-blob--three { width: 400px; height: 400px; bottom: -160px; left: 28%; background: #E0E7FF; }

.prd-glass {
  background: rgba(255, 255, 255, 0.98);
  backdrop-filter: blur(18px);
  -webkit-backdrop-filter: blur(18px);
  border: 1px solid rgba(226, 232, 240, 0.9);
  box-shadow: 0 24px 60px -20px rgba(30, 64, 175, 0.18), 0 2px 8px rgba(15, 23, 42, 0.04);
}

@keyframes prdFloat {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-9px); }
}
@keyframes prdFloat2 {
  0%, 100% { transform: translateY(0) rotate(0deg); }
  50% { transform: translateY(7px) rotate(-3deg); }
}
@keyframes prdSweep {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}
@keyframes prdPulse {
  0%, 100% { opacity: 0.35; }
  50% { opacity: 1; }
}
.prd-ill__sweep { transform-origin: 240px 150px; animation: prdSweep 9s linear infinite; }
.prd-ill__float { animation: prdFloat 6s ease-in-out infinite; }
.prd-ill__float--2 { animation: prdFloat2 7s ease-in-out infinite; }
.prd-ill__blip { animation: prdPulse 3s ease-in-out infinite; }
.prd-ill__wave { animation: prdPulse 2.4s ease-out infinite; }
.prd-ill__wave--2 { animation-delay: 0.4s; }
.prd-ill__wave--3 { animation-delay: 0.8s; }
`

const inputSx = {
  '& .MuiOutlinedInput-root': {
    borderRadius: '12px',
    backgroundColor: '#F8FAFC',
    transition: 'box-shadow 0.2s ease, border-color 0.2s ease, background-color 0.2s ease',
    '& .MuiOutlinedInput-notchedOutline': { borderColor: '#CBD5E1' },
    '&:hover .MuiOutlinedInput-notchedOutline': { borderColor: '#93C5FD' },
    '&.Mui-focused': {
      backgroundColor: '#FFFFFF',
      boxShadow: '0 0 0 4px rgba(37, 99, 235, 0.13)',
      '& .MuiOutlinedInput-notchedOutline': { borderColor: '#2563EB', borderWidth: 2 },
    },
  },
  '& .MuiInputLabel-root': { color: '#475569', fontSize: '0.875rem' },
  '& .MuiInputLabel-root.Mui-focused': { color: '#2563EB', fontWeight: 600 },
  '& .MuiFormHelperText-root': { ml: 0.5, fontSize: '0.75rem', fontWeight: 500 },
  '& .MuiSelect-icon': { color: '#64748B' },
}

const TRUST_BADGES = [
  { icon: GppGoodIcon, label: 'Secure & Confidential' },
  { icon: VerifiedUserIcon, label: 'Verified Reporting' },
  { icon: SpeedIcon, label: 'Rapid Response' },
]

const REPORTING_TIPS = [
  { icon: VerifiedUserIcon, text: 'Be as specific and accurate as you can.' },
  { icon: PhotoCameraIcon, text: 'Attach photos, videos or documents as evidence.' },
  { icon: MyLocationIcon, text: 'Share your exact GPS location for faster dispatch.' },
  { icon: ScheduleIcon, text: 'Report as early as possible — every minute matters.' },
]

const HELPLINES = [
  { icon: PhoneEnabledIcon, label: 'Emergency Helpline', value: '112' },
  { icon: EmergencyIcon, label: 'National Disaster Helpline', value: '1078' },
]

function SectionTitle({ icon: Icon, title, subtitle, accent, first }) {
  return (
    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5, mb: 2.5, mt: first ? 0 : { xs: 3.5, sm: 4 } }}>
      <Box sx={{
        width: 44, height: 44, flexShrink: 0, borderRadius: '14px',
        display: 'flex', alignItems: 'center', justifyContent: 'center',
        background: `linear-gradient(135deg, ${accent}, ${accent}CC)`,
        color: '#fff',
        boxShadow: `0 10px 20px -8px ${accent}99`,
      }}>
        <Icon fontSize="small" />
      </Box>
      <Box>
        <Typography variant="subtitle1" sx={{ fontWeight: 800, lineHeight: 1.2, color: '#0F172A' }}>
          {title}
        </Typography>
        <Typography variant="caption" color="text.secondary" sx={{ lineHeight: 1.3, display: 'block', fontWeight: 500 }}>
          {subtitle}
        </Typography>
      </Box>
    </Box>
  )
}

function EmergencyIllustration() {
  return (
    <svg className="prd-ill" viewBox="0 0 480 300" fill="none" aria-hidden="true" style={{ width: '100%', maxWidth: 460, height: 'auto' }}>
      <defs>
        <linearGradient id="prdRadar" x1="0" y1="0" x2="1" y2="1">
          <stop offset="0%" stopColor="#7CBDFF" />
          <stop offset="100%" stopColor="#5BDCCA" />
        </linearGradient>
        <linearGradient id="prdShield" x1="0" y1="0" x2="0" y2="1">
          <stop offset="0%" stopColor="#7CBDFF" />
          <stop offset="100%" stopColor="#2BC8B2" />
        </linearGradient>
        <radialGradient id="prdGlow" cx="50%" cy="50%" r="50%">
          <stop offset="0%" stopColor="rgba(255,255,255,0.28)" />
          <stop offset="100%" stopColor="rgba(255,255,255,0)" />
        </radialGradient>
      </defs>

      <circle cx="240" cy="150" r="130" fill="url(#prdGlow)" />

      <g stroke="rgba(255,255,255,0.12)" strokeWidth="1">
        <path d="M60 70H420M60 110H420M60 150H420M60 190H420M60 230H420" />
        <path d="M100 60V250M140 60V250M180 60V250M220 60V250M260 60V250M300 60V250M340 60V250M380 60V250" />
      </g>

      <circle cx="240" cy="150" r="36" stroke="rgba(255,255,255,0.30)" strokeWidth="1.4" strokeDasharray="4 6" />
      <circle cx="240" cy="150" r="66" stroke="rgba(255,255,255,0.20)" strokeWidth="1.2" />
      <circle cx="240" cy="150" r="98" stroke="rgba(255,255,255,0.14)" strokeWidth="1" />

      <g className="prd-ill__sweep">
        <path d="M240 150 L240 52 A98 98 0 0 1 337 92 Z" fill="url(#prdRadar)" opacity="0.18" />
        <path d="M240 150 L240 52" stroke="rgba(255,255,255,0.55)" strokeWidth="2" strokeLinecap="round" />
      </g>

      <circle className="prd-ill__blip" cx="286" cy="112" r="4.5" fill="#5BDCCA" />
      <circle className="prd-ill__blip" cx="196" cy="186" r="4.5" fill="#7CBDFF" style={{ animationDelay: '0.6s' }} />
      <circle className="prd-ill__blip" cx="160" cy="130" r="4" fill="#F9A825" style={{ animationDelay: '1.2s' }} />
      <circle className="prd-ill__blip" cx="312" cy="196" r="3.6" fill="#5BDCCA" style={{ animationDelay: '1.8s' }} />

      <g className="prd-ill__float">
        <circle cx="240" cy="150" r="32" fill="rgba(77,163,255,0.16)" stroke="rgba(255,255,255,0.35)" strokeWidth="1.2" />
        <path
          d="M240 126l15 6v10.2c0 11.3-6.6 19-15 21.8-8.4-2.8-15-10.5-15-21.8V132l15-6z"
          fill="url(#prdShield)"
        />
        <path d="M234 152.5l4.2 4.2 8-8.8" stroke="#05121F" strokeWidth="2.4" strokeLinecap="round" strokeLinejoin="round" fill="none" />
      </g>

      <g className="prd-ill__float prd-ill__float--2" transform="translate(360 52)">
        <rect x="-15" y="-6" width="30" height="17" rx="4.5" fill="rgba(91,220,202,0.92)" />
        <rect x="-32" y="-2" width="17" height="8" rx="2.5" fill="rgba(91,220,202,0.55)" />
        <rect x="15" y="-2" width="17" height="8" rx="2.5" fill="rgba(91,220,202,0.55)" />
        <rect x="-2" y="-24" width="4" height="15" rx="2" fill="rgba(91,220,202,0.75)" />
        <rect x="-2" y="-24" width="25" height="4" rx="2" fill="rgba(91,220,202,0.75)" transform="rotate(-35 -2 -24)" />
      </g>

      <g transform="translate(360 52)">
        <circle className="prd-ill__wave" cx="0" cy="0" r="20" stroke="rgba(91,220,202,0.55)" strokeWidth="1.6" />
        <circle className="prd-ill__wave prd-ill__wave--2" cx="0" cy="0" r="20" stroke="rgba(91,220,202,0.4)" strokeWidth="1.6" />
        <circle className="prd-ill__wave prd-ill__wave--3" cx="0" cy="0" r="20" stroke="rgba(91,220,202,0.25)" strokeWidth="1.6" />
      </g>

      <g className="prd-ill__float">
        <path d="M116 226c0-8.4 7.2-15.2 16-15.2S148 217.6 148 226c0 8.2-16 22-16 22s-16-13.8-16-22z" fill="rgba(249,168,37,0.95)" />
        <circle cx="132" cy="225" r="5.4" fill="#05121F" />
      </g>
      <g className="prd-ill__float prd-ill__float--2">
        <path d="M336 220c0-7.6 6.2-13.8 13.8-13.8S363.6 212.4 363.6 220c0 7.4-13.8 19.8-13.8 19.8s-13.8-12.4-13.8-19.8z" fill="rgba(91,220,202,0.95)" />
        <circle cx="349.8" cy="219" r="4.8" fill="#05121F" />
      </g>
    </svg>
  )
}

export default function PublicReport() {
  const navigate = useNavigate()
  const [form, setForm] = useState({
    reporterName: '', reporterMobile: '', reporterEmail: '',
    disasterType: '', severity: '', priority: 'MEDIUM',
    description: '', address: '', latitude: '', longitude: '',
  })
  const [attachments, setAttachments] = useState([])
  const [errors, setErrors] = useState({})
  const [loading, setLoading] = useState(false)
  const [locating, setLocating] = useState(false)
  const [success, setSuccess] = useState(null)

  const handleChange = (e) => {
    const { name, value } = e.target
    setForm(prev => ({ ...prev, [name]: value }))
    setErrors(prev => ({ ...prev, [name]: '' }))
  }

  const handleReset = () => {
    setForm({
      reporterName: '', reporterMobile: '', reporterEmail: '',
      disasterType: '', severity: '', priority: 'MEDIUM',
      description: '', address: '', latitude: '', longitude: '',
    })
    setErrors({})
    setAttachments([])
  }

  const detectLocation = () => {
    if (!navigator.geolocation) {
      toast.error('Geolocation is not supported by your browser')
      return
    }
    setLocating(true)
    navigator.geolocation.getCurrentPosition(
      (pos) => {
        setForm(prev => ({
          ...prev,
          latitude: pos.coords.latitude.toFixed(6),
          longitude: pos.coords.longitude.toFixed(6),
        }))
        toast.success('GPS location captured')
        setLocating(false)
      },
      () => {
        toast.error('Could not access your location. Allow location permission and try again.')
        setLocating(false)
      },
      { enableHighAccuracy: true, timeout: 12000 }
    )
  }

  const validate = () => {
    const next = {}
    if (!form.reporterName.trim()) next.reporterName = 'Name is required'
    if (!form.reporterMobile.trim()) next.reporterMobile = 'Mobile number is required'
    else if (!/^[+0-9\-() ]{8,15}$/.test(form.reporterMobile.trim())) next.reporterMobile = 'Enter a valid mobile number'
    if (form.reporterEmail && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.reporterEmail)) next.reporterEmail = 'Enter a valid email'
    if (!form.disasterType) next.disasterType = 'Disaster type is required'
    if (!form.severity) next.severity = 'Severity is required'
    if (!form.description.trim()) next.description = 'Description is required'
    else if (form.description.trim().length < 10) next.description = 'Description must be at least 10 characters'
    if (!form.address.trim()) next.address = 'Address is required'
    const lat = parseFloat(form.latitude)
    const lng = parseFloat(form.longitude)
    if (form.latitude !== '' && (isNaN(lat) || lat < -90 || lat > 90)) next.latitude = 'Valid latitude (-90 to 90)'
    if (form.longitude !== '' && (isNaN(lng) || lng < -180 || lng > 180)) next.longitude = 'Valid longitude (-180 to 180)'
    setErrors(next)
    return Object.keys(next).length === 0
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    if (!validate()) {
      toast.error('Please fix the highlighted fields')
      return
    }
    setLoading(true)
    try {
      const payload = {
        ...form,
        latitude: form.latitude !== '' ? parseFloat(form.latitude) : null,
        longitude: form.longitude !== '' ? parseFloat(form.longitude) : null,
        reporterEmail: form.reporterEmail.trim() || null,
        attachments: attachments.map(({ id, ...rest }) => rest),
      }
      const res = await publicReportApi.submit(payload)
      setSuccess(res.data)
      toast.success('Report submitted successfully')
    } catch (err) {
      const data = err.response?.data
      if (data && typeof data === 'object') {
        const msgs = Object.values(data).filter(Boolean).join('. ')
        toast.error(msgs || 'Failed to submit report')
      } else {
        toast.error('Failed to submit report')
      }
    } finally {
      setLoading(false)
    }
  }

  const handleSubmitAnother = () => {
    setSuccess(null)
    setForm({
      reporterName: '', reporterMobile: '', reporterEmail: '',
      disasterType: '', severity: '', priority: 'MEDIUM',
      description: '', address: '', latitude: '', longitude: '',
    })
    setAttachments([])
  }

  return (
    <ThemeProvider theme={lightTheme}>
      <style>{PAGE_CSS}</style>
      <Box className="prd-page">
        {/* Decorative light background */}
        <Box className="prd-bg" aria-hidden="true">
          <Box className="prd-bg__grid" />
          <Box className="prd-blob prd-blob--one" />
          <Box className="prd-blob prd-blob--two" />
          <Box className="prd-blob prd-blob--three" />
        </Box>

        <Box sx={{ position: 'relative', zIndex: 1, maxWidth: 1200, mx: 'auto', width: '100%' }}>
          {/* Branding */}
          <Box sx={{ textAlign: 'center', mb: { xs: 3, md: 4 } }}>
            <Box className="prd-brand-pill" sx={{ mb: 1.25 }}>
              <Box sx={{
                width: 34, height: 34, borderRadius: '10px', display: 'flex', alignItems: 'center', justifyContent: 'center',
                background: 'linear-gradient(135deg, #2563EB, #0F766E)',
                boxShadow: '0 6px 14px -4px rgba(37,99,235,0.5)',
              }}>
                <SecurityIcon sx={{ color: '#fff', fontSize: 19 }} />
              </Box>
              <Typography variant="h6" sx={{ color: '#0F172A', fontWeight: 800, letterSpacing: '0.4px', fontSize: '0.98rem' }}>
                National Disaster Management
              </Typography>
            </Box>
            <Typography variant="subtitle2" sx={{ color: '#475569', fontWeight: 600, letterSpacing: '0.3px' }}>
              Public Citizen Disaster Reporting Portal
            </Typography>
          </Box>

          {success ? (
            <Card className="prd-glass" sx={{ borderRadius: '24px', maxWidth: 720, mx: 'auto' }}>
              <CardContent sx={{ p: { xs: 3, sm: 5 }, textAlign: 'center' }}>
                <Box sx={{
                  width: 96, height: 96, mx: 'auto', borderRadius: '50%',
                  display: 'flex', alignItems: 'center', justifyContent: 'center',
                  background: 'linear-gradient(135deg, #16a34a, #059669)',
                  boxShadow: '0 16px 36px -10px rgba(22,163,74,0.5)',
                  mb: 2.5,
                }}>
                  <CheckCircleIcon sx={{ fontSize: 54, color: '#fff' }} />
                </Box>
                <Typography variant="h4" fontWeight={800} color="#0F172A" sx={{ mt: 1 }}>
                  Report Submitted Successfully
                </Typography>
                <Typography variant="body2" color="text.secondary" sx={{ mt: 1, mb: 2.5 }}>
                  Thank you, {success.reporterName}. Your report has been received and is being reviewed.
                </Typography>

                <Paper
                  variant="outlined"
                  sx={{ p: 3, mb: 3, borderRadius: '16px', backgroundColor: '#EFF6FF', borderColor: 'rgba(37,99,235,0.25)' }}
                >
                  <Typography variant="caption" color="text.secondary" display="block" sx={{ fontWeight: 600 }}>
                    Your Report ID
                  </Typography>
                  <Typography variant="h3" sx={{ fontFamily: 'monospace', fontWeight: 800, color: '#1D4ED8', letterSpacing: 1 }}>
                    {success.reportId}
                  </Typography>
                  <Typography variant="caption" color="text.secondary" sx={{ mt: 1, display: 'block' }}>
                    Save this ID to track your report. An SMS confirmation has been sent to {success.reporterName}'s mobile.
                  </Typography>
                </Paper>

                <Box sx={{ display: 'flex', gap: 1.5, justifyContent: 'center', flexWrap: 'wrap' }}>
                  <Button
                    variant="contained" startIcon={<TrackChangesIcon />}
                    onClick={() => navigate(`/track-report?reportId=${success.reportId}`)}
                    sx={{
                      borderRadius: '12px', textTransform: 'none', fontWeight: 700, px: 3, py: 1.1,
                      background: 'linear-gradient(135deg, #2563EB, #1D4ED8)',
                      boxShadow: '0 10px 22px -8px rgba(37,99,235,0.55)',
                      '&:hover': { background: 'linear-gradient(135deg, #3B82F6, #2563EB)', transform: 'translateY(-1px)' },
                    }}
                  >
                    Track This Report
                  </Button>
                  <Button
                    variant="outlined" startIcon={<ReportProblemIcon />} onClick={handleSubmitAnother}
                    sx={{
                      borderRadius: '12px', textTransform: 'none', fontWeight: 700, px: 3, py: 1.1,
                      borderColor: '#CBD5E1', color: '#1D4ED8',
                      '&:hover': { borderColor: '#2563EB', backgroundColor: 'rgba(37,99,235,0.06)' },
                    }}
                  >
                    Submit Another Report
                  </Button>
                </Box>
              </CardContent>
            </Card>
          ) : (
            <Grid container spacing={{ xs: 2.5, md: 4 }} sx={{ alignItems: 'stretch' }}>
              {/* LEFT PANEL — Information */}
              <Grid size={{ xs: 12, md: 5 }}>
                <Box sx={{
                  height: '100%',
                  borderRadius: '24px',
                  p: { xs: 3, sm: 4 },
                  color: '#fff',
                  background: 'linear-gradient(165deg, #1E40AF 0%, #2563EB 52%, #0F766E 100%)',
                  position: 'relative',
                  overflow: 'hidden',
                  border: '1px solid rgba(255,255,255,0.2)',
                  boxShadow: '0 28px 60px -18px rgba(30,64,175,0.55)',
                  display: 'flex', flexDirection: 'column',
                }}>
                  <Box sx={{
                    position: 'absolute', top: -70, right: -70, width: 220, height: 220, borderRadius: '50%',
                    background: 'radial-gradient(circle, rgba(91,220,202,0.22), transparent 70%)',
                    pointerEvents: 'none',
                  }} />
                  <Box sx={{ position: 'relative' }}>
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.25, mb: 2 }}>
                      <Box sx={{
                        width: 38, height: 38, borderRadius: '12px', display: 'flex', alignItems: 'center', justifyContent: 'center',
                        background: 'rgba(255,255,255,0.16)', border: '1px solid rgba(255,255,255,0.22)',
                      }}>
                        <CrisisAlertIcon sx={{ color: '#FFD54F' }} />
                      </Box>
                      <Typography variant="body2" sx={{ fontWeight: 700, letterSpacing: '0.5px', textTransform: 'uppercase', fontSize: '0.72rem', color: 'rgba(255,255,255,0.9)' }}>
                        Emergency Reporting Center
                      </Typography>
                    </Box>

                    <Typography variant="h4" sx={{ fontWeight: 800, lineHeight: 1.15, letterSpacing: '-0.02em', fontSize: '1.6rem' }}>
                      Help Us Save Lives
                    </Typography>
                    <Typography variant="body2" sx={{ mt: 1.5, mb: 1, color: 'rgba(255,255,255,0.92)', lineHeight: 1.65, fontWeight: 500 }}>
                      Report a disaster in your area in under two minutes. Every verified report helps
                      first responders reach affected people faster.
                    </Typography>

                    <Box sx={{ my: 1 }}>
                      <EmergencyIllustration />
                    </Box>

                    {/* Trust badges */}
                    <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1, mb: 3 }}>
                      {TRUST_BADGES.map(({ icon: Icon, label }) => (
                        <Box key={label} sx={{
                          display: 'inline-flex', alignItems: 'center', gap: 0.75,
                          px: 1.5, py: 0.75, borderRadius: '10px',
                          background: 'rgba(255,255,255,0.14)', border: '1px solid rgba(255,255,255,0.2)',
                          backdropFilter: 'blur(6px)',
                        }}>
                          <Icon sx={{ fontSize: 16, color: '#5BDCCA' }} />
                          <Typography variant="caption" sx={{ fontWeight: 600, color: '#fff' }}>{label}</Typography>
                        </Box>
                      ))}
                    </Box>

                    {/* Quick reporting tips */}
                    <Box sx={{ mb: 3 }}>
                      <Typography variant="subtitle2" sx={{ fontWeight: 700, mb: 1.5, color: '#FFD54F', letterSpacing: '0.3px' }}>
                        Quick Reporting Tips
                      </Typography>
                      <Stack spacing={1.25}>
                        {REPORTING_TIPS.map(({ icon: Icon, text }) => (
                          <Box key={text} sx={{ display: 'flex', alignItems: 'flex-start', gap: 1.25 }}>
                            <Box sx={{
                              width: 28, height: 28, flexShrink: 0, borderRadius: '9px',
                              display: 'flex', alignItems: 'center', justifyContent: 'center',
                              background: 'rgba(255,255,255,0.14)',
                            }}>
                              <Icon sx={{ fontSize: 15, color: '#5BDCCA' }} />
                            </Box>
                            <Typography variant="body2" sx={{ color: 'rgba(255,255,255,0.92)', lineHeight: 1.55, fontWeight: 500 }}>
                              {text}
                            </Typography>
                          </Box>
                        ))}
                      </Stack>
                    </Box>

                    {/* Emergency contacts */}
                    <Box sx={{ p: 2, borderRadius: '14px', background: 'rgba(255,255,255,0.12)', border: '1px solid rgba(255,255,255,0.2)' }}>
                      <Typography variant="subtitle2" sx={{ fontWeight: 700, mb: 1.25, color: '#fff' }}>
                        Emergency Contact
                      </Typography>
                      <Stack spacing={1}>
                        {HELPLINES.map(({ icon: Icon, label, value }) => (
                          <Box key={label} sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: 1 }}>
                            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                              <Icon sx={{ fontSize: 18, color: '#FFD54F' }} />
                              <Typography variant="body2" sx={{ color: 'rgba(255,255,255,0.92)' }}>{label}</Typography>
                            </Box>
                            <Typography variant="body2" sx={{ fontWeight: 800, fontSize: '1.05rem' }}>{value}</Typography>
                          </Box>
                        ))}
                      </Stack>
                    </Box>

                    {/* Confidential notice */}
                    <Box sx={{ display: 'flex', alignItems: 'flex-start', gap: 1.25, mt: 3, pt: 2.5, borderTop: '1px solid rgba(255,255,255,0.18)' }}>
                      <LockIcon sx={{ fontSize: 18, color: '#5BDCCA', mt: 0.25, flexShrink: 0 }} />
                      <Typography variant="caption" sx={{ color: 'rgba(255,255,255,0.8)', lineHeight: 1.6, fontWeight: 500 }}>
                        Your report is kept strictly confidential and is used only for emergency response
                        and coordination purposes.
                      </Typography>
                    </Box>
                  </Box>
                </Box>
              </Grid>

              {/* RIGHT PANEL — Reporting form */}
              <Grid size={{ xs: 12, md: 7 }}>
                <Card className="prd-glass" sx={{ borderRadius: '20px', height: '100%' }}>
                  <CardContent sx={{ p: { xs: 3, sm: 4, md: 4.5 } }}>
                    <Box sx={{ mb: 1 }}>
                      <Typography variant="h5" fontWeight={800} color="#0F172A" sx={{ mb: 0.5, fontSize: '1.45rem' }}>
                        Report a Disaster
                      </Typography>
                      <Typography variant="body2" color="text.secondary" sx={{ fontWeight: 500 }}>
                        No login required. Please provide accurate information to help responders.
                      </Typography>
                    </Box>

                    <Divider sx={{ mt: 2, mb: 3, borderColor: 'rgba(15,23,42,0.08)' }} />

                    <Box component="form" onSubmit={handleSubmit} noValidate>
                      {/* Section 1 — Incident Information */}
                      <SectionTitle
                        icon={CrisisAlertIcon}
                        title="Incident Information"
                        subtitle="What happened and how severe is it?"
                        accent="#2563EB"
                        first
                      />
                      <Grid container spacing={2}>
                        <Grid size={{ xs: 12, sm: 6 }}>
                          <TextField select fullWidth label="Disaster Type" name="disasterType" required
                            sx={inputSx}
                            error={!!errors.disasterType} helperText={errors.disasterType}
                            value={form.disasterType} onChange={handleChange}
                            slotProps={{
                              input: {
                                startAdornment: <InputAdornment position="start"><CategoryIcon fontSize="small" sx={{ color: '#2563EB' }} /></InputAdornment>,
                              },
                            }}
                          >
                            <MenuItem value="">Select type</MenuItem>
                            {DISASTER_TYPES.map(t => <MenuItem key={t} value={t}>{t}</MenuItem>)}
                          </TextField>
                        </Grid>
                        <Grid size={{ xs: 12, sm: 6 }}>
                          <TextField select fullWidth label="Severity" name="severity" required
                            sx={inputSx}
                            error={!!errors.severity} helperText={errors.severity}
                            value={form.severity} onChange={handleChange}
                            slotProps={{
                              input: {
                                startAdornment: <InputAdornment position="start"><WarningAmberIcon fontSize="small" sx={{ color: '#F59E0B' }} /></InputAdornment>,
                              },
                            }}
                          >
                            <MenuItem value="">Select severity</MenuItem>
                            {SEVERITIES.map(s => <MenuItem key={s} value={s}>{s}</MenuItem>)}
                          </TextField>
                        </Grid>
                        <Grid size={{ xs: 12 }}>
                          <TextField select fullWidth label="Priority" name="priority"
                            sx={inputSx}
                            value={form.priority} onChange={handleChange}
                            slotProps={{
                              input: {
                                startAdornment: <InputAdornment position="start"><PriorityHighIcon fontSize="small" sx={{ color: '#DC2626' }} /></InputAdornment>,
                              },
                            }}
                          >
                            {PRIORITIES.map(p => <MenuItem key={p} value={p}>{p.charAt(0) + p.slice(1).toLowerCase()}</MenuItem>)}
                          </TextField>
                        </Grid>
                      </Grid>

                      {/* Section 2 — Reporter Information */}
                      <SectionTitle
                        icon={BadgeIcon}
                        title="Reporter Information"
                        subtitle="How responders can reach you"
                        accent="#0D9488"
                      />
                      <Grid container spacing={2}>
                        <Grid size={{ xs: 12, sm: 6 }}>
                          <TextField fullWidth label="Your Name" name="reporterName" required
                            sx={inputSx}
                            error={!!errors.reporterName} helperText={errors.reporterName}
                            value={form.reporterName} onChange={handleChange}
                            slotProps={{
                              input: {
                                startAdornment: <InputAdornment position="start"><PersonIcon fontSize="small" sx={{ color: '#0D9488' }} /></InputAdornment>,
                              },
                            }}
                          />
                        </Grid>
                        <Grid size={{ xs: 12, sm: 6 }}>
                          <TextField fullWidth label="Mobile Number" name="reporterMobile" required
                            sx={inputSx}
                            error={!!errors.reporterMobile} helperText={errors.reporterMobile || 'e.g. +91-9876543210'}
                            value={form.reporterMobile} onChange={handleChange}
                            slotProps={{
                              input: {
                                startAdornment: <InputAdornment position="start"><PhoneIcon fontSize="small" sx={{ color: '#0D9488' }} /></InputAdornment>,
                              },
                            }}
                          />
                        </Grid>
                        <Grid size={{ xs: 12 }}>
                          <TextField fullWidth label="Email (optional)" name="reporterEmail" type="email"
                            sx={inputSx}
                            error={!!errors.reporterEmail} helperText={errors.reporterEmail || 'We will email you status updates'}
                            value={form.reporterEmail} onChange={handleChange}
                            slotProps={{
                              input: {
                                startAdornment: <InputAdornment position="start"><EmailIcon fontSize="small" sx={{ color: '#0D9488' }} /></InputAdornment>,
                              },
                            }}
                          />
                        </Grid>
                      </Grid>

                      {/* Section 3 — Location Information */}
                      <SectionTitle
                        icon={MyLocationIcon}
                        title="Location Information"
                        subtitle="Where did it happen?"
                        accent="#7C3AED"
                      />
                      <Grid container spacing={2}>
                        <Grid size={{ xs: 12 }}>
                          <TextField fullWidth label="Address" name="address" required
                            sx={inputSx}
                            error={!!errors.address} helperText={errors.address || 'Area, street, city'}
                            value={form.address} onChange={handleChange}
                            slotProps={{
                              input: {
                                startAdornment: <InputAdornment position="start"><LocationOnIcon fontSize="small" sx={{ color: '#7C3AED' }} /></InputAdornment>,
                              },
                            }}
                          />
                        </Grid>
                        <Grid size={{ xs: 12, sm: 6 }}>
                          <TextField fullWidth label="Latitude" name="latitude" type="number"
                            sx={inputSx}
                            error={!!errors.latitude} helperText={errors.latitude || 'Optional'}
                            value={form.latitude} onChange={handleChange}
                            slotProps={{
                              input: {
                                startAdornment: <InputAdornment position="start"><LocationOnIcon fontSize="small" sx={{ color: '#7C3AED' }} /></InputAdornment>,
                              },
                            }}
                          />
                        </Grid>
                        <Grid size={{ xs: 12, sm: 6 }}>
                          <TextField fullWidth label="Longitude" name="longitude" type="number"
                            sx={inputSx}
                            error={!!errors.longitude} helperText={errors.longitude || 'Optional'}
                            value={form.longitude} onChange={handleChange}
                            slotProps={{
                              input: {
                                startAdornment: <InputAdornment position="start"><LocationOnIcon fontSize="small" sx={{ color: '#7C3AED' }} /></InputAdornment>,
                              },
                            }}
                          />
                        </Grid>
                        <Grid size={{ xs: 12 }}>
                          <Button
                            variant="outlined" color="secondary"
                            startIcon={locating ? <CircularProgress size={16} color="inherit" /> : <MyLocationIcon />}
                            onClick={detectLocation} disabled={locating}
                            sx={{
                              borderRadius: '12px', textTransform: 'none', fontWeight: 700, py: 1,
                              borderColor: '#99F6E4', color: '#0F766E', backgroundColor: '#F0FDFA',
                              '&:hover': { borderColor: '#14B8A6', backgroundColor: '#CCFBF1' },
                            }}
                          >
                            {locating ? 'Detecting location...' : 'Share My GPS Location'}
                          </Button>
                        </Grid>
                      </Grid>

                      {/* Section 4 — Incident Description */}
                      <SectionTitle
                        icon={DescriptionIcon}
                        title="Incident Description"
                        subtitle="Tell responders what is happening"
                        accent="#DB2777"
                      />
                      <Grid container spacing={2}>
                        <Grid size={{ xs: 12 }}>
                          <TextField fullWidth label="Description" name="description" required multiline rows={4}
                            sx={inputSx}
                            error={!!errors.description} helperText={errors.description || 'Describe what is happening'}
                            value={form.description} onChange={handleChange}
                            slotProps={{
                              input: {
                                startAdornment: <InputAdornment position="start"><DescriptionIcon fontSize="small" sx={{ color: '#DB2777' }} /></InputAdornment>,
                              },
                            }}
                          />
                        </Grid>
                      </Grid>

                      {/* Evidence */}
                      <SectionTitle
                        icon={PhotoCameraIcon}
                        title="Evidence Attachments"
                        subtitle="Photos · Videos · PDFs"
                        accent="#EA580C"
                      />
                      <Grid container spacing={2}>
                        <Grid size={{ xs: 12 }}>
                          <Paper variant="outlined" sx={{ p: 2.5, borderRadius: '16px', backgroundColor: '#FAFAFA', borderColor: '#E2E8F0' }}>
                            <Typography variant="subtitle2" fontWeight={600} gutterBottom sx={{ color: '#0F172A' }}>
                              Attach Evidence (Photos · Videos · PDFs)
                            </Typography>
                            <FileUploadDropzone value={attachments} onChange={setAttachments} />
                          </Paper>
                        </Grid>
                      </Grid>

                      {/* Actions */}
                      <Box sx={{ mt: 3.5, display: 'flex', gap: 1.5, flexWrap: 'wrap' }}>
                        <Button type="submit" variant="contained" size="large" disabled={loading}
                          startIcon={loading ? <CircularProgress size={18} color="inherit" /> : <SendIcon />}
                          sx={{
                            flexGrow: 1, minWidth: { xs: '100%', sm: 220 },
                            borderRadius: '12px', textTransform: 'none', fontWeight: 700, py: 1.35,
                            background: 'linear-gradient(135deg, #2563EB 0%, #1D4ED8 100%)',
                            boxShadow: '0 12px 26px -8px rgba(37,99,235,0.6)',
                            transition: 'transform 0.18s ease, box-shadow 0.18s ease, background 0.18s ease',
                            '&:hover': {
                              background: 'linear-gradient(135deg, #3B82F6 0%, #2563EB 100%)',
                              boxShadow: '0 16px 32px -8px rgba(37,99,235,0.7)',
                              transform: 'translateY(-2px)',
                            },
                            '&:active': { transform: 'translateY(0) scale(0.99)' },
                            '&:disabled': { background: 'linear-gradient(135deg, #93C5FD, #60A5FA)', boxShadow: 'none' },
                          }}>
                          {loading ? 'Submitting...' : 'Submit Report'}
                        </Button>
                        <Button
                          variant="outlined" size="large" disabled={loading}
                          onClick={handleReset}
                          startIcon={<RestartAltIcon />}
                          sx={{
                            borderRadius: '12px', textTransform: 'none', fontWeight: 700, py: 1.35,
                            borderColor: '#CBD5E1', color: '#334155',
                            '&:hover': { borderColor: '#2563EB', color: '#1D4ED8', backgroundColor: 'rgba(37,99,235,0.06)' },
                          }}
                        >
                          Reset
                        </Button>
                      </Box>
                    </Box>
                  </CardContent>
                </Card>
              </Grid>
            </Grid>
          )}

          {/* Footer actions */}
          <Box sx={{ mt: 3, display: 'flex', justifyContent: 'center', gap: 1.5, flexWrap: 'wrap' }}>
            <Button
              size="small" color="inherit"
              startIcon={<TrackChangesIcon fontSize="small" />}
              onClick={() => navigate('/track-report')}
              sx={{ color: '#334155', fontWeight: 600, textTransform: 'none', '&:hover': { bgcolor: 'rgba(37,99,235,0.08)' } }}
            >
              Track a submitted report
            </Button>
            <Button
              size="small" color="inherit"
              startIcon={<LoginIcon fontSize="small" />}
              onClick={() => navigate('/login')}
              sx={{ color: '#334155', fontWeight: 600, textTransform: 'none', '&:hover': { bgcolor: 'rgba(37,99,235,0.08)' } }}
            >
              Authorized Personnel Login
            </Button>
          </Box>
          <Box sx={{ mt: 1.5, textAlign: 'center', color: '#64748B' }}>
            <Typography variant="caption" sx={{ fontWeight: 500 }}>
              Emergency Helpline: <b style={{ color: '#1D4ED8' }}>112</b> · National Disaster Helpline: <b style={{ color: '#1D4ED8' }}>1078</b>
            </Typography>
          </Box>
          <Box sx={{ mt: 1.5, display: 'flex', justifyContent: 'center', alignItems: 'center', gap: 0.75 }}>
            <LocalPoliceIcon sx={{ fontSize: 14, color: '#94A3B8' }} />
            <Typography variant="caption" color="text.secondary">
              Official Government Emergency Reporting Service
            </Typography>
          </Box>
        </Box>
      </Box>
    </ThemeProvider>
  )
}
