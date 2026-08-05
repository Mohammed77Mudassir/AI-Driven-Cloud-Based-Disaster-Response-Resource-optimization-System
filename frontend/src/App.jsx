import { lazy, Suspense } from 'react'
import { Routes, Route, Navigate } from 'react-router-dom'
import { useAuth } from './context/AuthContext'
import { Box, Typography, CircularProgress } from '@mui/material'
import SecurityIcon from '@mui/icons-material/Security'
import ProtectedRoute from './components/ProtectedRoute'
import ErrorBoundary from './components/ErrorBoundary'

const Login = lazy(() => import('./pages/Login'))
const Register = lazy(() => import('./pages/Register'))
const ForgotPassword = lazy(() => import('./pages/ForgotPassword'))
const ResetPassword = lazy(() => import('./pages/ResetPassword'))
const PublicReport = lazy(() => import('./pages/PublicReport'))
const PublicTrack = lazy(() => import('./pages/PublicTrack'))
const UserDashboard = lazy(() => import('./pages/UserDashboard'))
const AdminDashboard = lazy(() => import('./pages/AdminDashboard'))
const ReportDisaster = lazy(() => import('./pages/ReportDisaster'))
const DisasterList = lazy(() => import('./pages/DisasterList'))
const DisasterDetail = lazy(() => import('./pages/DisasterDetail'))
const AIInsights = lazy(() => import('./pages/AIInsights'))
const MapPage = lazy(() => import('./pages/MapPage'))
const DroneMonitoring = lazy(() => import('./pages/DroneMonitoring'))
const HospitalManagement = lazy(() => import('./pages/HospitalManagement'))
const ShelterManagement = lazy(() => import('./pages/ShelterManagement'))
const VolunteerManagement = lazy(() => import('./pages/VolunteerManagement'))
const ResourceManagement = lazy(() => import('./pages/ResourceManagement'))
const NotificationCenter = lazy(() => import('./pages/NotificationCenter'))
const AuditLogPage = lazy(() => import('./pages/AuditLogPage'))
const AdvancedAnalytics = lazy(() => import('./pages/AdvancedAnalytics'))
const UserManagement = lazy(() => import('./pages/UserManagement'))
const RescueTeamManagement = lazy(() => import('./pages/RescueTeamManagement'))
const RouteOptimization = lazy(() => import('./pages/RouteOptimization'))
const CaseStudyAnalysis = lazy(() => import('./pages/CaseStudyAnalysis'))
const AdminControlCenter = lazy(() => import('./pages/AdminControlCenter'))
const EmergencyOperationsCenter = lazy(() => import('./pages/EmergencyOperationsCenter'))
const UserProfile = lazy(() => import('./pages/UserProfile'))

const routeErrorHandler = (error, errorInfo) => {
  console.error('[RouteErrorBoundary]', error, errorInfo)
}

