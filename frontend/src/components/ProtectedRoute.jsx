import { Navigate, useLocation } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { Box, CircularProgress, Typography } from '@mui/material'
import SecurityIcon from '@mui/icons-material/Security'

/**
 * Route guard for authorized personnel.
 *
 * - No logged-in user         -> redirect to /login
 * - `adminOnly` and not ADMIN -> redirect to /dashboard
 * - `permission` required but not granted -> redirect to /dashboard (403)
 */
export default function ProtectedRoute({ children, adminOnly = false, permission = null }) {
  const { user, loading, hasPermission } = useAuth()
  const location = useLocation()

  if (loading) {
    return (
      <Box sx={{ display: 'flex', flexDirection: 'column', justifyContent: 'center', alignItems: 'center', height: '100vh', gap: 2 }}>
        <Box sx={{ position: 'relative', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
          <CircularProgress size={64} thickness={3} />
          <SecurityIcon sx={{ position: 'absolute', fontSize: 24, color: 'primary.main' }} />
        </Box>
        <Typography variant="body2" color="text.secondary">Verifying credentials…</Typography>
      </Box>
    )
  }
  if (!user) return <Navigate to="/login" replace state={{ from: location.pathname }} />
  if (adminOnly && user.role !== 'ADMIN') return <Navigate to="/dashboard" replace />
  if (permission && !hasPermission(permission)) return <Navigate to="/dashboard" replace />

  return children
}

export function AdminRoute({ children }) {
  return <ProtectedRoute adminOnly>{children}</ProtectedRoute>
}
