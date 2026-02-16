import type { Config } from 'tailwindcss'

export default {
  content: ['./index.html', './src/**/*.{vue,js,ts,jsx,tsx}'],
  theme: {
    extend: {
      colors: {
        primary: {
          50: '#f0f9f4',
          100: '#ddf1e5',
          200: '#c1e6d2',
          300: '#8ad0a6',
          400: '#5ab882',
          500: '#2d9d5e',
          600: '#1a7a42',
          700: '#155f35',
          800: '#124c2b',
          900: '#0f3d23',
        },
      },
      borderRadius: {
        DEFAULT: '10px',
      },
    },
  },
  plugins: [],
} satisfies Config
