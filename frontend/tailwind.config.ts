import type { Config } from "tailwindcss";
export default {
  content: ["./index.html", "./src/**/*.{ts,tsx}"],
  theme: {
    extend: {
      colors: {
        primary: { 50:"#ecfdf5",100:"#d1fae5",500:"#10b981",600:"#059669",700:"#047857" },
        secondary: { 600:"#475569" },
        accent: { 500:"#f59e0b" },
        danger: "#dc2626",
        success: "#16a34a",
      },
      fontFamily: {
        display: ["Sora","system-ui","sans-serif"],
        sans: ["Inter","system-ui","sans-serif"],
      },
    },
  },
  plugins: [],
} satisfies Config;
