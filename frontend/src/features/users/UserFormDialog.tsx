import {
  Box, Button, CircularProgress, Dialog, DialogActions, DialogContent,
  DialogTitle, Stack, TextField,
} from '@mui/material'
import { useEffect, useState } from 'react'
import type { FormEvent } from 'react'
import ErrorMessage from '../../shared/ui/ErrorMessage'
import type { UserDTO } from './types'

interface Props {
  open: boolean
  editing: UserDTO | null
  onClose: () => void
  onSave: (data: { username: string; password: string; role: string }, id?: string) => Promise<void>
}

export default function UserFormDialog({ open, editing, onClose, onSave }: Props) {
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    if (open) {
      setUsername(editing?.username ?? '')
      setPassword('')
      setError(null)
    }
  }, [open, editing])

  async function handleSubmit(e: FormEvent) {
    e.preventDefault()
    if (!username.trim()) return
    setError(null)
    setLoading(true)
    try {
      await onSave({ username, password, role: 'CLIENT' }, editing?.id)
      onClose()
    } catch (err: any) {
      setError(err?.response?.data || 'An error occurred.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <Dialog open={open} onClose={onClose} fullWidth maxWidth="xs">
      <DialogTitle>{editing ? 'Edit User' : 'New User'}</DialogTitle>
      <DialogContent>
        <Box component="form" id="user-form" onSubmit={handleSubmit} noValidate>
          <Stack spacing={2.5} sx={{ pt: 1 }}>
            <TextField
              label="Username"
              value={username}
              onChange={e => setUsername(e.target.value)}
              required
              autoFocus
            />
            <TextField
              label={editing ? 'New password (leave blank to keep)' : 'Password'}
              type="password"
              value={password}
              onChange={e => setPassword(e.target.value)}
              required={!editing}
            />
            {error && <ErrorMessage>{error}</ErrorMessage>}
          </Stack>
        </Box>
      </DialogContent>
      <DialogActions sx={{ px: 3, pb: 2 }}>
        <Button onClick={onClose} disabled={loading}>Cancel</Button>
        <Button
          type="submit"
          form="user-form"
          variant="contained"
          disabled={loading}
          startIcon={loading ? <CircularProgress size={14} color="inherit" /> : undefined}
        >
          {loading ? 'Saving…' : editing ? 'Save changes' : 'Create user'}
        </Button>
      </DialogActions>
    </Dialog>
  )
}
