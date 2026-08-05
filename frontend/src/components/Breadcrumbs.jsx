import { useLocation, Link } from 'react-router-dom';
import { Breadcrumbs as MuiBreadcrumbs, Typography, useTheme } from '@mui/material';
import HomeIcon from '@mui/icons-material/Home';
import NavigateNextIcon from '@mui/icons-material/NavigateNext';

const pathLabels = {
  'dashboard': 'Dashboard',
  'disasters': 'Disasters',
  'report': 'Report Disaster',
  'ai-insights': 'AI Insights',
  'map': 'Live Map',
  'notifications': 'Notifications',
  'admin': 'Admin',
  'eoc': 'Emergency Ops Center',
  'users': 'User Management',
  'rescue-teams': 'Rescue Teams',
  'route-optimization': 'Route Optimization',
  'case-studies': 'Case Studies',
  'admin-control': 'Control Center',
  'settings': 'Settings',
  'profile': 'Profile',
  'drones': 'Drones',
  'hospitals': 'Hospitals',
  'shelters': 'Shelters',
  'volunteers': 'Volunteers',
  'resources': 'Resources',
  'audit-logs': 'Audit Logs',
  'analytics': 'Analytics',
};

export default function Breadcrumbs() {
  const location = useLocation();
  const theme = useTheme();
  const pathnames = location.pathname.split('/').filter(x => x);

  if (pathnames.length === 0) return null;

  const linkColor = theme.palette.text.secondary;
  const currentColor = theme.palette.primary.main;

  return (
    <MuiBreadcrumbs
      aria-label="Breadcrumb"
      separator={<NavigateNextIcon fontSize="small" sx={{ color: 'text.disabled' }} />}
      sx={{ mb: 1, '& .MuiBreadcrumbs-separator': { mx: 0.4 } }}
    >
      <Link to="/dashboard" style={{ color: linkColor, textDecoration: 'none', fontSize: '0.82rem', display: 'inline-flex', alignItems: 'center', gap: 4, fontWeight: 500 }}>
        <HomeIcon sx={{ fontSize: 15 }} />
        Home
      </Link>
      {pathnames.map((value, index) => {
        const last = index === pathnames.length - 1;
        const to = `/${pathnames.slice(0, index + 1).join('/')}`;
        const label = pathLabels[value] || value.charAt(0).toUpperCase() + value.slice(1);

        return last ? (
          <Typography key={to} sx={{ color: currentColor, fontWeight: 650, fontSize: '0.82rem' }} aria-current="page">
            {label}
          </Typography>
        ) : (
          <Link key={to} to={to} style={{ color: linkColor, textDecoration: 'none', fontSize: '0.82rem', fontWeight: 500 }}>
            {label}
          </Link>
        );
      })}
    </MuiBreadcrumbs>
  );
}
