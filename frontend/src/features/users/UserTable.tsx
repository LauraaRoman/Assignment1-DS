import { Delete, Edit } from '@mui/icons-material'
import {
  Chip, IconButton, Paper, Table, TableBody, TableCell,
  TableContainer, TableHead, TableRow, Tooltip, Typography,
} from '@mui/material'
import type { UserDTO } from './types'

interface Props {
  users: UserDTO[]
  onEdit: (user: UserDTO) => void
  onDelete: (user: UserDTO) => void
}

export default function UserTable({ users, onEdit, onDelete }: Props) {
  if (users.length === 0) {
    return (
      <Paper sx={{ p: 4, textAlign: 'center' }}>
        <Typography color="text.secondary">No users yet. Add one above.</Typography>
      </Paper>
    )
  }

  return (
    <TableContainer component={Paper}>
      <Table size="small" aria-label="Users">
        <TableHead>
          <TableRow>
            <TableCell>Username</TableCell>
            <TableCell>Role</TableCell>
            <TableCell sx={{ width: 80 }} align="right">Actions</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {users.map(u => (
            <TableRow key={u.id} hover>
              <TableCell sx={{ fontWeight: 500 }}>{u.username}</TableCell>
              <TableCell>
                <Chip
                  label={u.role}
                  size="small"
                  color={u.role === 'ADMIN' ? 'primary' : 'default'}
                  variant={u.role === 'ADMIN' ? 'filled' : 'outlined'}
                />
              </TableCell>
              <TableCell align="right">
                <Tooltip title="Edit">
                  <IconButton size="small" onClick={() => onEdit(u)} aria-label={`Edit ${u.username}`}>
                    <Edit fontSize="small" />
                  </IconButton>
                </Tooltip>
                <Tooltip title="Delete">
                  <IconButton size="small" color="error" onClick={() => onDelete(u)} aria-label={`Delete ${u.username}`}>
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
