package com.oatrice.jarwise.data

import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
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
        AppDatabase::class.java.canonicalName,
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    @Throws(IOException::class)
    fun migrate4To5() {
        // Create DB at version 4
        var db = helper.createDatabase(TEST_DB, 4).apply {
            // Insert 6 default jars into jar_configs
            // jar_configs: id, name, percentage, colorName, iconName
            execSQL("INSERT INTO jar_configs VALUES ('1', 'Necessities', 55, 'blue', 'home')")
            execSQL("INSERT INTO jar_configs VALUES ('2', 'Financial Freedom', 10, 'green', 'dollar-sign')")
            
            close()
        }

        // Migrate to version 5
        db = helper.runMigrationsAndValidate(TEST_DB, 5, true, AppDatabase.MIGRATION_4_5)

        // Validate allocations table
        val cursor = db.query("SELECT * FROM allocations ORDER BY sortOrder ASC")
        
        // Expect 2 rows
        assertEquals("Should have 2 rows migrated", 2, cursor.count)
        
        // Row 1: Necessities
        assertTrue(cursor.moveToNext())
        // userId should be 'local_user'
        assertEquals("local_user", cursor.getString(cursor.getColumnIndex("userId")))
        assertEquals("Necessities", cursor.getString(cursor.getColumnIndex("name")))
        assertEquals(0, cursor.getInt(cursor.getColumnIndex("level")))
        assertEquals(55, cursor.getInt(cursor.getColumnIndex("targetPercent")))
        // parentId should be NULL (not present in cursor usually if null, checking column index > -1)
        val parentIdIndex = cursor.getColumnIndex("parentId")
        assertTrue(cursor.isNull(parentIdIndex))
        assertEquals(1, cursor.getInt(cursor.getColumnIndex("sortOrder"))) // id '1' -> 1
        
        // Row 2: Financial Freedom
        assertTrue(cursor.moveToNext())
        assertEquals("Financial Freedom", cursor.getString(cursor.getColumnIndex("name")))
        assertEquals(10, cursor.getInt(cursor.getColumnIndex("targetPercent")))
        assertEquals(2, cursor.getInt(cursor.getColumnIndex("sortOrder")))
        
        cursor.close()
    }
}
