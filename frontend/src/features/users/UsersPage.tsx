import { PersonAdd } from '@mui/icons-material'
import { Box, Button, Stack, Typography } from '@mui/material'
import { useEffect, useState } from 'react'
import api from '../../shared/api/client'
import ErrorMessage from '../../shared/ui/ErrorMessage'
import LoadingIndicator from '../../shared/ui/LoadingIndicator'
import UserFormDialog from './UserFormDialog'
import UserTable from './UserTable'
import type { UserDTO } from './types'

export default function UsersPage() {
  const [users, setUsers] = useState<UserDTO[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [dialogOpen, setDialogOpen] = useState(false)
  const [editing, setEditing] = useState<UserDTO | null>(null)

  async function loadUsers() {
    try {
      const res = await api.get('/users')
      setUsers(res.data)
    } catch {
      setError('Could not load users.')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { loadUsers() }, [])

  function openCreate() { setEditing(null); setDialogOpen(true) }
  function openEdit(u: UserDTO) { setEditing(u); setDialogOpen(true) }

  async function handleSave(data: { username: string; password: string; role: string }, id?: string) {
    if (id) {
      await api.put(`/users/${id}`, data)
    } else {
      await api.post('/auth/register', { ...data, role: 'CLIENT' })
      await api.post('/users', { ...data, role: 'CLIENT' })
    }
    await loadUsers()
  }

  async function handleDelete(user: UserDTO) {
    if (!window.confirm(`Delete "${user.username}"? This cannot be undone.`)) return
    await api.delete(`/users/${user.id}`)
    setUsers(prev => prev.filter(u => u.id !== user.id))
  }

  return (
    <Box>
      <Stack direction="row" sx={{ justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h2">Users</Typography>
        <Button variant="contained" startIcon={<PersonAdd />} onClick={openCreate}>
          New user
        </Button>
      </Stack>

      {loading && <LoadingIndicator />}
      {error && <ErrorMessage>{error}</ErrorMessage>}
      {!loading && !error && (
        <UserTable users={users} onEdit={openEdit} onDelete={handleDelete} />
      )}

      <UserFormDialog
        open={dialogOpen}
        editing={editing}
        onClose={() => setDialogOpen(false)}
        onSave={handleSave}
      />
    </Box>
  )
}
