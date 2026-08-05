import { createTheme } from '@mui/material/styles';

const FONT = '"Inter", "Roboto", "Segoe UI", sans-serif';
const RADIUS = { sm: 8, md: 12, lg: 18, xl: 24 };

const brand = {
  navy: '#0F4C81',
  navyLight: '#1A6BB5',
  navyDark: '#0A3560',
  teal: '#00897B',
  amber: '#F9A825',
};

export const modePalettes = {
  light: {
    primary: { main: '#0F4C81', light: '#1A6BB5', dark: '#0A3560', contrastText: '#ffffff' },
    secondary: { main: '#00897B', light: '#26A69A', dark: '#00695C', contrastText: '#ffffff' },
    accent: { main: '#F57C00', light: '#FF9800', dark: '#E65100' },
    info: { main: '#0288D1', light: '#29B6F6', dark: '#01579B' },
    success: { main: '#2E7D32', light: '#4CAF50', dark: '#1B5E20' },
    warning: { main: '#F57F17', light: '#FFC107', dark: '#E65100' },
    error: { main: '#C62828', light: '#EF5350', dark: '#B71C1C' },
    background: { default: '#EEF2F7', paper: '#FFFFFF', elevated: '#FFFFFF' },
    text: { primary: '#16233B', secondary: '#5B6B84', disabled: '#9AA7BC' },
    divider: 'rgba(15, 76, 129, 0.10)',
    action: { hover: 'rgba(15, 76, 129, 0.05)', selected: 'rgba(15, 76, 129, 0.10)' },
  },
  dark: {
    primary: { main: '#4DA3FF', light: '#7CBDFF', dark: '#2E7FD9', contrastText: '#05121F' },
    secondary: { main: '#2BC8B2', light: '#5BDCCA', dark: '#12A291', contrastText: '#04140F' },
    accent: { main: '#FFA23E', light: '#FFC27A', dark: '#E67E22' },
    info: { main: '#38BDF8', light: '#7DD3FC', dark: '#0284C7' },
    success: { main: '#4ADE80', light: '#86EFAC', dark: '#16A34A' },
    warning: { main: '#FBBF24', light: '#FDE68A', dark: '#D97706' },
    error: { main: '#F87171', light: '#FCA5A5', dark: '#DC2626' },
    background: { default: '#0A1220', paper: '#101B2E', elevated: '#152238' },
    text: { primary: '#E7EEF9', secondary: '#9FB2CC', disabled: '#5C6F8C' },
    divider: 'rgba(148, 178, 219, 0.12)',
    action: { hover: 'rgba(77, 163, 255, 0.08)', selected: 'rgba(77, 163, 255, 0.14)' },
  },
};

