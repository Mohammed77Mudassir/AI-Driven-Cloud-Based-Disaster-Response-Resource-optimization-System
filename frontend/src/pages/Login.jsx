import { useState } from 'react'
import { useNavigate, useLocation } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import toast from 'react-hot-toast'
import {
  Box, TextField, Button, Typography, Checkbox, FormControlLabel,
  Alert, CircularProgress, InputAdornment, IconButton, ToggleButton, ToggleButtonGroup
} from '@mui/material'
import LockOutlinedIcon from '@mui/icons-material/LockOutlined'
import PersonIcon from '@mui/icons-material/Person'
import AdminPanelSettingsIcon from '@mui/icons-material/AdminPanelSettings'
import VisibilityIcon from '@mui/icons-material/Visibility'
import VisibilityOffIcon from '@mui/icons-material/VisibilityOff'
import ReportProblemIcon from '@mui/icons-material/ReportProblem'
import BadgeIcon from '@mui/icons-material/Badge'
import TrackChangesIcon from '@mui/icons-material/TrackChanges'
import SecurityIcon from '@mui/icons-material/Security'
import EmergencyIcon from '@mui/icons-material/Emergency'
import PsychologyIcon from '@mui/icons-material/Psychology'
import RadarIcon from '@mui/icons-material/Radar'
import SensorsIcon from '@mui/icons-material/Sensors'
import MyLocationIcon from '@mui/icons-material/MyLocation'
import VerifiedUserIcon from '@mui/icons-material/VerifiedUser'
import SpeedIcon from '@mui/icons-material/Speed'
import PublicIcon from '@mui/icons-material/Public'
import './Login.css'

const goHomeFor = (role) => role === 'ADMIN' ? '/admin/disasters' : '/dashboard'

const FEATURES = [
  {
    icon: PsychologyIcon,
    title: 'AI-Driven Early Warning',
    desc: 'Predictive models flag emerging threats before they escalate.',
  },
  {
    icon: RadarIcon,
    title: 'Real-Time Disaster Radar',
    desc: 'Live monitoring of incidents, assets and responders.',
  },
  {
    icon: SensorsIcon,
    title: 'IoT & Drone Integration',
    desc: 'Field sensors and drone feeds stream into one command view.',
  },
  {
    icon: MyLocationIcon,
    title: 'Coordinated Response',
    desc: 'Unified mission control across agencies and rescue teams.',
  },
]

const CAPABILITIES = [
  { icon: VerifiedUserIcon, label: 'Verified Access' },
  { icon: SpeedIcon, label: 'Real-time Intel' },
  { icon: PublicIcon, label: 'Nationwide Coverage' },
]

// Decorative rising particles — pure UI, no data.
const PARTICLES = [...Array(14)].map((_, i) => ({
  left: (i * 7.3 + 3) % 100,
  delay: (i % 6) * 0.9,
  duration: 10 + (i % 5) * 2.6,
  size: 2 + (i % 3),
}))

