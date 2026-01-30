package com.oatrice.jarwise.data

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class MigrationTest {

    private val TEST_DB = "migration-test"

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java.canonicalName!!,
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    @Throws(IOException::class)
    fun migrate2To3() {
        var db = helper.createDatabase(TEST_DB, 2).apply {
            // Insert data manually for version 2
            execSQL("INSERT INTO transactions (id, amount, note, jarId, date, type, status) VALUES (1, 100.0, 'Test Tx', 'necessities', '2024-01-01', 'expense', 'completed')")
            close()
        }

        // Migrate to version 3
        db = helper.runMigrationsAndValidate(TEST_DB, 3, true, AppDatabase.MIGRATION_2_3)

        // Query to validate data survival and new column default value
        val cursor = db.query("SELECT * FROM transactions WHERE id = 1")
        cursor.moveToFirst()
        
        // Check new column 'walletId' exists and has default value
        val walletIdIndex = cursor.getColumnIndex("walletId")
        val walletId = cursor.getString(walletIdIndex)
        
        assert(walletId == "wallet-cash")
        
        cursor.close()
    }
}
