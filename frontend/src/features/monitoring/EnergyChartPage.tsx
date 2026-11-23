import { CalendarMonth, FlashOn } from '@mui/icons-material'
import {
  Box,
  Button,
  CircularProgress,
  MenuItem,
  Paper,
  Select,
  Stack,
  TextField,
  Tooltip,
  Typography,
} from '@mui/material'
import axios from 'axios'
import { jwtDecode } from 'jwt-decode'
import { useEffect, useState } from 'react'
import {
  Bar,
  BarChart,
  CartesianGrid,
  ResponsiveContainer,
  Tooltip as ChartTooltip,
  XAxis,
  YAxis,
} from 'recharts'
import api from '../../shared/api/client'
import type { DeviceDTO } from '../devices/types'
import ErrorMessage from '../../shared/ui/ErrorMessage'
import type { ChartDataPoint, HourlyConsumptionDTO } from './types'

function today(): string {
  return new Date().toISOString().slice(0, 10)
}

function buildChartData(records: HourlyConsumptionDTO[]): ChartDataPoint[] {
  const byHour: Record<number, number> = {}
  for (const r of records) {
    byHour[r.hour] = (byHour[r.hour] ?? 0) + r.totalKwh
  }
  return Array.from({ length: 24 }, (_, h) => ({
    hour: `${String(h).padStart(2, '0')}:00`,
    totalKwh: Math.round((byHour[h] ?? 0) * 1000) / 1000,
  }))
}

const simulatorApi = axios.create({ baseURL: '' })

export default function EnergyChartPage() {
  const [date, setDate] = useState<string>(today())
  const [data, setData] = useState<ChartDataPoint[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [userId, setUserId] = useState<string | null>(null)
  const [devices, setDevices] = useState<DeviceDTO[]>([])
  const [selectedDeviceId, setSelectedDeviceId] = useState<string>('all')
  const [backfilling, setBackfilling] = useState(false)
  const [backfillMsg, setBackfillMsg] = useState<string | null>(null)
  const [refreshTick, setRefreshTick] = useState(0)

  useEffect(() => {
    const token = localStorage.getItem('token')
    if (!token) return
    try {
      const decoded: any = jwtDecode(token)
      api.get(`/users/by-username/${decoded.sub}`).then(res => {
        const uid = res.data.id
        setUserId(uid)
        api.get<DeviceDTO[]>(`/devices/user/${uid}`).then(r => setDevices(r.data))
      })
    } catch {
      setError('Could not resolve user.')
    }
  }, [])

  useEffect(() => {
    if (!userId || !date) return
    setLoading(true)
    setError(null)

    const req = selectedDeviceId === 'all'
      ? api.get<HourlyConsumptionDTO[]>(`/monitoring/user/${userId}`, { params: { date } })
      : api.get<HourlyConsumptionDTO[]>(`/monitoring/device/${selectedDeviceId}`, { params: { date } })

    req
      .then(res => setData(buildChartData(res.data)))
      .catch(() => setError('Could not load energy data.'))
      .finally(() => setLoading(false))
  }, [userId, date, selectedDeviceId, refreshTick])

  async function handleBackfill() {
    setBackfilling(true)
    setBackfillMsg(null)
    try {
      const res = await simulatorApi.post('/simulate/backfill', null, { params: { date } })
      setBackfillMsg(`Sent ${res.data.total} readings. Refreshing...`)
      setTimeout(() => {
        setRefreshTick(t => t + 1)
        setBackfillMsg(null)
      }, 1500)
    } catch {
      setBackfillMsg('Backfill failed — is the simulator running?')
    } finally {
      setBackfilling(false)
    }
  }

  const totalDay = data.reduce((s, d) => s + d.totalKwh, 0)
  const selectedDevice = devices.find(d => d.id === selectedDeviceId)

  return (
    <Box>
      <Stack sx={{ flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h2">Energy Consumption</Typography>
        <Stack sx={{ flexDirection: 'row', alignItems: 'center', gap: 1 }}>
          <CalendarMonth color="action" />
          <TextField
            type="date"
            size="small"
            value={date}
            onChange={e => setDate(e.target.value)}
            slotProps={{ htmlInput: { max: today() } }}
          />
          <Tooltip title="Instantly fill 24h of simulated data for the selected date (demo mode)">
            <span>
              <Button
                variant="outlined"
                size="small"
                startIcon={<FlashOn />}
                onClick={handleBackfill}
                disabled={backfilling}
                color="warning"
              >
                {backfilling ? 'Sending...' : 'Fill Demo Data'}
              </Button>
            </span>
          </Tooltip>
        </Stack>
      </Stack>

      {devices.length > 1 && (
        <Stack sx={{ flexDirection: 'row', alignItems: 'center', gap: 1, mb: 2 }}>
          <Typography variant="body2" color="text.secondary">Device:</Typography>
          <Select
            size="small"
            value={selectedDeviceId}
            onChange={e => setSelectedDeviceId(e.target.value)}
            sx={{ minWidth: 200 }}
          >
            <MenuItem value="all">All devices (combined)</MenuItem>
            {devices.map(d => (
              <MenuItem key={d.id} value={d.id}>
                {d.description} — max {d.maxHourlyEnergyConsumption} kWh/h
              </MenuItem>
            ))}
          </Select>
        </Stack>
      )}

      {backfillMsg && (
        <Typography variant="body2" color="warning.main" sx={{ mb: 1 }}>
          {backfillMsg}
        </Typography>
      )}
      {error && <ErrorMessage>{error}</ErrorMessage>}

      <Paper sx={{ p: 3 }}>
        {loading ? (
          <Box sx={{ display: 'flex', justifyContent: 'center', py: 8 }}>
            <CircularProgress />
          </Box>
        ) : (
          <>
            <Stack sx={{ flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
              <Typography variant="body2" color="text.secondary">
                {selectedDevice
                  ? <><strong>{selectedDevice.description}</strong> on {date}</>
                  : <>All devices on {date}</>
                }
                {' · '}Total: <strong>{totalDay.toFixed(3)} kWh</strong>
                {selectedDevice && (
                  <> · Limit: <strong>{selectedDevice.maxHourlyEnergyConsumption} kWh/h</strong></>
                )}
              </Typography>
              <Button size="small" onClick={() => setRefreshTick(t => t + 1)}>Refresh</Button>
            </Stack>
            <ResponsiveContainer width="100%" height={380}>
              <BarChart data={data} margin={{ top: 5, right: 20, left: 10, bottom: 5 }}>
                <CartesianGrid strokeDasharray="3 3" vertical={false} />
                <XAxis
                  dataKey="hour"
                  tick={{ fontSize: 11 }}
                  interval={1}
                  angle={-45}
                  textAnchor="end"
                  height={50}
                />
                <YAxis unit=" kWh" tick={{ fontSize: 11 }} width={72} />
                <ChartTooltip
                  formatter={(v: number) => [`${v.toFixed(3)} kWh`, 'Consumption']}
                />
                <Bar dataKey="totalKwh" fill="#1565c0" radius={[3, 3, 0, 0]} name="kWh" />
              </BarChart>
            </ResponsiveContainer>
          </>
        )}
      </Paper>
    </Box>
  )
}
