import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      '/auth': {
        target: 'http://localhost', // Adresa API Gateway-ului NGINX
        changeOrigin: true
      },
      '/users': {
        target: 'http://localhost',
        changeOrigin: true
      },
      '/devices': {
        target: 'http://localhost',
        changeOrigin: true
      }
    }
  }
})