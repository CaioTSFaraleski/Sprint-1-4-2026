import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// Em desenvolvimento, /api e /ws são encaminhados para a API Spring Boot (porta 8080),
// então o navegador fala com uma origem só e não há CORS.
const API = process.env.VITE_API_URL ?? 'http://localhost:8080';

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/api': { target: API, changeOrigin: true },
      '/ws': { target: API.replace(/^http/, 'ws'), ws: true, changeOrigin: true },
    },
  },
  build: {
    rollupOptions: {
      output: {
        manualChunks: { graficos: ['recharts'], react: ['react', 'react-dom', 'react-router-dom'] },
      },
    },
  },
  test: {
    environment: 'node',
  },
});
