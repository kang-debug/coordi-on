import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import { VitePWA } from 'vite-plugin-pwa';

export default defineConfig({
  plugins: [
    react(),
    VitePWA({
      registerType: 'autoUpdate',
      manifest: {
        name: 'coordi-on',
        short_name: '코디온',
        description: 'Our awesome Vite-powered PWA!',
        theme_color: '#ffffff',
        icons: [
          {
            src: "./icons/favicon-16x16.png",
            sizes: "16x16",
            type: "image/png"
          },
          {
            "src": "./icons/favicon-32x32",
            sizes: "32x32",
            type: "image/png"
          },
          {
            src: "./icons/favicon.ico",
            sizes: "144x144",
            type: "image/png"
          }
        ]
      }
    })
  ]
});