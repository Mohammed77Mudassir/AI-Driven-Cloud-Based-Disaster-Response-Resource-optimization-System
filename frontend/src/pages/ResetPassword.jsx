import { useState } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { authApi } from '../services/api'
import toast from 'react-hot-toast'
import {
  Box, TextField, Button, Typography, Alert, CircularProgress,
  InputAdornment, IconButton
} from '@mui/material'
import LockResetIcon from '@mui/icons-material/LockReset'
import LockOutlinedIcon from '@mui/icons-material/LockOutlined'
import VisibilityIcon from '@mui/icons-material/Visibility'
import VisibilityOffIcon from '@mui/icons-material/VisibilityOff'
import AuthLayout from '../components/AuthLayout'

export default function ResetPassword() {
  const [searchParams] = useSearchParams()
  const token = searchParams.get('token') || ''
  const [password, setPassword] = useState('')
  const [confirm, setConfirm] = useState('')
  const [show, setShow] = useState(false)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [done, setDone] = useState(false)
  const navigate = useNavigate()

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    if (password !== confirm) {
      setError('Passwords do not match')
      return
    }
    if (password.length < 6) {
      setError('Password must be at least 6 characters')
      return
    }
    setLoading(true)
    try {
      await authApi.resetPassword(token, password)
      setDone(true)
      toast.success('Password reset successfully')
      setTimeout(() => navigate('/login'), 1500)
    } catch (err) {
      const msg = err.response?.data?.error || 'Invalid or expired reset link'
      setError(msg)
      toast.error(msg)
    } finally {
      setLoading(false)
    }
  }

  return (
    <AuthLayout
      title="Set New Password"
      subtitle="Choose a strong new password for your account."
    >
      {done ? (
        <Alert severity="success">Password has been reset. Redirecting to login...</Alert>
      ) : (
        <Box component="form" onSubmit={handleSubmit}>
          {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
          <TextField
            fullWidth size="small" label="New Password" required margin="normal"
            type={show ? 'text' : 'password'} value={password}
            onChange={e => setPassword(e.target.value)}
            autoComplete="new-password"
            slotProps={{
              input: {
                startAdornment: <InputAdornment position="start"><LockOutlinedIcon fontSize="small" /></InputAdornment>,
                endAdornment: (
                  <InputAdornment position="end">
                    <IconButton size="small" onClick={() => setShow(v => !v)} edge="end" aria-label="toggle password visibility">
                      {show ? <VisibilityOffIcon fontSize="small" /> : <VisibilityIcon fontSize="small" />}
                    </IconButton>
                  </InputAdornment>
                )
              }
            }}
          />
          <TextField
            fullWidth size="small" label="Confirm New Password" required margin="normal"
            type={show ? 'text' : 'password'} value={confirm}
            onChange={e => setConfirm(e.target.value)}
            autoComplete="new-password"
          />
          <Button type="submit" fullWidth variant="contained" size="large" disabled={loading}
            startIcon={<LockResetIcon />} sx={{ mt: 2.5, py: 1.3 }}>
            {loading ? <CircularProgress size={22} color="inherit" /> : 'Reset Password'}
          </Button>
        </Box>
      )}
    </AuthLayout>
  )
}
