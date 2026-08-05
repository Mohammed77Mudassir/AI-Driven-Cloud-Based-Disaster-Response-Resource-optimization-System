import { Box, Card, CardContent, Typography, useTheme } from '@mui/material'
import SecurityIcon from '@mui/icons-material/Security'
import EmergencyIcon from '@mui/icons-material/Emergency'
import GridOnIcon from '@mui/icons-material/GridOn'

const GRADIENT_CSS = `
@keyframes dmsGradientShift {
  0%   { background-position: 0% 50%; }
  50%  { background-position: 100% 50%; }
  100% { background-position: 0% 50%; }
}
`

export default function AuthLayout({ title, subtitle, maxWidth = 440, children, footer }) {
  const theme = useTheme()
  const isDark = theme.palette.mode === 'dark'

  return (
    <>
      <style>{GRADIENT_CSS}</style>
      <Box sx={{
        minHeight: '100vh',
        position: 'relative',
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        p: 3,
      }}>
        <Box
          aria-hidden
          sx={{
            position: 'fixed', inset: 0, zIndex: 0,
            background: isDark
              ? 'linear-gradient(-45deg, #050B16, #0A1220, #0E1B30, #123A3B, #0E1B30, #050B16)'
              : 'linear-gradient(-45deg, #0A3560, #0F4C81, #1A6BB5, #00897B, #0F4C81, #0A3560)',
            backgroundSize: '400% 400%',
            animation: 'dmsGradientShift 18s ease infinite',
          }}
        >
          <Box sx={{
            position: 'absolute', inset: 0,
            backgroundImage: isDark
              ? 'linear-gradient(rgba(77,163,255,0.05) 1px, transparent 1px), linear-gradient(90deg, rgba(77,163,255,0.05) 1px, transparent 1px)'
              : 'linear-gradient(rgba(255,255,255,0.06) 1px, transparent 1px), linear-gradient(90deg, rgba(255,255,255,0.06) 1px, transparent 1px)',
            backgroundSize: '44px 44px',
          }} />
        </Box>

        <Box sx={{ position: 'relative', zIndex: 1, textAlign: 'center', mb: 3, maxWidth: 560, px: 2 }}>
          <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 1.5, mb: 1 }}>
            <Box sx={{
              width: 58, height: 58, borderRadius: '50%', display: 'flex', alignItems: 'center',
              justifyContent: 'center', bgcolor: 'rgba(255,255,255,0.14)',
              border: '1.5px solid rgba(255,255,255,0.45)', backdropFilter: 'blur(6px)'
            }}>
              <SecurityIcon sx={{ fontSize: 30, color: '#fff' }} />
            </Box>
          </Box>
          <Typography variant="h4" sx={{ color: '#fff', fontWeight: 800, letterSpacing: '0.5px', textShadow: '0 2px 12px rgba(0,0,0,0.35)' }}>
            National Disaster Management
          </Typography>
          <Typography variant="subtitle1" sx={{ color: 'rgba(255,255,255,0.92)', fontWeight: 500, mt: 0.5 }}>
            Emergency Operations Command Center
          </Typography>
        </Box>

        <Card
          sx={{
            position: 'relative', zIndex: 1, maxWidth, width: '100%', borderRadius: 4,
            background: isDark ? 'rgba(16,27,46,0.88)' : 'rgba(255,255,255,0.88)',
            backdropFilter: 'blur(18px)', WebkitBackdropFilter: 'blur(18px)',
            border: '1px solid rgba(255,255,255,0.5)',
            boxShadow: isDark ? '0 20px 60px rgba(0,0,0,0.6)' : '0 20px 60px rgba(4,28,51,0.45)',
          }}
        >
          <CardContent sx={{ p: { xs: 3, sm: 4 } }}>
            <Box sx={{ textAlign: 'center', mb: 2.5 }}>
              <Typography variant="h5" sx={{ fontWeight: 800, color: 'primary.main' }}>
                {title}
              </Typography>
              {subtitle && (
                <Typography variant="body2" color="text.secondary" sx={{ mt: 0.5 }}>
                  {subtitle}
                </Typography>
              )}
            </Box>
            {children}
          </CardContent>
        </Card>

        {footer}
      </Box>
    </>
  )
}

export function AuthFooter({ helpline = true }) {
  return (
    <Box sx={{ position: 'relative', zIndex: 1, mt: 3, display: 'flex', alignItems: 'center', gap: 1, color: 'rgba(255,255,255,0.92)' }}>
      <EmergencyIcon fontSize="small" />
      <Typography variant="caption">Emergency Helpline: <b>112</b> · National Disaster Helpline: <b>1078</b></Typography>
    </Box>
  )
}
