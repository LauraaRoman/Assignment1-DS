import {
  Box, Button, CircularProgress, Dialog, DialogActions, DialogContent,
  DialogTitle, MenuItem, Stack, TextField,
} from '@mui/material'
import { useEffect, useState } from 'react'
import type { FormEvent } from 'react'
import ErrorMessage from '../../shared/ui/ErrorMessage'
import type { DeviceDTO, UserDTO } from './types'

interface FormValues {
  description: string
  address: string
  maxHourlyEnergyConsumption: string
  userId: string
}

const EMPTY: FormValues = { description: '', address: '', maxHourlyEnergyConsumption: '', userId: '' }

interface Props {
  open: boolean
  editing: DeviceDTO | null
  users: UserDTO[]
  onClose: () => void
  onSave: (data: Omit<DeviceDTO, 'id'>, id?: string) => Promise<void>
}

export default function DeviceFormDialog({ open, editing, users, onClose, onSave }: Props) {
  const [values, setValues] = useState<FormValues>(EMPTY)
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    if (open) {
      setValues(editing ? {
        description: editing.description,
        address: editing.address,
        maxHourlyEnergyConsumption: String(editing.maxHourlyEnergyConsumption),
        userId: editing.userId,
      } : EMPTY)
      setError(null)
    }
  }, [open, editing])

  function set(field: keyof FormValues) {
    return (e: React.ChangeEvent<HTMLInputElement>) => setValues(v => ({ ...v, [field]: e.target.value }))
  }

  async function handleSubmit(e: FormEvent) {
    e.preventDefault()
    if (!values.description.trim() || !values.userId) return
    setError(null)
    setLoading(true)
    try {
      await onSave({
        description: values.description,
        address: values.address,
        maxHourlyEnergyConsumption: Number(values.maxHourlyEnergyConsumption),
        userId: values.userId,
      }, editing?.id)
      onClose()
    } catch {
      setError('Could not save device.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <Dialog open={open} onClose={onClose} fullWidth maxWidth="xs">
      <DialogTitle>{editing ? 'Edit Device' : 'New Device'}</DialogTitle>
      <DialogContent>
        <Box component="form" id="device-form" onSubmit={handleSubmit} noValidate>
          <Stack spacing={2.5} sx={{ pt: 1 }}>
            <TextField label="Description" value={values.description} onChange={set('description')} required autoFocus />
            <TextField label="Address" value={values.address} onChange={set('address')} required />
            <TextField
              label="Max hourly consumption (kWh)"
              type="number"
              slotProps={{ htmlInput: { step: 0.01, min: 0 } }}
              value={values.maxHourlyEnergyConsumption}
              onChange={set('maxHourlyEnergyConsumption')}
              required
            />
            <TextField
              select
              label="Owner"
              value={values.userId}
              onChange={set('userId')}
              required
            >
              <MenuItem value="" disabled>Select owner</MenuItem>
              {users.map(u => (
                <MenuItem key={u.id} value={u.id}>{u.username} ({u.role})</MenuItem>
              ))}
            </TextField>
            {error && <ErrorMessage>{error}</ErrorMessage>}
          </Stack>
        </Box>
      </DialogContent>
      <DialogActions sx={{ px: 3, pb: 2 }}>
        <Button onClick={onClose} disabled={loading}>Cancel</Button>
        <Button
          type="submit"
          form="device-form"
          variant="contained"
          disabled={loading}
          startIcon={loading ? <CircularProgress size={14} color="inherit" /> : undefined}
        >
          {loading ? 'Saving…' : editing ? 'Save changes' : 'Create device'}
        </Button>
      </DialogActions>
    </Dialog>
  )
}
