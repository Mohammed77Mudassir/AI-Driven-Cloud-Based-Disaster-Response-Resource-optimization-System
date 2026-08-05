import React from 'react'
import ReactDOM from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom'
import { Toaster } from 'react-hot-toast'
import CssBaseline from '@mui/material/CssBaseline'
import { ThemeModeProvider } from './context/ThemeContext'
import { AuthProvider } from './context/AuthContext'
import { WebSocketProvider } from './context/WebSocketContext'
import App from './App'
import ErrorBoundary from './components/ErrorBoundary'
import './App.css'

ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <ThemeModeProvider>
      <CssBaseline />
      <BrowserRouter>
        <ErrorBoundary>
          <AuthProvider>
            <WebSocketProvider>
              <App />
              <Toaster
                position="top-right"
                gutter={8}
                toastOptions={{
                  style: {
                    borderRadius: 12,
                    background: 'var(--toast-bg, #101B2E)',
                    color: '#fff',
                    fontSize: '0.86rem',
                    fontWeight: 500,
                    border: '1px solid rgba(255,255,255,0.12)',
                    boxShadow: '0 8px 28px rgba(0,0,0,0.35)',
                  },
                  success: { iconTheme: { primary: '#4ADE80', secondary: '#fff' } },
                  error: { iconTheme: { primary: '#F87171', secondary: '#fff' } },
                  duration: 3500,
                }}
              />
            </WebSocketProvider>
          </AuthProvider>
        </ErrorBoundary>
      </BrowserRouter>
    </ThemeModeProvider>
  </React.StrictMode>
)
