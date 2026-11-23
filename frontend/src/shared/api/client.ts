import axios from 'axios'

const api = axios.create()

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')  // ia tokenul din localStorage
  if (token && config.headers) {
    config.headers.Authorization = `Bearer ${token}`     //il pune in header
  }
  return config    //lasa requestul sa continue
})

export default api
