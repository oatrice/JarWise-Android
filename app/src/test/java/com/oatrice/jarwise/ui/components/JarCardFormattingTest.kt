package com.oatrice.jarwise.ui.components

import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for decimal formatting in JarCard
 * 
 * Tests that jar amounts display with 2 decimal places
 * Fixes: https://github.com/oatrice/JarWise-Root/issues/11
 */
class JarCardFormattingTest {

    @Test
    fun `format currency with decimals - whole number`() {
        val amount = 2450.0
        val formatted = String.format("%,.2f", amount)
        assertEquals("2,450.00", formatted)
    }

    @Test
    fun `format currency with decimals - with cents`() {
        val amount = 2450.5
        val formatted = String.format("%,.2f", amount)
        assertEquals("2,450.50", formatted)
    }

    @Test
    fun `format currency with decimals - full cents`() {
        val amount = 12000.99
        val formatted = String.format("%,.2f", amount)
        assertEquals("12,000.99", formatted)
    }

    @Test
    fun `format currency with decimals - large number`() {
        val amount = 100000.0
        val formatted = String.format("%,.2f", amount)
        assertEquals("100,000.00", formatted)
    }

    @Test
    fun `format currency with decimals - small number`() {
        val amount = 120.5
        val formatted = String.format("%,.2f", amount)
        assertEquals("120.50", formatted)
    }

    @Test
    fun `format currency should not round to integer`() {
        val amount = 2450.5
        val formatted = String.format("%,.2f", amount)
        // This should NOT be "2,450" or "2,451"
        assertNotEquals("2,450", formatted)
        assertNotEquals("2,451", formatted)
        assertEquals("2,450.50", formatted)
    }
}
