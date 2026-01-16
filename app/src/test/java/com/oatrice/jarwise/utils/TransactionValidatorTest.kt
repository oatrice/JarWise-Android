package com.oatrice.jarwise.utils

import org.junit.Assert.*
import org.junit.Test

class TransactionValidatorTest {

    @Test
    fun `validateTransaction - valid inputs - returns valid`() {
        val result = TransactionValidator.validateTransaction(
            amount = "100.0",
            jarId = "NECESSITY"
        )
        assertTrue(result.isValid)
        assertTrue(result.errors.isEmpty())
    }

    @Test
    fun `validateTransaction - empty amount - returns error`() {
        val result = TransactionValidator.validateTransaction(
            amount = "",
            jarId = "NECESSITY"
        )
        assertFalse(result.isValid)
        assertEquals("Amount is required", result.errors["amount"])
    }

    @Test
    fun `validateTransaction - zero amount - returns error`() {
        val result = TransactionValidator.validateTransaction(
            amount = "0",
            jarId = "NECESSITY"
        )
        assertFalse(result.isValid)
        assertEquals("Amount must be greater than 0", result.errors["amount"])
    }

    @Test
    fun `validateTransaction - negative amount - returns error`() {
        val result = TransactionValidator.validateTransaction(
            amount = "-50",
            jarId = "NECESSITY"
        )
        assertFalse(result.isValid)
        assertEquals("Amount must be greater than 0", result.errors["amount"])
    }

    @Test
    fun `validateTransaction - non numeric amount - returns error`() {
        val result = TransactionValidator.validateTransaction(
            amount = "abc",
            jarId = "NECESSITY"
        )
        assertFalse(result.isValid)
        assertEquals("Amount must be greater than 0", result.errors["amount"])
    }

    @Test
    fun `validateTransaction - empty jarId - returns error`() {
        val result = TransactionValidator.validateTransaction(
            amount = "100",
            jarId = ""
        )
        assertFalse(result.isValid)
        assertEquals("Please select a jar", result.errors["jarId"])
    }

    @Test
    fun `validateTransaction - blank jarId - returns error`() {
        val result = TransactionValidator.validateTransaction(
            amount = "100",
            jarId = "   "
        )
        assertFalse(result.isValid)
        assertEquals("Please select a jar", result.errors["jarId"])
    }
}