const componentOverrides = (palette) => ({
  MuiCssBaseline: {
    styleOverrides: {
      body: { fontFamily: FONT, scrollbarColor: `${palette.divider} transparent` },
      '::-webkit-scrollbar': { width: 10, height: 10 },
      '::-webkit-scrollbar-thumb': { background: palette.divider, borderRadius: 8, '&:hover': { background: palette.text.disabled } },
      '::-webkit-scrollbar-track': { background: 'transparent' },
    },
  },
  MuiButton: {
    defaultProps: { disableElevation: true },
    styleOverrides: {
      root: {
        borderRadius: RADIUS.sm,
        padding: '8px 18px',
        fontWeight: 650,
        textTransform: 'none',
        letterSpacing: '0.1px',
        transition: 'transform 160ms ease, box-shadow 160ms ease, background-color 160ms ease',
        '&:active': { transform: 'scale(0.985)' },
        '&:focus-visible': { outline: `2px solid ${palette.primary.main}`, outlineOffset: 2 },
      },
      sizeLarge: { borderRadius: RADIUS.md, padding: '12px 24px', fontSize: '0.95rem' },
      contained: { boxShadow: `0 4px 14px ${palette.primary.main}33`, '&:hover': { boxShadow: `0 6px 20px ${palette.primary.main}55` } },
      containedSecondary: { boxShadow: `0 4px 14px ${palette.secondary.main}33`, '&:hover': { boxShadow: `0 6px 20px ${palette.secondary.main}55` } },
    },
  },
  MuiCard: {
    styleOverrides: {
      root: {
        borderRadius: RADIUS.md,
        boxShadow: `0 1px 2px ${palette.divider}, 0 6px 24px -12px ${palette.text.primary}22`,
        border: `1px solid ${palette.divider}`,
        backgroundImage: 'none',
        transition: 'box-shadow 200ms ease, transform 200ms ease',
      },
    },
  },
  MuiPaper: {
    styleOverrides: {
      root: { borderRadius: RADIUS.md, backgroundImage: 'none' },
      outlined: { borderColor: palette.divider },
    },
  },
  MuiTableHead: {
    styleOverrides: {
      root: {
        '& .MuiTableCell-head': {
          fontWeight: 700,
          fontSize: '0.72rem',
          textTransform: 'uppercase',
          letterSpacing: '0.6px',
          color: palette.text.secondary,
          backgroundColor: palette.action.hover,
          borderBottom: `1px solid ${palette.divider}`,
        },
      },
    },
  },
  MuiTableRow: {
    styleOverrides: {
      root: {
        transition: 'background-color 120ms ease',
        '&:hover': { backgroundColor: palette.action.hover },
        '&:last-child .MuiTableCell-root': { borderBottom: 'none' },
      },
    },
  },
  MuiTableCell: {
    styleOverrides: {
      root: { borderColor: palette.divider, fontSize: '0.875rem' },
      head: { fontSize: '0.72rem' },
    },
  },
  MuiChip: {
    styleOverrides: {
      root: { fontWeight: 600, fontSize: '0.72rem', borderRadius: 6 },
      label: { lineHeight: '1.4' },
    },
  },
  MuiDialog: {
    styleOverrides: {
      paper: { borderRadius: RADIUS.lg, border: `1px solid ${palette.divider}` },
    },
  },
  MuiDialogTitle: { styleOverrides: { root: { fontWeight: 700, fontSize: '1.1rem' } } },
  MuiAlert: {
    styleOverrides: { root: { borderRadius: RADIUS.md, fontWeight: 500 } },
  },
  MuiTextField: {
    styleOverrides: {
      root: {
        '& .MuiOutlinedInput-root': {
          borderRadius: RADIUS.sm,
          transition: 'box-shadow 180ms ease, border-color 180ms ease',
          '&.Mui-focused': { boxShadow: `0 0 0 3px ${palette.primary.main}22` },
        },
      },
    },
  },
  MuiSelect: { styleOverrides: { outlined: { borderRadius: RADIUS.sm } } },
  MuiAutocomplete: { styleOverrides: { paper: { borderRadius: RADIUS.md } } },
  MuiMenuItem: {
    styleOverrides: { root: { borderRadius: RADIUS.sm, margin: '2px 6px', minHeight: 36 } },
  },
  MuiListSubheader: {
    styleOverrides: {
      root: {
        fontWeight: 700,
        fontSize: '0.7rem',
        textTransform: 'uppercase',
        letterSpacing: '0.8px',
        color: palette.text.secondary,
        backgroundColor: 'transparent',
        lineHeight: '28px',
      },
    },
  },
  MuiTooltip: {
    styleOverrides: { tooltip: { borderRadius: RADIUS.sm, fontSize: '0.75rem', fontWeight: 500 } },
  },
  MuiSkeleton: {
    styleOverrides: { root: { backgroundColor: palette.action.hover, borderRadius: RADIUS.sm } },
  },
  MuiTab: {
    styleOverrides: {
      root: {
        textTransform: 'none',
        fontWeight: 600,
        fontSize: '0.875rem',
        minHeight: 46,
        '&:focus-visible': { outline: `2px solid ${palette.primary.main}`, outlineOffset: -2, borderRadius: RADIUS.sm },
      },
    },
  },
  MuiToggleButton: {
    styleOverrides: { root: { textTransform: 'none', borderRadius: RADIUS.sm } },
  },
  MuiDrawer: {
    styleOverrides: {
      paper: { borderRight: `1px solid ${palette.divider}` },
    },
  },
  MuiListItemButton: {
    styleOverrides: { root: { '&:focus-visible': { outline: `2px solid ${palette.primary.main}`, outlineOffset: -2 } } },
  },
  MuiIconButton: {
    styleOverrides: { root: { '&:focus-visible': { outline: `2px solid ${palette.primary.main}`, outlineOffset: 1 } } },
  },
});

