import { Box, Card, CardContent, Typography, IconButton, Tooltip } from '@mui/material'
import MoreVertIcon from '@mui/icons-material/MoreVert'

export default function SectionCard({ title, subtitle, action, actions, children, icon: Icon, spacing = 0, sx = {} }) {
  const header = title || actions || action
  return (
    <Card sx={{ height: '100%', display: 'flex', flexDirection: 'column', ...sx }}>
      {header && (
        <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', px: 2.5, pt: 2.25, pb: 1.25 }}>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.25, minWidth: 0 }}>
            {Icon && (
              <Box
                aria-hidden
                sx={{
                  width: 36, height: 36, borderRadius: 2, display: 'flex', alignItems: 'center',
                  justifyContent: 'center', bgcolor: 'action.hover', color: 'primary.main', flexShrink: 0,
                }}
              >
                <Icon sx={{ fontSize: 20 }} />
              </Box>
            )}
            <Box sx={{ minWidth: 0 }}>
              {title && <Typography variant="h6" noWrap>{title}</Typography>}
              {subtitle && <Typography variant="caption" color="text.secondary" noWrap>{subtitle}</Typography>}
            </Box>
          </Box>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            {action}
            {actions?.map((a, i) => (
              <Tooltip key={i} title={a.title || ''}>
                <IconButton size="small" aria-label={a.title || 'action'} onClick={a.onClick}>
                  {a.icon || <MoreVertIcon fontSize="small" />}
                </IconButton>
              </Tooltip>
            ))}
          </Box>
        </Box>
      )}
      <CardContent sx={{ pt: header ? 0 : 2.5, pb: 2.5, px: 2.5, flex: 1, '&:last-child': { pb: 2.5 } }}>
        {children}
      </CardContent>
    </Card>
  )
}
