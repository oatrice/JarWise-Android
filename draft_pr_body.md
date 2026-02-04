# 📋 Summary
This PR implements comprehensive transaction linking and transfer functionality for JarWise, allowing users to track money movements between wallets and jars. The feature introduces bidirectional transaction linking, a dedicated transfer flow with a new UI, and improved transaction type support with proper domain layer architecture.

## ✅ Checklist
- [x] 🏗️ I have moved the related issue to "In Progress" on the Kanban board

# 🎯 Type
- [ ] 🐛 Bug fix
- [x] ✨ New feature
- [ ] ⚡ Performance improvement
- [x] 🔧 Refactoring
- [x] 🎨 UI Update (Jetpack Compose)
- [ ] 🤖 SDK/Dependency Update
- [ ] 💥 Breaking change

# 📱 Android Specific Checks
- [x] Verified on Emulator
- [ ] Verified on Real Device
- [x] Screen Orientation Support (Portrait/Landscape)
- [x] Dark/Light Mode Tested

# 📝 Changes

## 🏗️ Architecture & Domain Layer
- **New Domain Module**: Added `DomainModule.kt` with dependency injection for use cases
- **Use Cases**:
  - `CreateTransferUseCase`: Handles atomic creation of linked expense/income transaction pairs
  - `UnlinkTransactionsUseCase`: Manages unlinking of transaction relationships
- **Repository Pattern**: Implemented `TransactionRepository` interface with `TransactionRepositoryImpl` for better separation of concerns

## 💾 Data Layer
- **Database Migration (6→7)**: Added `linkedTransactionId` field to transactions table
- **Enhanced TransactionDao**: 
  - New methods for unlinking transactions
  - Optimized linked transaction lookups with proper SQL queries
- **Transaction Entity**: Added `linkedTransactionId: String?` field to support bidirectional linking

## 🎨 UI Enhancements
- **AddTransactionScreen**:
  - Added transaction type selector (Expense/Income/Transfer)
  - Transfer mode with source and destination wallet/jar selection
  - Dynamic UI that adapts based on selected transaction type
  - Improved form validation and state management
- **TransactionCard**: 
  - Visual indicators for transfer transactions
  - Shows source → destination for linked transactions
  - Enhanced display with transfer-specific styling
- **DashboardScreen**: Updated to handle navigation to transfer flow
- **TransactionHistoryScreen**: Improved grouping and display of linked transactions

## 🔧 Core Functionality
- **Bidirectional Linking**: Expense and income transactions are atomically linked using database transactions
- **Transfer Flow**: Complete workflow for moving money between wallets/jars
- **Type Support**: Added `transactionType` field with support for "expense", "income", and "transfer"
- **Total Spent Calculation Fix**: Optimized to exclude linked transactions and prevent double-counting
- **Navigation Improvements**: Added previous screen tracking for better UX flow

## 🧪 Testing
- Added `CreateTransferUseCaseTest.kt` with comprehensive test coverage

## 📦 Other Updates
- Updated app version: `1.8.0` → `1.9.0`
- Updated dependency injection configuration across all modules
- Updated CHANGELOG.md with new features
- Code review improvements and refactoring

# 📸 UI/UX Screenshots
<!-- Include screenshots from the Android device/emulator showing:
- Transfer transaction type selection
- Source/Destination wallet selection
- Completed transfer in transaction list
- TransactionCard showing transfer indicator
-->

# 🧪 Testing
- [x] `./gradlew build` passes
- [x] Unit Tests pass
  - CreateTransferUseCaseTest: Tests atomic transfer creation and linking
  - Repository layer tests for transaction operations

# 🚀 Migration/Deployment
- [x] Database migration required (Room)
- [ ] Environment variables/Secrets updated
- [x] New Dependencies added

```bash
# Database Migration 6→7
# Automatically handled by Room migration
# Adds linkedTransactionId field to transactions table
# Migration runs on app upgrade
```

## Database Schema Changes
```sql
ALTER TABLE transactions ADD COLUMN linkedTransactionId TEXT;
ALTER TABLE transactions ADD COLUMN transactionType TEXT NOT NULL DEFAULT 'expense';
```

# 🔗 Related Issues
- Closes https://github.com/oatrice/JarWise-Root/issues/71

**Breaking Changes**: No  
**Migration Required**: Yes (Database schema v6→v7)

---

## 🎯 Key Highlights
- ✅ Atomic transfer operations with database transactions for data consistency
- ✅ Clean architecture with domain layer separation
- ✅ Comprehensive transfer UI with type selection
- ✅ Optimized SQL queries for linked transaction lookups
- ✅ Proper dependency injection setup
- ✅ Test coverage for critical use cases

## 📊 Impact
**Files Changed**: 29 files  
**Insertions**: +1060 lines  
**Deletions**: -318 lines

This feature significantly improves JarWise's money management capabilities by allowing users to accurately track fund movements between different accounts and jars, providing better financial visibility and organization.