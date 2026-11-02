export interface DeviceDTO {
  id: string
  description: string
  address: string
  maxHourlyEnergyConsumption: number
  userId: string
}

export interface UserDTO {
  id: string
  username: string
  role: string
}
