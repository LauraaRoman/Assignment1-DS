import { Bolt } from '@mui/icons-material'
import { Alert, Box, Button, CircularProgress, Paper, Stack, TextField, Typography } from '@mui/material'
import { useState } from 'react'
import type { FormEvent } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import api from '../../shared/api/client'

export default function RegisterPage() {
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)
  const navigate = useNavigate()

  async function handleSubmit(e: FormEvent) {
    e.preventDefault()
    setError(null)
    setLoading(true)
    try {
      await api.post('/auth/register', { username, password, role: 'CLIENT' })
      const loginRes = await api.post('/auth/login', null, { params: { username, password } })
      const token = loginRes.data
      await api.post('/users', { username, password, role: 'CLIENT' }, {
        headers: { Authorization: `Bearer ${token}` }
      })
      navigate('/login')
    } catch (err: any) {
      setError(err?.response?.data || 'Could not create account. Username may already be taken.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <Box sx={{ minHeight: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center', bgcolor: 'background.default', p: 2 }}>
      <Box sx={{ width: '100%', maxWidth: 400 }}>
        <Stack sx={{ alignItems: 'center', mb: 4 }} spacing={1}>
          <Box sx={{ bgcolor: 'primary.main', borderRadius: '50%', p: 1.5, display: 'flex' }}>
            <Bolt sx={{ color: 'white', fontSize: 28 }} />
          </Box>
          <Typography variant="h1" sx={{ fontSize: '1.5rem' }}>Energy Management</Typography>
          <Typography color="text.secondary" variant="body2">Create a client account</Typography>
        </Stack>

        <Paper sx={{ p: { xs: 3, sm: 4 } }}>
          <Box component="form" onSubmit={handleSubmit} noValidate>
            <Stack spacing={2.5}>
              <TextField label="Username" value={username} onChange={e => setUsername(e.target.value)} autoComplete="username" required />
              <TextField label="Password" type="password" value={password} onChange={e => setPassword(e.target.value)} autoComplete="new-password" required />
              {error && <Alert severity="error">{error}</Alert>}
              <Button type="submit" variant="contained" size="large" disabled={loading}
                startIcon={loading ? <CircularProgress size={16} color="inherit" /> : undefined}>
                {loading ? 'Creating account…' : 'Create account'}
              </Button>
            </Stack>
          </Box>
        </Paper>

        <Typography variant="body2" align="center" sx={{ mt: 2.5, color: 'text.secondary' }}>
          Already have an account?{' '}
          <Typography component={Link} to="/login" variant="body2" sx={{ color: 'primary.main', textDecoration: 'none', fontWeight: 600 }}>
            Sign in
          </Typography>
        </Typography>
      </Box>
    </Box>
  )
}
