package com.oatrice.jarwise.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [Transaction::class, JarConfig::class], version = 4, exportSchema = true)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun jarConfigDao(): JarConfigDao

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
    }
}

