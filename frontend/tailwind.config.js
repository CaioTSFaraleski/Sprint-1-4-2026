/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{ts,tsx}'],
  theme: {
    extend: {
      colors: {
        marca: {
          50: '#FFF8E6',
          100: '#FFEDB8',
          400: '#FBBF24',
          500: '#F59E0B',
          600: '#D97706',
        },
        painel: {
          900: '#0B1220',
          800: '#111A2E',
          700: '#1B2640',
          600: '#26334F',
        },
        risco: {
          baixo: '#15803D',
          medio: '#B45309',
          alto: '#C2410C',
          critico: '#B91C1C',
        },
      },
      fontFamily: {
        sans: ['Inter', 'ui-sans-serif', 'system-ui', 'Segoe UI', 'Roboto', 'sans-serif'],
      },
      boxShadow: {
        cartao: '0 1px 2px rgba(15, 23, 42, 0.06), 0 1px 3px rgba(15, 23, 42, 0.08)',
      },
    },
  },
  plugins: [],
};
