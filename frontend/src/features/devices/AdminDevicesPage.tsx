import { AddCircleOutlined } from '@mui/icons-material'
import { Box, Button, Stack, Typography } from '@mui/material'
import { useEffect, useState } from 'react'
import api from '../../shared/api/client'
import ErrorMessage from '../../shared/ui/ErrorMessage'
import LoadingIndicator from '../../shared/ui/LoadingIndicator'
import DeviceFormDialog from './DeviceFormDialog'
import DeviceTable from './DeviceTable'
import type { DeviceDTO, UserDTO } from './types'

export default function AdminDevicesPage() {
  const [devices, setDevices] = useState<DeviceDTO[]>([])
  const [users, setUsers] = useState<UserDTO[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [dialogOpen, setDialogOpen] = useState(false)
  const [editing, setEditing] = useState<DeviceDTO | null>(null)

  async function loadAll() {
    try {
      const [devRes, usrRes] = await Promise.all([api.get('/devices'), api.get('/users')])
      setDevices(devRes.data)
      setUsers(usrRes.data)
    } catch {
      setError('Could not load data.')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { loadAll() }, [])

  function openCreate() { setEditing(null); setDialogOpen(true) }
  function openEdit(d: DeviceDTO) { setEditing(d); setDialogOpen(true) }

  async function handleSave(data: Omit<DeviceDTO, 'id'>, id?: string) {
    if (id) {
      await api.put(`/devices/${id}`, data)
    } else {
      await api.post('/devices', data)
    }
    await loadAll()
  }

  async function handleDelete(device: DeviceDTO) {
    if (!window.confirm(`Delete "${device.description}"?`)) return
    await api.delete(`/devices/${device.id}`)
    setDevices(prev => prev.filter(d => d.id !== device.id))
  }

  return (
    <Box>
      <Stack direction="row" sx={{ justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h2">Devices</Typography>
        <Button variant="contained" startIcon={<AddCircleOutlined />} onClick={openCreate}>
          New device
        </Button>
      </Stack>

      {loading && <LoadingIndicator />}
      {error && <ErrorMessage>{error}</ErrorMessage>}
      {!loading && !error && (
        <DeviceTable devices={devices} users={users} onEdit={openEdit} onDelete={handleDelete} />
      )}

      <DeviceFormDialog
        open={dialogOpen}
        editing={editing}
        users={users}
        onClose={() => setDialogOpen(false)}
        onSave={handleSave}
      />
    </Box>
  )
}
