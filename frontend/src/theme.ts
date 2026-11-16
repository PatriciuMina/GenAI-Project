import { createTheme } from '@mui/material/styles';

export const theme = createTheme({
  palette: {
    mode: 'light',

    primary: {
      main: '#3949ab',
      contrastText: '#ffffff',
    },

    secondary: {
      main: '#00796b',
      contrastText: '#ffffff',
    },

    success: {
      main: '#2e7d32',
      contrastText: '#ffffff',
    },

    warning: {
      main: '#8a4b00',
      contrastText: '#ffffff',
    },

    error: {
      main: '#b71c1c',
      contrastText: '#ffffff',
    },

    info: {
      main: '#01579b',
      contrastText: '#ffffff',
    },

    background: {
      default: '#f5f6fa',
      paper: '#ffffff',
    },

    text: {
      primary: '#212121',
      secondary: '#424242',
    },
  },

  shape: {
    borderRadius: 8,
  },

  typography: {
    fontFamily: ['Roboto', 'Helvetica', 'Arial', 'sans-serif'].join(','),
  },

  components: {
    MuiButton: {
      defaultProps: {
        disableElevation: true,
      },
    },
  },
});
