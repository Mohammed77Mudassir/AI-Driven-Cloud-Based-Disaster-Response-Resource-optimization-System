import { useState } from 'react'
import { NavLink, useLocation } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { useWebSocket } from '../context/WebSocketContext'
import {
  Drawer, Box, List, ListItem, ListItemButton, ListItemIcon, ListItemText,
  Typography, Avatar, Divider, Button, Collapse, IconButton, Tooltip, useTheme, useMediaQuery
} from '@mui/material'
import DashboardIcon from '@mui/icons-material/Dashboard'
import WavesIcon from '@mui/icons-material/Waves'
import ReportIcon from '@mui/icons-material/Report'
import SmartToyIcon from '@mui/icons-material/SmartToy'
import MapIcon from '@mui/icons-material/Map'
import NotificationsIcon from '@mui/icons-material/Notifications'
import AdminPanelSettingsIcon from '@mui/icons-material/AdminPanelSettings'
import FlightIcon from '@mui/icons-material/Flight'
import LocalHospitalIcon from '@mui/icons-material/LocalHospital'
import HomeIcon from '@mui/icons-material/Home'
import PeopleIcon from '@mui/icons-material/People'
import InventoryIcon from '@mui/icons-material/Inventory'
import DescriptionIcon from '@mui/icons-material/Description'
import BarChartIcon from '@mui/icons-material/BarChart'
import LogoutIcon from '@mui/icons-material/Logout'
import ExpandLess from '@mui/icons-material/ExpandLess'
import ExpandMore from '@mui/icons-material/ExpandMore'
import SecurityIcon from '@mui/icons-material/Security'
import PersonIcon from '@mui/icons-material/Person'
import GroupsIcon from '@mui/icons-material/Groups'
import RouteIcon from '@mui/icons-material/Route'
import MenuBookIcon from '@mui/icons-material/MenuBook'
import MenuIcon from '@mui/icons-material/Menu'
import ChevronLeftIcon from '@mui/icons-material/ChevronLeft'
import ChevronRightIcon from '@mui/icons-material/ChevronRight'
import ThemeToggle from './ThemeToggle'
import WebSocketStatus from './WebSocketStatus'
import NotificationBell from './NotificationBell'

const FULL_WIDTH = 268
const MINI_WIDTH = 76

const navGroups = [
  {
    label: 'Command',
    items: [
      { path: '/dashboard', label: 'Dashboard', icon: <DashboardIcon />, permission: 'DASHBOARD_VIEW' },
      { path: '/disasters', label: 'Disasters', icon: <WavesIcon />, permission: 'DISASTER_VIEW' },
      { path: '/map', label: 'Live Map', icon: <MapIcon />, permission: 'MAP_VIEW' },
      { path: '/report', label: 'Report Disaster', icon: <ReportIcon />, permission: 'DISASTER_CREATE' },
    ],
  },
  {
    label: 'Intelligence',
    items: [
      { path: '/ai-insights', label: 'AI Insights', icon: <SmartToyIcon />, permission: 'AI_VIEW' },
      { path: '/route-optimization', label: 'Route Planner', icon: <RouteIcon />, permission: 'ROUTE_VIEW' },
      { path: '/case-studies', label: 'Case Studies', icon: <MenuBookIcon />, permission: 'CASE_STUDY_VIEW' },
    ],
  },
  {
    label: 'Field Operations',
    items: [
      { path: '/rescue-teams', label: 'Rescue Teams', icon: <GroupsIcon />, permission: 'RESCUE_TEAM_MANAGE' },
      { path: '/drones', label: 'Drones', icon: <FlightIcon />, permission: 'DRONE_MANAGE' },
      { path: '/resources', label: 'Resources', icon: <InventoryIcon />, permission: 'RESOURCE_MANAGE' },
      { path: '/hospitals', label: 'Hospitals', icon: <LocalHospitalIcon />, permission: 'HOSPITAL_MANAGE' },
      { path: '/shelters', label: 'Shelters', icon: <HomeIcon />, permission: 'SHELTER_MANAGE' },
      { path: '/volunteers', label: 'Volunteers', icon: <PeopleIcon />, permission: 'VOLUNTEER_MANAGE' },
    ],
  },
  {
    label: 'Administration',
    items: [
      { path: '/admin/eoc', label: 'Ops Center', icon: <AdminPanelSettingsIcon />, permission: 'SETTINGS_MANAGE' },
      { path: '/admin/disasters', label: 'All Disasters', icon: <WavesIcon />, permission: 'DISASTER_VIEW' },
      { path: '/users', label: 'Users', icon: <PersonIcon />, permission: 'USER_MANAGE' },
      { path: '/audit-logs', label: 'Audit Logs', icon: <DescriptionIcon />, permission: 'AUDIT_VIEW' },
      { path: '/analytics', label: 'Analytics', icon: <BarChartIcon />, permission: 'ANALYTICS_VIEW' },
      { path: '/admin-control', label: 'Control Center', icon: <DashboardIcon />, permission: 'SETTINGS_MANAGE' },
    ],
  },
]