function buildTheme(mode) {
  const palette = modePalettes[mode];
  return createTheme({
    palette: { mode, ...palette },
    typography: {
      fontFamily: FONT,
      h1: { fontWeight: 800, fontSize: '1.9rem', lineHeight: 1.25 },
      h2: { fontWeight: 700, fontSize: '1.45rem', lineHeight: 1.3 },
      h3: { fontWeight: 700, fontSize: '1.22rem' },
      h4: { fontWeight: 700, fontSize: '1.05rem' },
      h5: { fontWeight: 650, fontSize: '0.95rem' },
      h6: { fontWeight: 650, fontSize: '0.86rem', letterSpacing: '0.2px' },
      subtitle1: { fontWeight: 550, fontSize: '0.95rem' },
      subtitle2: { fontWeight: 600, fontSize: '0.82rem' },
      body1: { fontSize: '0.9rem', lineHeight: 1.55 },
      body2: { fontSize: '0.82rem', lineHeight: 1.5 },
      button: { textTransform: 'none', fontWeight: 650 },
      caption: { fontSize: '0.74rem', letterSpacing: '0.2px' },
      overline: { fontSize: '0.68rem', fontWeight: 700, letterSpacing: '1.2px' },
    },
    shape: { borderRadius: 10 },
    shadows: [
      'none', '0 1px 2px rgba(16,24,40,0.05)', '0 1px 3px rgba(16,24,40,0.08)',
      '0 2px 8px -2px rgba(16,24,40,0.1)', '0 4px 12px -2px rgba(16,24,40,0.12)',
      '0 8px 24px -4px rgba(16,24,40,0.14)', '0 10px 28px -4px rgba(16,24,40,0.16)',
      '0 12px 32px -4px rgba(16,24,40,0.18)', '0 14px 40px -4px rgba(16,24,40,0.2)',
      '0 16px 48px -4px rgba(16,24,40,0.22)', '0 18px 52px -4px rgba(16,24,40,0.24)',
      '0 20px 56px -6px rgba(16,24,40,0.26)', '0 22px 60px -6px rgba(16,24,40,0.28)',
      '0 24px 64px -6px rgba(16,24,40,0.3)', '0 26px 68px -6px rgba(16,24,40,0.32)',
      '0 28px 72px -6px rgba(16,24,40,0.34)', '0 30px 76px -6px rgba(16,24,40,0.36)',
      '0 32px 80px -6px rgba(16,24,40,0.38)', '0 34px 84px -6px rgba(16,24,40,0.4)',
      '0 36px 88px -6px rgba(16,24,40,0.42)', '0 38px 92px -6px rgba(16,24,40,0.44)',
      '0 40px 96px -6px rgba(16,24,40,0.46)', '0 42px 100px -6px rgba(16,24,40,0.48)',
      '0 44px 104px -6px rgba(16,24,40,0.5)', '0 46px 108px -6px rgba(16,24,40,0.52)',
    ],
    components: componentOverrides(palette),
  });
}

export const lightTheme = buildTheme('light');
export const darkTheme = buildTheme('dark');

export default lightTheme;
