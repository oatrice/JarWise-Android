# [Web | Android] Manage Jars (Edit %, Name, Icon)

<!-- Paste your generated PR description here -->
## 🚀 Manage Jars & Database Refactoring (v4)

### 📋 Summary
เพิ่มฟีเจอร์ **Manage Jars** ที่ช่วยให้ผู้ใช้สามารถปรับแต่ง ชื่อ (Name), สัดส่วนเปอร์เซ็นต์ (Percentage), และไอคอน (Icon) ของกระปุกออมเงินได้เอง นอกจากนี้ยังมีการปรับปรุงระบบ Navigation, การแสดงผลหน้า History และการอัปเกรด Database Schema เป็นเวอร์ชัน 4

---

### 🎯 Type of Change
- [x] ✨ New feature (Manage Jars)
- [x] 🔧 Refactoring (Navigation System)
- [x] 🎨 UI Update (Jetpack Compose)
- [x] 💥 Breaking change (Database Migration v4)

---

### 📝 Key Changes

#### 1. Jar Management System
* เพิ่มหน้า `ManageJarsScreen` สำหรับแก้ไขรายละเอียดกระปุก
* สร้าง `JarConfig` Entity และ DAO เพื่อแยกการตั้งค่าออกจากข้อมูลธุรกรรม
* เพิ่มระบบคำนวณสัดส่วน % ให้รวมกันครบ **100%**

#### 2. Database & Migrations
* อัปเกรด Room Database เป็น **version 4**
* **Migration Paths:** 1 → 2 (type/status), 2 → 3 (walletId), 3 → 4 (jar_configs)
* เพิ่ม `MigrationTest` เพื่อรับประกันความถูกต้องของข้อมูล

#### 3. UI/UX & Navigation Refactoring
* ปรับปรุง Navigation เป็นแบบ **Unified System** ใน `MainActivity`
* แยก Component `BottomNav` และ `DashboardTopBar` ให้เรียกใช้งานง่ายขึ้น
* **History Page:** เพิ่มการจัดกลุ่มธุรกรรมตามวันที่ (Grouping) พร้อมแสดงยอดรวมรายวัน

#### 4. Infrastructure & Testing
* เพิ่ม `ManageJarsViewModelTest` และ `TransactionGroupingUtilsTest`
* เพิ่ม Dependency: **JUnit4, Robolectric, และ Coroutines Test**

---

### 🧪 Testing & Verification
* [x] `./gradlew build` passes
* [x] Unit Tests ผ่านทั้งหมด (Database migrations & ViewModel logic)
* [x] ทดสอบแล้วบน Emulator และเครื่องจริง (Real Device)
* [x] รองรับทั้ง Portrait/Landscape และ Dark/Light Mode

**Commands for manual check:**
```bash
./gradlew test
./gradlew connectedAndroidTest
```

### 📸 Screenshots
Manage Jars Screen,Dashboard,History (Grouping)
[Insert Image],[Insert Image],[Insert Image]

### 🔗 Related Issues & Metadata
* Closes: #17
* Breaking Changes: Yes (Database Schema Change)
* Migration Required: Yes (Room Migration 1 to 4)

Created by JarWise-Root Team