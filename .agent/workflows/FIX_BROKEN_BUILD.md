# 🔧 Fix Broken Build Workflow

กระบวนการแก้ไขปัญหาเมื่อ Build พัง (Web Focus)

## Web (React + Vite)
หาก `npm run build` ล้มเหลว:
1.  **Clear Cache**: `rm -rf node_modules package-lock.json && npm install`
2.  **Type Check**: รัน `npm run type-check`
3.  **Dependency Conflict**: ตรวจสอบ `package.json`
