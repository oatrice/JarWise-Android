# 🤖 Update Android Version

การอัปเดตเวอร์ชันแอป Android (ทั้ง Native และ Flutter)

## 1. Native Android (`/Android`)
- แก้ไข `build.gradle (Module: app)`:
  - `versionCode`: +1
  - `versionName`: เป็นเวอร์ชันใหม่ (เช่น 1.2.0)
- Sync Gradle

## 2. Flutter Android (`/Mobile`)
- แก้ไข `pubspec.yaml`:
  - `version: 1.2.0+5` (Major.Minor.Patch + BuildNumber)
- รัน `flutter pub get`

## 3. Web PWA (`/Web`)
- อัปเดต `package.json`: `"version": "1.2.0"`
