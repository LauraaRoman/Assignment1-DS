import { BoltOutlined } from '@mui/icons-material'
import { Box, Paper, Stack, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Typography } from '@mui/material'
import { jwtDecode } from 'jwt-decode'
import { useEffect, useState } from 'react'
import api from '../../shared/api/client'
import ErrorMessage from '../../shared/ui/ErrorMessage'
import LoadingIndicator from '../../shared/ui/LoadingIndicator'
import type { DeviceDTO } from './types'

export default function ClientDevicesPage() {
  const [devices, setDevices] = useState<DeviceDTO[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    async function load() {
      const token = localStorage.getItem('token')
      if (!token) return
      try {
        const decoded: any = jwtDecode(token)
        const userRes = await api.get(`/users/by-username/${decoded.sub}`)
        const devRes = await api.get(`/devices/user/${userRes.data.id}`)
        setDevices(devRes.data)
      } catch (err: any) {
        if (err?.response?.status === 404) {
          setError('Your profile was not found. Contact an administrator.')
        } else {
          setError('Could not load your devices.')
        }
      } finally {
        setLoading(false)
      }
    }
    load()
  }, [])

  if (loading) return <LoadingIndicator />
  if (error) return <ErrorMessage>{error}</ErrorMessage>

  if (devices.length === 0) {
    return (
      <Paper sx={{ p: 6, textAlign: 'center' }}>
        <BoltOutlined sx={{ fontSize: 48, color: 'text.secondary', mb: 1 }} />
        <Typography variant="h2" sx={{ mb: 0.5 }}>No devices yet</Typography>
        <Typography color="text.secondary">No smart devices have been assigned to your account.</Typography>
      </Paper>
    )
  }

  const totalMax = devices.reduce((sum, d) => sum + d.maxHourlyEnergyConsumption, 0)

  return (
    <Box>
      <Stack direction="row" sx={{ justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h2">My Devices</Typography>
        <Typography variant="body2" color="text.secondary">
          {devices.length} device{devices.length !== 1 ? 's' : ''} · {totalMax.toFixed(2)} kWh/h total
        </Typography>
      </Stack>

      <TableContainer component={Paper}>
        <Table size="small" aria-label="My devices">
          <TableHead>
            <TableRow>
              <TableCell>Description</TableCell>
              <TableCell>Address</TableCell>
              <TableCell align="right">Max kWh/h</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {devices.map(d => (
              <TableRow key={d.id} hover>
                <TableCell sx={{ fontWeight: 500 }}>{d.description}</TableCell>
                <TableCell sx={{ color: 'text.secondary' }}>{d.address}</TableCell>
                <TableCell align="right">{d.maxHourlyEnergyConsumption}</TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>
    </Box>
  )
}
