import { Delete, Edit } from '@mui/icons-material'
import {
  IconButton, Paper, Table, TableBody, TableCell,
  TableContainer, TableHead, TableRow, Tooltip, Typography,
} from '@mui/material'
import type { DeviceDTO, UserDTO } from './types'

interface Props {
  devices: DeviceDTO[]
  users: UserDTO[]
  onEdit: (device: DeviceDTO) => void
  onDelete: (device: DeviceDTO) => void
}

export default function DeviceTable({ devices, users, onEdit, onDelete }: Props) {
  if (devices.length === 0) {
    return (
      <Paper sx={{ p: 4, textAlign: 'center' }}>
        <Typography color="text.secondary">No devices yet. Add one above.</Typography>
      </Paper>
    )
  }

  function ownerName(userId: string) {
    return users.find(u => u.id === userId)?.username ?? userId.substring(0, 8) + '…'
  }

  return (
    <TableContainer component={Paper}>
      <Table size="small" aria-label="Devices">
        <TableHead>
          <TableRow>
            <TableCell>Description</TableCell>
            <TableCell>Address</TableCell>
            <TableCell align="right">Max kWh/h</TableCell>
            <TableCell>Owner</TableCell>
            <TableCell sx={{ width: 80 }} align="right">Actions</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {devices.map(d => (
            <TableRow key={d.id} hover>
              <TableCell sx={{ fontWeight: 500 }}>{d.description}</TableCell>
              <TableCell sx={{ color: 'text.secondary' }}>{d.address}</TableCell>
              <TableCell align="right">{d.maxHourlyEnergyConsumption}</TableCell>
              <TableCell>{ownerName(d.userId)}</TableCell>
              <TableCell align="right">
                <Tooltip title="Edit">
                  <IconButton size="small" onClick={() => onEdit(d)} aria-label={`Edit ${d.description}`}>
                    <Edit fontSize="small" />
                  </IconButton>
                </Tooltip>
                <Tooltip title="Delete">
                  <IconButton size="small" color="error" onClick={() => onDelete(d)} aria-label={`Delete ${d.description}`}>
                    <Delete fontSize="small" />
                  </IconButton>
                </Tooltip>
              </TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </TableContainer>
  )
}
