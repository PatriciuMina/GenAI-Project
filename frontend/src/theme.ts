import { createTheme } from '@mui/material/styles';

// PLACEHOLDER colors: a teammate will design the real palette. Change only the values below;
// components use theme.palette.* so they pick up the new colors automatically.
export const theme = createTheme({
  palette: {
    mode: 'light',
    primary: { main: '#3949ab' },
    secondary: { main: '#00897b' },
    background: { default: '#f5f6fa' },
  },
  shape: { borderRadius: 8 },
  typography: {
    fontFamily: ['Roboto', 'Helvetica', 'Arial', 'sans-serif'].join(','),
  },
  components: {
    MuiButton: { defaultProps: { disableElevation: true } },
  },
});
