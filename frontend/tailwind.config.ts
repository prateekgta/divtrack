import type { Config } from 'tailwindcss';

const config: Config = {
  content: [
    './src/**/*.{js,ts,jsx,tsx,mdx}',
  ],
  theme: {
    extend: {
      colors: {
        forest: {
          50: '#f0fdf4',
          100: '#dcfce7',
          200: '#bbf7d0',
          300: '#86efac',
          400: '#4ade80',
          500: '#22c55e',
          600: '#16a34a',
          700: '#15803d',
          800: '#166534',
          900: '#14532d',
        },
        leaf: {
          50: '#fffbeb',
          100: '#fef3c7',
          200: '#fde68a',
          300: '#fcd34d',
          400: '#fbbf24',
          500: '#f59e0b',
          600: '#d97706',
          700: '#b45309',
          800: '#92400e',
          900: '#78350f',
        },
        earth: {
          50: '#faf5ef',
          100: '#f0e6d3',
          200: '#e0ccaa',
          300: '#d0b282',
          400: '#c0985a',
          500: '#8B6914',
          600: '#7A5B12',
          700: '#5C4410',
          800: '#3E2E0E',
          900: '#2A1F0C',
        },
      },
      fontFamily: {
        display: ['Georgia', 'Cambria', 'serif'],
        body: ['Inter', 'system-ui', 'sans-serif'],
      },
      animation: {
        'tree-grow': 'treeGrow 2s ease-out',
        'leaf-fall': 'leafFall 3s ease-in infinite',
      },
      keyframes: {
        treeGrow: {
          '0%': { transform: 'scaleY(0)', opacity: '0' },
          '100%': { transform: 'scaleY(1)', opacity: '1' },
        },
        leafFall: {
          '0%': { transform: 'translateY(-10px) rotate(0deg)', opacity: '1' },
          '100%': { transform: 'translateY(20px) rotate(180deg)', opacity: '0' },
        },
      },
    },
  },
  plugins: [],
};
export default config;
