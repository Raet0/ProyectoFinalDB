/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    "./src/**/*.{html,ts}",
  ],
  darkMode: 'class', // Importante para el modo oscuro
  theme: {
    extend: {
      colors: {
        "primary": "#135bec",
        "background-light": "#f6f6f8",
        "background-dark": "#101622",
        // Colores específicos del dashboard
        "sidebar-bg": "#111318", 
        "card-bg": "#1c1f27",
      },
      fontFamily: {
        "display": ["Newsreader", "serif"],
        "sans": ["Noto Sans", "sans-serif"]
      }
    },
  },
  plugins: [
    require('@tailwindcss/forms'),
    require('@tailwindcss/container-queries'),
  ],
}