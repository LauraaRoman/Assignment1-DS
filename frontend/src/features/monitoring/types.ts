export interface HourlyConsumptionDTO {
  id: string
  deviceId: string
  consumptionDate: string
  hour: number
  totalKwh: number
}

export interface ChartDataPoint {
  hour: string
  totalKwh: number
}
