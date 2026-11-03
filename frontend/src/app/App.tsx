import { Bolt, Logout } from '@mui/icons-material'
import { AppBar, Box, Button, Container, Tab, Tabs, Toolbar, Typography } from '@mui/material'
import { jwtDecode } from 'jwt-decode'
import { useState, useEffect } from 'react'
import { Link, Navigate, Route, Routes, useLocation, useNavigate } from 'react-router-dom'
import LoginPage from '../features/auth/LoginPage'
import RegisterPage from '../features/auth/RegisterPage'
import AdminDevicesPage from '../features/devices/AdminDevicesPage'
import ClientDevicesPage from '../features/devices/ClientDevicesPage'
import UsersPage from '../features/users/UsersPage'

function getRole(): string {
  try {
    const token = localStorage.getItem('token')
    if (!token) return ''
    const decoded: any = jwtDecode(token)
    if (decoded.exp * 1000 < Date.now()) { localStorage.removeItem('token'); return '' }
    return decoded.role ?? ''
  } catch { return '' }
}

function AuthLayout() {
  const role = getRole()
  const navigate = useNavigate()
  const location = useLocation()

  if (!role) return <Navigate to="/login" replace />

  const isAdmin = role === 'ADMIN'
  const tabs = isAdmin
    ? [{ label: 'Users', path: '/users' }, { label: 'Devices', path: '/devices' }]
    : [{ label: 'My Devices', path: '/devices' }]

  const currentTab = Math.max(0, tabs.findIndex(t => location.pathname.startsWith(t.path)))

  function handleLogout() {
    localStorage.removeItem('token')
    navigate('/login')
  }

  return (
    <>
      <AppBar position="static" sx={{ bgcolor: 'primary.main' }}>
        <Container maxWidth="lg">
          <Toolbar disableGutters sx={{ gap: 1 }}>
            <Bolt sx={{ mr: 0.5 }} />
            <Typography
              variant="h6"
              component={Link}
              to={isAdmin ? '/users' : '/devices'}
              sx={{ flexGrow: 1, color: 'inherit', textDecoration: 'none', fontWeight: 700, fontSize: '1rem' }}
            >
              Energy Management
            </Typography>
            <Tabs
              value={currentTab}
              textColor="inherit"
              sx={{
                minHeight: 48,
                '& .MuiTabs-indicator': { backgroundColor: 'white' },
              }}
            >
              {tabs.map(t => (
                <Tab
                  key={t.path}
                  label={t.label}
                  component={Link}
                  to={t.path}
                  sx={{ minHeight: 48, color: 'rgba(255,255,255,0.8)', '&.Mui-selected': { color: 'white' } }}
                />
              ))}
            </Tabs>
            <Button
              color="inherit"
              size="small"
              startIcon={<Logout fontSize="small" />}
              onClick={handleLogout}
              sx={{ ml: 1 }}
            >
              Sign out
            </Button>
          </Toolbar>
        </Container>
      </AppBar>

      <Container component="main" maxWidth="lg" sx={{ py: { xs: 3, sm: 5 } }}>
        <Routes>
          {isAdmin && <Route path="/users" element={<UsersPage />} />}
          <Route path="/devices" element={isAdmin ? <AdminDevicesPage /> : <ClientDevicesPage />} />
          <Route path="*" element={<Navigate to={isAdmin ? '/users' : '/devices'} replace />} />
        </Routes>
      </Container>
    </>
  )
}

export default function App() {
  const [, forceUpdate] = useState(0)
  useEffect(() => {
    const handler = () => forceUpdate(n => n + 1)
    window.addEventListener('storage', handler)
    return () => window.removeEventListener('storage', handler)
  }, [])

  return (
    <Box sx={{ minHeight: '100vh', bgcolor: 'background.default' }}>
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/*" element={<AuthLayout />} />
      </Routes>
    </Box>
  )
}
