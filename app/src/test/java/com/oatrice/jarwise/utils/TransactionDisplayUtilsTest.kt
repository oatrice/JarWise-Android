package com.oatrice.jarwise.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class TransactionDisplayUtilsTest {

    @Test
    fun `getDisplayDetails - with note`() {
        val jarName = "Necessities"
        val note = "Buying Milk"
        
        val result = TransactionDisplayUtils.getDisplayDetails(jarName, note)
        
        assertEquals("Buying Milk", result.first) // Title is Note
        assertEquals("Necessities", result.second) // Subtitle is Jar Name
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
