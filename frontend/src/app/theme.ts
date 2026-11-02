import { createTheme } from '@mui/material/styles'

export default createTheme({
  palette: {
    primary: { main: '#1a6b5a' },
    secondary: { main: '#e8f5e9' },
    error: { main: '#c62828' },
    warning: { main: '#f57c00' },
    text: { primary: '#1a2e26', secondary: '#4a6158' },
    background: { default: '#f4f7f5', paper: '#ffffff' },
    divider: '#d0dbd6',
  },
  shape: { borderRadius: 8 },
  typography: {
    fontFamily: '"Segoe UI", system-ui, -apple-system, "Helvetica Neue", Arial, sans-serif',
    h1: { fontSize: '1.75rem', fontWeight: 700, lineHeight: 1.2 },
    h2: { fontSize: '1.25rem', fontWeight: 600 },
    button: { textTransform: 'none', fontWeight: 600, letterSpacing: 0.2 },
  },
  components: {
    MuiAppBar: { defaultProps: { elevation: 0 } },
    MuiButton: { defaultProps: { disableElevation: true } },
    MuiTableCell: { styleOverrides: { head: { fontWeight: 700, backgroundColor: '#f4f7f5' } } },
    MuiTextField: { defaultProps: { fullWidth: true, size: 'small' } },
    MuiChip: { defaultProps: { size: 'small' } },
    MuiPaper: { defaultProps: { variant: 'outlined' } },
  },
})
