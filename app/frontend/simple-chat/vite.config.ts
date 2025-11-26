import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

// https://vitejs.dev/config/
export default defineConfig({
    plugins: [react()],
    build: {
        outDir: "./build",
        emptyOutDir: true,
        sourcemap: true,
        rollupOptions: {
            output: {
                manualChunks: id => {
                    if (id.includes("@fluentui/react-icons")) {
                        return "fluentui-icons";
                    } else if (id.includes("@fluentui/react")) {
                        return "fluentui-react";
                    } else if (id.includes("node_modules")) {
                        return "vendor";
                    }
                }
            }
        },
        target: "esnext"
    },
    server: {
        proxy: {
            "/api/ask": {
                target: 'http://localhost:8080',
                changeOrigin: true
            },
            "/api/chat": {
                target: 'http://localhost:8080',
                changeOrigin: true
            },
            "/api/content": {
                target: 'http://localhost:8080',
                changeOrigin: true
            },
            "/api/auth_setup": {
                 target: 'http://localhost:8080',
                 changeOrigin: true
                        }
        }
    }
});