function DisasterIllustration() {
  return (
    <svg className="login-illustration" viewBox="0 0 480 260" fill="none" aria-hidden="true">
      <defs>
        <linearGradient id="illRadar" x1="0" y1="0" x2="1" y2="1">
          <stop offset="0%" stopColor="#4DA3FF" />
          <stop offset="100%" stopColor="#2BC8B2" />
        </linearGradient>
        <linearGradient id="illShield" x1="0" y1="0" x2="0" y2="1">
          <stop offset="0%" stopColor="#7CBDFF" />
          <stop offset="100%" stopColor="#2BC8B2" />
        </linearGradient>
        <radialGradient id="illGlow" cx="50%" cy="50%" r="50%">
          <stop offset="0%" stopColor="rgba(77,163,255,0.35)" />
          <stop offset="100%" stopColor="rgba(77,163,255,0)" />
        </radialGradient>
      </defs>

      {/* backdrop glow */}
      <circle cx="240" cy="120" r="118" fill="url(#illGlow)" />

      {/* base grid */}
      <g stroke="rgba(124,189,249,0.14)" strokeWidth="1">
        <path d="M60 60H420M60 100H420M60 140H420M60 180H420M60 220H420" />
        <path d="M60 60V220M100 60V220M140 60V220M180 60V220M220 60V220M260 60V220M300 60V220M340 60V220M380 60V220" />
      </g>

      {/* radar rings */}
      <circle cx="240" cy="130" r="34" stroke="rgba(124,189,249,0.28)" strokeWidth="1.4" strokeDasharray="4 6" />
      <circle cx="240" cy="130" r="64" stroke="rgba(124,189,249,0.18)" strokeWidth="1.2" />
      <circle cx="240" cy="130" r="96" stroke="rgba(124,189,249,0.12)" strokeWidth="1" />

      {/* radar sweep */}
      <g className="login-ill__sweep">
        <path d="M240 130 L240 34 A96 96 0 0 1 335 74 Z" fill="url(#illRadar)" opacity="0.16" />
        <path d="M240 130 L240 34" stroke="rgba(124,189,249,0.6)" strokeWidth="2" strokeLinecap="round" />
      </g>

      {/* radar blips */}
      <circle className="login-ill__blip" cx="282" cy="96" r="4" fill="#5BDCCA" />
      <circle className="login-ill__blip login-ill__blip--2" cx="204" cy="160" r="4" fill="#4DA3FF" />
      <circle className="login-ill__blip login-ill__blip--3" cx="168" cy="108" r="3.4" fill="#7CBDFF" />
      <circle className="login-ill__blip" cx="300" cy="170" r="3.2" fill="#F9A825" />

      {/* central shield */}
      <g className="login-ill__float">
        <circle cx="240" cy="130" r="30" fill="rgba(77,163,255,0.14)" stroke="rgba(124,189,249,0.4)" strokeWidth="1.2" />
        <path
          d="M240 108l14 5.6v9.4c0 10.4-6.1 17.6-14 20.2-7.9-2.6-14-9.8-14-20.2v-9.4l14-5.6z"
          fill="url(#illShield)"
        />
        <path d="M234.5 130.5l4 4 7.5-8.2" stroke="#05121F" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round" fill="none" />
      </g>

      {/* satellite / drone corner */}
      <g className="login-ill__float login-ill__float--2" transform="translate(352 40)">
        <rect x="-14" y="-6" width="28" height="16" rx="4" fill="rgba(43,200,178,0.9)" />
        <rect x="-30" y="-2" width="16" height="8" rx="2" fill="rgba(43,200,178,0.55)" />
        <rect x="14" y="-2" width="16" height="8" rx="2" fill="rgba(43,200,178,0.55)" />
        <rect x="-2" y="-22" width="4" height="14" rx="2" fill="rgba(43,200,178,0.7)" />
        <rect x="-2" y="-22" width="24" height="4" rx="2" fill="rgba(43,200,178,0.7)" transform="rotate(-35 -2 -22)" />
      </g>

      {/* signal waves from satellite */}
      <g transform="translate(352 40)">
        <circle className="login-ill__wave" cx="0" cy="0" r="18" stroke="rgba(43,200,178,0.6)" strokeWidth="1.6" />
        <circle className="login-ill__wave login-ill__wave--2" cx="0" cy="0" r="18" stroke="rgba(43,200,178,0.45)" strokeWidth="1.6" />
        <circle className="login-ill__wave login-ill__wave--3" cx="0" cy="0" r="18" stroke="rgba(43,200,178,0.3)" strokeWidth="1.6" />
      </g>

      {/* floating marker pins */}
      <g className="login-ill__float">
        <path d="M120 190c0-8 6.5-14.5 14.5-14.5S149 182 149 190c0 7.5-14.5 20-14.5 20S120 197.5 120 190z" fill="rgba(249,168,37,0.9)" />
        <circle cx="134.5" cy="189" r="5" fill="#05121F" />
      </g>
      <g className="login-ill__float login-ill__float--2">
        <path d="M330 186c0-7.2 5.8-13 13-13s13 5.8 13 13c0 6.7-13 18-13 18s-13-11.3-13-18z" fill="rgba(91,220,202,0.9)" />
        <circle cx="343" cy="185" r="4.6" fill="#05121F" />
      </g>
    </svg>
  )
}

