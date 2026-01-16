package com.oatrice.jarwise.data

import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for GeneratedMockData
 * 
 * Ensures data structure integrity from shared-spec
 * Related to: https://github.com/oatrice/JarWise-Root/issues/8
 */
class GeneratedMockDataTest {

    @Test
    fun `jars list should not be empty`() {
        assertTrue("Jars list should contain data", GeneratedMockData.jars.isNotEmpty())
    }

    @Test
    fun `jars should have 6 items`() {
        assertEquals("Should have 6 jars from shared-spec", 6, GeneratedMockData.jars.size)
    }

    @Test
    fun `transactions list should not be empty`() {
        assertTrue("Transactions list should contain data", GeneratedMockData.transactions.isNotEmpty())
    }

    @Test
    fun `transactions should have 3 items`() {
        assertEquals("Should have 3 transactions from shared-spec", 3, GeneratedMockData.transactions.size)
    }

    @Test
    fun `first jar should be Necessities`() {
        val firstJar = GeneratedMockData.jars.first()
        assertEquals("1", firstJar.id)
        assertEquals("Necessities", firstJar.name)
        assertEquals(2450.5, firstJar.current, 0.01)
        assertEquals(4000.0, firstJar.goal, 0.01)
        assertEquals(4, firstJar.level)
    }

    @Test
    fun `all jars should have valid properties`() {
        GeneratedMockData.jars.forEach { jar ->
            assertNotNull("Jar ID should not be null", jar.id)
            assertNotNull("Jar name should not be null", jar.name)
            assertTrue("Jar current should be >= 0", jar.current >= 0)
            assertTrue("Jar goal should be > 0", jar.goal > 0)
            assertTrue("Jar level should be > 0", jar.level > 0)
            assertNotNull("Jar icon should not be null", jar.icon)
            assertNotNull("Jar color should not be null", jar.color)
        }
    }

    @Test
    fun `all transactions should have valid properties`() {
        GeneratedMockData.transactions.forEach { transaction ->
            assertNotNull("Transaction ID should not be null", transaction.id)
            assertNotNull("Transaction merchant should not be null", transaction.merchant)
            assertNotEquals("Transaction amount should not be zero", 0.0, transaction.amount, 0.01)
            assertNotNull("Transaction category should not be null", transaction.category)
            assertNotNull("Transaction date should not be null", transaction.date)
            assertNotNull("Transaction icon should not be null", transaction.icon)
        }
    }

    @Test
    fun `jar amounts should have decimal precision`() {
        val necessitiesJar = GeneratedMockData.jars.first { it.name == "Necessities" }
        // Verify that decimal values are preserved (not rounded to integers)
        assertEquals(2450.5, necessitiesJar.current, 0.01)
        assertNotEquals(2450.0, necessitiesJar.current, 0.01)
        assertNotEquals(2451.0, necessitiesJar.current, 0.01)
    }
}