function RouteFallback() {
  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', justifyContent: 'center', alignItems: 'center', height: '100vh', gap: 2 }}>
      <Box sx={{ position: 'relative', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
        <CircularProgress size={60} thickness={3} />
        <SecurityIcon sx={{ position: 'absolute', fontSize: 22, color: 'primary.main' }} />
      </Box>
      <Typography variant="body2" color="text.secondary">Loading module…</Typography>
    </Box>
  )
}

export default function App() {
  const { user, loading } = useAuth()

  if (loading) {
    return (
      <Box sx={{ display: 'flex', flexDirection: 'column', justifyContent: 'center', alignItems: 'center', height: '100vh', gap: 2 }}>
        <Box sx={{ position: 'relative', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
          <CircularProgress size={72} thickness={3} />
          <SecurityIcon sx={{ position: 'absolute', fontSize: 26, color: 'primary.main' }} />
        </Box>
        <Typography variant="body2" color="text.secondary">Initializing Emergency Operations Center…</Typography>
      </Box>
    )
  }

  const lazyPage = (element) => (
    <ErrorBoundary onError={routeErrorHandler}>
      <Suspense fallback={<RouteFallback />}>{element}</Suspense>
    </ErrorBoundary>
  )

  return (
    <Routes>
      {/* Public, no authentication required */}
      <Route path="/public-report" element={lazyPage(<PublicReport />)} />
      <Route path="/track-report" element={lazyPage(<PublicTrack />)} />
      <Route path="/forgot-password" element={user ? <Navigate to="/dashboard" /> : lazyPage(<ForgotPassword />)} />
      <Route path="/reset-password" element={lazyPage(<ResetPassword />)} />

      {/* Authentication */}
      <Route path="/login" element={user ? <Navigate to="/dashboard" /> : lazyPage(<Login />)} />
      <Route path="/register" element={user ? <Navigate to="/dashboard" /> : lazyPage(<Register />)} />

      {/* Core authorized pages */}
      <Route path="/dashboard" element={<ProtectedRoute permission="DASHBOARD_VIEW">{lazyPage(<UserDashboard />)}</ProtectedRoute>} />
      <Route path="/disasters" element={<ProtectedRoute permission="DISASTER_VIEW">{lazyPage(<DisasterList />)}</ProtectedRoute>} />
      <Route path="/disasters/:id" element={<ProtectedRoute permission="DISASTER_VIEW">{lazyPage(<DisasterDetail />)}</ProtectedRoute>} />
      <Route path="/report" element={<ProtectedRoute permission="DISASTER_CREATE">{lazyPage(<ReportDisaster />)}</ProtectedRoute>} />
      <Route path="/admin/disasters" element={<ProtectedRoute permission="DISASTER_VIEW">{lazyPage(<AdminDashboard />)}</ProtectedRoute>} />
      <Route path="/admin/eoc" element={<ProtectedRoute permission="SETTINGS_MANAGE">{lazyPage(<EmergencyOperationsCenter />)}</ProtectedRoute>} />
      <Route path="/ai-insights" element={<ProtectedRoute permission="AI_VIEW">{lazyPage(<AIInsights />)}</ProtectedRoute>} />
      <Route path="/map" element={<ProtectedRoute permission="MAP_VIEW">{lazyPage(<MapPage />)}</ProtectedRoute>} />
      <Route path="/notifications" element={<ProtectedRoute permission="NOTIFICATION_VIEW">{lazyPage(<NotificationCenter />)}</ProtectedRoute>} />
      <Route path="/profile" element={<ProtectedRoute permission="PROFILE_VIEW">{lazyPage(<UserProfile />)}</ProtectedRoute>} />
      <Route path="/route-optimization" element={<ProtectedRoute permission="ROUTE_VIEW">{lazyPage(<RouteOptimization />)}</ProtectedRoute>} />
      <Route path="/case-studies" element={<ProtectedRoute permission="CASE_STUDY_VIEW">{lazyPage(<CaseStudyAnalysis />)}</ProtectedRoute>} />

      {/* Management pages */}
      <Route path="/drones" element={<ProtectedRoute permission="DRONE_MANAGE">{lazyPage(<DroneMonitoring />)}</ProtectedRoute>} />
      <Route path="/hospitals" element={<ProtectedRoute permission="HOSPITAL_MANAGE">{lazyPage(<HospitalManagement />)}</ProtectedRoute>} />
      <Route path="/shelters" element={<ProtectedRoute permission="SHELTER_MANAGE">{lazyPage(<ShelterManagement />)}</ProtectedRoute>} />
      <Route path="/volunteers" element={<ProtectedRoute permission="VOLUNTEER_MANAGE">{lazyPage(<VolunteerManagement />)}</ProtectedRoute>} />
      <Route path="/resources" element={<ProtectedRoute permission="RESOURCE_MANAGE">{lazyPage(<ResourceManagement />)}</ProtectedRoute>} />
      <Route path="/users" element={<ProtectedRoute permission="USER_MANAGE">{lazyPage(<UserManagement />)}</ProtectedRoute>} />
      <Route path="/rescue-teams" element={<ProtectedRoute permission="RESCUE_TEAM_MANAGE">{lazyPage(<RescueTeamManagement />)}</ProtectedRoute>} />
      <Route path="/audit-logs" element={<ProtectedRoute permission="AUDIT_VIEW">{lazyPage(<AuditLogPage />)}</ProtectedRoute>} />
      <Route path="/analytics" element={<ProtectedRoute permission="ANALYTICS_VIEW">{lazyPage(<AdvancedAnalytics />)}</ProtectedRoute>} />
      <Route path="/admin-control" element={<ProtectedRoute permission="SETTINGS_MANAGE">{lazyPage(<AdminControlCenter />)}</ProtectedRoute>} />
      <Route path="/settings" element={<ProtectedRoute permission="SETTINGS_MANAGE">{lazyPage(<AdminControlCenter />)}</ProtectedRoute>} />

      <Route path="/" element={user ? <Navigate to="/dashboard" /> : <Navigate to="/login" />} />
      <Route path="*" element={
        <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', height: '100vh', gap: 1.5, textAlign: 'center', px: 2 }}>
          <Typography variant="h1" sx={{ fontSize: { xs: '4rem', md: '6rem' }, fontWeight: 900, color: 'primary.main', lineHeight: 1 }}>
            404
          </Typography>
          <Typography variant="h6" sx={{ fontWeight: 700 }}>Sector Not Found</Typography>
          <Typography variant="body2" color="text.secondary">The command you requested does not exist in this system.</Typography>
        </Box>
      } />
    </Routes>
  )
}