export default function Sidebar() {
  const { user, logout, hasPermission } = useAuth()
  const { connected, status, reconnectAttempt } = useWebSocket()
  const location = useLocation()
  const theme = useTheme()
  const isMobile = useMediaQuery(theme.breakpoints.down('md'))
  const [collapsed, setCollapsed] = useState(false)
  const [mobileOpen, setMobileOpen] = useState(false)
  const [adminOpen, setAdminOpen] = useState(true)

  const isActive = (path) => location.pathname === path || location.pathname.startsWith(path + '/')

  const groups = navGroups
    .map(g => ({ ...g, items: g.items.filter(i => hasPermission(i.permission)) }))
    .filter(g => g.items.length > 0)

  const renderNavItem = (item) => {
    const active = isActive(item.path)
    const content = (
      <ListItemButton
        component={NavLink}
        to={item.path}
        selected={active}
        onClick={() => isMobile && setMobileOpen(false)}
        sx={{
          borderRadius: 2,
          py: 1.15,
          mb: 0.4,
          justifyContent: collapsed && !isMobile ? 'center' : 'flex-start',
          '&.Mui-selected': {
            backgroundColor: theme.palette.action.selected,
            color: 'primary.main',
            '&:hover': { backgroundColor: theme.palette.action.selected },
            '&::before': {
              content: '""',
              position: 'absolute',
              left: 0,
              top: '20%',
              height: '60%',
              width: 3,
              borderRadius: 3,
              backgroundColor: 'primary.main',
            },
          },
        }}
      >
        <ListItemIcon sx={{ minWidth: collapsed && !isMobile ? 0 : 38, justifyContent: 'center', color: active ? 'primary.main' : 'text.secondary' }}>
          {item.icon}
        </ListItemIcon>
        {(!collapsed || isMobile) && (
          <ListItemText primary={item.label} slotProps={{ primary: { fontSize: '0.86rem', fontWeight: active ? 700 : 500 } }} />
        )}
      </ListItemButton>
    )
    return (!collapsed && !isMobile)
      ? <Tooltip key={item.path} title={item.label} placement="right">{content}</Tooltip>
      : <Box key={item.path}>{content}</Box>
  }

  const renderGroup = (group) => {
    const label = (!collapsed || isMobile) && (
      <Typography
        variant="overline"
        sx={{
          display: 'block',
          px: 2,
          pt: 2,
          pb: 0.75,
          color: 'text.disabled',
          fontSize: '0.64rem',
          letterSpacing: '1.2px',
        }}
      >
        {group.label}
      </Typography>
    )
    return (
      <Box key={group.label}>
        {label}
        <List dense sx={{ px: 1.25 }}>{group.items.map(renderNavItem)}</List>
      </Box>
    )
  }

  const drawerContent = (inMini = false) => (
    <Box sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
      {/* Brand */}
      <Box
        component={NavLink}
        to="/dashboard"
        sx={{
          display: 'flex',
          alignItems: 'center',
          gap: 1.5,
          px: inMini ? 1 : 2.5,
          py: 2.25,
          textDecoration: 'none',
          justifyContent: inMini ? 'center' : 'flex-start',
        }}
      >
        <Box
          aria-hidden
          sx={{
            width: 42,
            height: 42,
            borderRadius: 2.5,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            background: `linear-gradient(135deg, ${theme.palette.primary.main}, ${theme.palette.secondary.main})`,
            color: '#fff',
            boxShadow: `0 6px 16px ${theme.palette.primary.main}55`,
            flexShrink: 0,
          }}
        >
          <SecurityIcon sx={{ fontSize: 24 }} />
        </Box>
        {!inMini && (
          <Box sx={{ minWidth: 0 }}>
            <Typography sx={{ fontWeight: 800, fontSize: '1.02rem', lineHeight: 1.1, color: 'text.primary' }}>
              DMS
            </Typography>
            <Typography variant="caption" sx={{ color: 'text.secondary', fontSize: '0.66rem', letterSpacing: '0.4px' }}>
              Disaster Management System
            </Typography>
          </Box>
        )}
      </Box>

      <Divider />

      {/* Collapse toggle (desktop) */}
      {!isMobile && (
        <Box sx={{ display: 'flex', justifyContent: inMini ? 'center' : 'flex-end', px: inMini ? 1 : 2, py: 1 }}>
          <Tooltip title={collapsed ? 'Expand navigation' : 'Collapse navigation'}>
            <IconButton size="small" onClick={() => setCollapsed(c => !c)} aria-label={collapsed ? 'Expand navigation' : 'Collapse navigation'}>
              {collapsed ? <ChevronRightIcon fontSize="small" /> : <ChevronLeftIcon fontSize="small" />}
            </IconButton>
          </Tooltip>
        </Box>
      )}

      {/* Navigation */}
      <Box sx={{ flex: 1, overflowY: 'auto', overflowX: 'hidden', pb: 1 }}>
        {groups.map(renderGroup)}
      </Box>

      {/* Live status + actions */}
      <Divider />
      <Box sx={{ p: inMini ? 1.25 : 2, display: 'flex', flexDirection: 'column', gap: 1.5 }}>
        {!inMini && (
          <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
            <WebSocketStatus connected={connected} status={status} reconnectAttempt={reconnectAttempt} />
            <ThemeToggle />
          </Box>
        )}
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
          {inMini ? (
            <Avatar sx={{ width: 36, height: 36, bgcolor: 'primary.main', fontSize: '0.9rem', fontWeight: 700 }}>
              {user?.username?.[0]?.toUpperCase()}
            </Avatar>
          ) : (
            <>
              <NotificationBell />
              <Avatar sx={{ width: 36, height: 36, bgcolor: 'primary.main', fontSize: '0.9rem', fontWeight: 700 }}>
                {user?.username?.[0]?.toUpperCase()}
              </Avatar>
              <Box sx={{ minWidth: 0 }}>
                <Typography variant="body2" sx={{ fontWeight: 700, lineHeight: 1.2, color: 'text.primary' }} noWrap>
                  {user?.username}
                </Typography>
                <Typography variant="caption" sx={{ color: 'text.secondary', fontSize: '0.68rem' }} noWrap>
                  {user?.role}
                </Typography>
              </Box>
            </>
          )}
        </Box>
        <Button
          fullWidth={!inMini}
          size="small"
          variant="outlined"
          startIcon={inMini ? null : <LogoutIcon />}
          onClick={logout}
          aria-label="Logout"
          sx={{
            minWidth: inMini ? 40 : 'auto',
            px: inMini ? 1 : 2,
            justifyContent: inMini ? 'center' : 'center',
            borderColor: 'divider',
            color: 'text.secondary',
            '&:hover': { borderColor: 'error.main', color: 'error.main', backgroundColor: 'error.soft' },
          }}
        >
          {inMini ? <LogoutIcon sx={{ fontSize: 18 }} /> : 'Logout'}
        </Button>
      </Box>
    </Box>
  )

  // Mobile: temporary drawer + floating hamburger
  if (isMobile) {
    return (
      <>
        <IconButton
          onClick={() => setMobileOpen(true)}
          aria-label="Open navigation menu"
          sx={{
            position: 'fixed',
            top: 12,
            left: 12,
            zIndex: (t) => t.zIndex.appBar - 1,
            bgcolor: 'background.paper',
            border: '1px solid',
            borderColor: 'divider',
            boxShadow: 3,
            '&:hover': { bgcolor: 'action.hover' },
          }}
        >
          <MenuIcon />
        </IconButton>
        <Drawer
          variant="temporary"
          open={mobileOpen}
          onClose={() => setMobileOpen(false)}
          ModalProps={{ keepMounted: true }}
          sx={{ '& .MuiDrawer-paper': { width: FULL_WIDTH, boxSizing: 'border-box' } }}
        >
          {drawerContent(false)}
        </Drawer>
      </>
    )
  }

  // Desktop: permanent, collapsible mini drawer
  return (
    <Drawer
      variant="permanent"
      sx={{
        width: collapsed ? MINI_WIDTH : FULL_WIDTH,
        flexShrink: 0,
        transition: 'width 220ms cubic-bezier(0.4, 0, 0.2, 1)',
        '& .MuiDrawer-paper': {
          width: collapsed ? MINI_WIDTH : FULL_WIDTH,
          boxSizing: 'border-box',
          overflowX: 'hidden',
          transition: 'width 220ms cubic-bezier(0.4, 0, 0.2, 1)',
        },
      }}
    >
      {drawerContent(collapsed)}
    </Drawer>
  )
}
