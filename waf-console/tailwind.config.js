/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        // WAF Console Dark Theme Colors
        'bg-primary': '#1b2431',
        'accent-primary': '#1ec997',
        'danger': '#ff6b6b',
        'warning': '#feca57',
        'info': '#48cae4',
        'success': '#1ec997',
        'bg-card': '#242c3a',
        'bg-surface': '#2c3545',
        'text-primary': '#ffffff',
        'text-secondary': '#a8b2c1',
        'border': '#3a4553',
      }
    },
  },
  plugins: [],
}