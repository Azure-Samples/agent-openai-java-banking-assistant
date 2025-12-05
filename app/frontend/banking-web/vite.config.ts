import { defineConfig } from "vite";
import react from "@vitejs/plugin-react-swc";
import path from "path";

const backendTarget = process.env.BACKEND_URL ?? "http://127.0.0.1:8000";

// https://vitejs.dev/config/
export default defineConfig(({ mode }) => ({
  server: {
    port: 5170,
    host: "0.0.0.0",
    proxy: {
      // Proxy API requests to local backend server on port 8080
      '/api/accounts/': {
        target: 'http://localhost:8070',
        changeOrigin: true
      },
      '/api/transactions/': {
        target: 'http://localhost:8071',
        changeOrigin: true
      },
       "/chatkit": {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
      "/upload": {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
      "/preview": {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
  plugins: [
    react()
  ].filter(Boolean),
  resolve: {
    alias: {
      "@": path.resolve(__dirname, "./src"),
    },
  },
}));
