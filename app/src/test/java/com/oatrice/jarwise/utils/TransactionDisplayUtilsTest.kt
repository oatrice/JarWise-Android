package com.oatrice.jarwise.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class TransactionDisplayUtilsTest {

    @Test
    fun `getDisplayDetails - with note`() {
        val jarName = "Necessities"
        val note = "Buying Milk"
        
        val result = TransactionDisplayUtils.getDisplayDetails(jarName, note)
        
        assertEquals("Necessities", result.first) // Title
        assertEquals("Buying Milk", result.second) // Subtitle
    }

    @Test
    fun `getDisplayDetails - empty note`() {
        val jarName = "Savings"
        val note = ""
        
        val result = TransactionDisplayUtils.getDisplayDetails(jarName, note)
        
        assertEquals("Savings", result.first)
        assertEquals("", result.second) // Subtitle empty
    }

    @Test
    fun `getDisplayDetails - blank note`() {
        val jarName = "Savings"
        val note = "   "
        
        val result = TransactionDisplayUtils.getDisplayDetails(jarName, note)
        
        assertEquals("Savings", result.first)
        assertEquals("", result.second)
    }
}