export default function Login() {
  const [form, setForm] = useState({ username: '', password: '', rememberMe: false })
  const [role, setRole] = useState('authorized')
  const [showPassword, setShowPassword] = useState(false)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const { login } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      const data = await login(form)
      const redirect = location.state?.from || goHomeFor(data.role)
      navigate(redirect)
    } catch (err) {
      const msg = err.response?.data?.error || err.response?.data?.message || 'Login failed'
      setError(msg)
      toast.error(msg)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="login-page">
      <div className="login-bg" aria-hidden="true">
        <div className="login-bg__grid" />
        <div className="login-blob login-blob--one" />
        <div className="login-blob login-blob--two" />
        <div className="login-blob login-blob--three" />
        <div className="login-particles">
          {PARTICLES.map((p, i) => (
            <span
              key={i}
              className="login-particle"
              style={{
                left: `${p.left}%`,
                width: `${p.size}px`,
                height: `${p.size}px`,
                animationDelay: `${p.delay}s`,
                animationDuration: `${p.duration}s`,
              }}
            />
          ))}
        </div>
        <div className="login-horizon" />
      </div>

      <div className="login-shell">
        <aside className="login-brand">
          <div className="login-brand__inner">
            <div className="login-brand__top">
              <div className="login-brand__badge">
                <SecurityIcon />
              </div>
              <span className="login-brand__status">
                <span className="login-brand__status-dot" aria-hidden="true" />
                All Systems Operational
              </span>
            </div>
            <div className="login-brand__eyebrow">National Emergency Operations Center</div>
            <h1 className="login-brand__title">
              National Disaster <em>Management</em>
            </h1>
            <p className="login-brand__subtitle">
              AI-powered command platform for situational awareness and coordinated emergency response.
            </p>

            <div className="login-brand__illustration">
              <DisasterIllustration />
            </div>

            <ul className="login-brand__features">
              {FEATURES.map(({ icon: Icon, title, desc }) => (
                <li key={title}>
                  <span className="login-brand__feature-icon">
                    <Icon fontSize="small" />
                  </span>
                  <div>
                    <b>{title}</b>
                    <span>{desc}</span>
                  </div>
                </li>
              ))}
            </ul>

            <div className="login-brand__chips">
              {CAPABILITIES.map(({ icon: Icon, label }) => (
                <span className="login-brand__chip" key={label}>
                  <Icon fontSize="small" />
                  {label}
                </span>
              ))}
            </div>

            <div className="login-brand__helpline">
              <EmergencyIcon />
              <span>
                Emergency Helpline: <b>112</b> · National Disaster Helpline: <b>1078</b>
              </span>
            </div>
          </div>
        </aside>

        <main className="login-panel">
          <div className={`login-card${error ? ' login-card--error' : ''}`}>
            <div className="login-card__glow" aria-hidden="true" />
            <div className="login-card__orb login-card__orb--one" aria-hidden="true" />
            <div className="login-card__orb login-card__orb--two" aria-hidden="true" />

            <header className="login-card__header">
              <span className="login-card__eyebrow">
                <LockOutlinedIcon fontSize="inherit" />
                Secure Access Portal
              </span>
              <h2>Welcome Back</h2>
              <p>Restricted to government &amp; emergency response personnel</p>
            </header>

            <ToggleButtonGroup
              exclusive fullWidth size="small" value={role} onChange={(_, v) => v && setRole(v)}
              sx={{ mb: 2.5 }}
              aria-label="Account type"
            >
              <ToggleButton value="admin" sx={{ py: 1, fontWeight: 600 }}>
                <AdminPanelSettingsIcon fontSize="small" sx={{ mr: 0.75 }} /> Admin
              </ToggleButton>
              <ToggleButton value="authorized" sx={{ py: 1, fontWeight: 600 }}>
                <BadgeIcon fontSize="small" sx={{ mr: 0.75 }} /> Authorized Personnel
              </ToggleButton>
            </ToggleButtonGroup>

            {error && (
              <Alert severity="error" sx={{ mb: 2 }} role="alert" icon={<ReportProblemIcon fontSize="small" />}>
                {error}
              </Alert>
            )}

            <Box component="form" onSubmit={handleSubmit} noValidate aria-label="Sign in form">
              <TextField
                fullWidth size="small" label="Username or Email" required margin="normal"
                value={form.username}
                onChange={e => setForm({ ...form, username: e.target.value })}
                autoComplete="username"
                slotProps={{
                  input: {
                    startAdornment: <InputAdornment position="start"><PersonIcon fontSize="small" /></InputAdornment>
                  }
                }}
              />
              <TextField
                fullWidth size="small" label="Password" required margin="normal"
                type={showPassword ? 'text' : 'password'}
                value={form.password}
                onChange={e => setForm({ ...form, password: e.target.value })}
                autoComplete="current-password"
                slotProps={{
                  input: {
                    startAdornment: <InputAdornment position="start"><LockOutlinedIcon fontSize="small" /></InputAdornment>,
                    endAdornment: (
                      <InputAdornment position="end">
                        <IconButton
                          aria-label={showPassword ? 'Hide password' : 'Show password'}
                          size="small"
                          onClick={() => setShowPassword(v => !v)}
                          edge="end"
                        >
                          {showPassword ? <VisibilityOffIcon fontSize="small" /> : <VisibilityIcon fontSize="small" />}
                        </IconButton>
                      </InputAdornment>
                    )
                  }
                }}
              />

              <div className="login-options">
                <FormControlLabel
                  control={
                    <Checkbox size="small" checked={form.rememberMe}
                      onChange={e => setForm({ ...form, rememberMe: e.target.checked })} />
                  }
                  label={<Typography variant="body2">Remember Me</Typography>}
                />
              </div>

              <Button type="submit" fullWidth variant="contained" size="large" disabled={loading}
                className="login-submit">
                {loading ? (
                  <>
                    <CircularProgress size={20} color="inherit" sx={{ mr: 1 }} />
                    Signing in…
                  </>
                ) : (
                  <>
                    Sign In Securely
                    <span className="login-submit__arrow" aria-hidden="true">→</span>
                  </>
                )}
              </Button>
            </Box>

            <Button
              fullWidth size="large"
              variant="contained"
              startIcon={<ReportProblemIcon />}
              onClick={() => navigate('/public-report')}
              className="login-public-report"
            >
              Report Disaster (No Login Required)
            </Button>
            <Button
              size="small"
              startIcon={<TrackChangesIcon fontSize="small" />}
              onClick={() => navigate('/track-report')}
              className="login-track-report"
            >
              Track a submitted report
            </Button>
          </div>

          <footer className="login-panel__footer">
            <EmergencyIcon />
            <span>Emergency Helpline: <b>112</b> · National Disaster Helpline: <b>1078</b></span>
          </footer>
        </main>
      </div>
    </div>
  )
}
