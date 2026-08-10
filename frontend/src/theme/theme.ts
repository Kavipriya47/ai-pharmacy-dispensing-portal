import { createTheme } from '@mui/material/styles';

const theme = createTheme({
  palette: {
    mode: 'light',
    primary: {
      main: '#0d7c66',     // Professional teal
      light: '#17b794',
      dark: '#095e4e',
      contrastText: '#ffffff',
    },
    secondary: {
      main: '#3a86a8',     // Calming blue
      light: '#5ba3c4',
      dark: '#276b8c',
    },
    error: {
      main: '#d32f2f',
    },
    warning: {
      main: '#ed6c02',
    },
    success: {
      main: '#2e7d32',
    },
    info: {
      main: '#0288d1',
    },
    background: {
      default: '#f5f7fa',
      paper: '#ffffff',
    },
    text: {
      primary: '#1a2027',
      secondary: '#556270',
    },
  },
  typography: {
    fontFamily: '"Inter", "Roboto", "Helvetica", "Arial", sans-serif',
    h4: {
      fontWeight: 600,
    },
    h5: {
      fontWeight: 600,
    },
    h6: {
      fontWeight: 600,
    },
  },
  shape: {
    borderRadius: 10,
  },
  components: {
    MuiButton: {
      styleOverrides: {
        root: {
          textTransform: 'none',
          fontWeight: 600,
          borderRadius: 8,
        },
      },
    },
    MuiCard: {
      styleOverrides: {
        root: {
          borderRadius: 12,
          boxShadow: '0 1px 3px rgba(0,0,0,0.08), 0 1px 2px rgba(0,0,0,0.06)',
        },
      },
    },
    MuiDrawer: {
      styleOverrides: {
        paper: {
          borderRight: 'none',
          boxShadow: '2px 0 8px rgba(0,0,0,0.05)',
        },
      },
    },
  },
});

export default theme;
