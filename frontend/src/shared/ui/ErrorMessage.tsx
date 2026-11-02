import { Alert } from '@mui/material'

export default function ErrorMessage({ children }: { children: string }) {
  return <Alert severity="error" sx={{ borderRadius: 2 }}>{children}</Alert>
}
