package com.oatrice.jarwise.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [Transaction::class, JarConfig::class, Allocation::class], version = 5, exportSchema = true)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun jarConfigDao(): JarConfigDao
    abstract fun allocationDao(): AllocationDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE transactions ADD COLUMN type TEXT NOT NULL DEFAULT 'expense'")
                db.execSQL("ALTER TABLE transactions ADD COLUMN status TEXT NOT NULL DEFAULT 'completed'")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE transactions ADD COLUMN walletId TEXT NOT NULL DEFAULT 'wallet-cash'")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS jar_configs (
                        id TEXT PRIMARY KEY NOT NULL,
                        name TEXT NOT NULL,
                        percentage INTEGER NOT NULL,
                        colorName TEXT NOT NULL,
                        iconName TEXT NOT NULL
                    )
                """.trimIndent())
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Create allocations table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `allocations` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                        `userId` TEXT NOT NULL, 
                        `name` TEXT NOT NULL, 
                        `parentId` INTEGER, 
                        `level` INTEGER NOT NULL, 
                        `targetPercent` INTEGER, 
                        `icon` TEXT NOT NULL, 
                        `color` TEXT NOT NULL, 
                        `sortOrder` INTEGER NOT NULL, 
                        `isSystemDefault` INTEGER NOT NULL, 
                        `isActive` INTEGER NOT NULL, 
                        FOREIGN KEY(`parentId`) REFERENCES `allocations`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                """.trimIndent())

                // Create Index
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_allocations_userId_parentId` ON `allocations` (`userId`, `parentId`)")

                // Migrate data from jar_configs to allocations (as system default jars)
                // Note: jar_configs.id is String '1'-'6', we need to map to Long id
                // We'll migrate them as new allocations with level=0, parentId=NULL
                db.execSQL("""
                    INSERT INTO allocations (userId, name, level, targetPercent, icon, color, sortOrder, isSystemDefault, isActive)
                    SELECT 
                        'local_user', 
                        name, 
                        0, 
                        percentage, 
                        iconName, 
                        colorName, 
                        CAST(id AS INTEGER), 
                        1, 
                        1 
                    FROM jar_configs
                """.trimIndent())
            }
        }

        val SEED_CALLBACK = object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                val jars = listOf(
                    Triple("Necessities", 55, "home"),
                    Triple("Financial Freedom", 10, "dollar-sign"),
                    Triple("Long-term Savings", 10, "piggy-bank"),
                    Triple("Education", 10, "book-open"),
                    Triple("Play", 10, "smile"),
                    Triple("Give", 5, "gift")
                )

                jars.forEachIndexed { index, (name, percent, icon) ->
                    db.execSQL("""
                        INSERT INTO allocations (userId, name, level, targetPercent, icon, color, sortOrder, isSystemDefault, isActive)
                        VALUES ('local_user', '$name', 0, $percent, '$icon', 'blue', ${index + 1}, 1, 1)
                    """.trimIndent())
                }
            }
        }
    }
}

