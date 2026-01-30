package com.oatrice.jarwise.data

import org.junit.Assert.*
import org.junit.Test

/**
 * TDD Tests for Draft Transaction functionality
 * Issue #46: Draft Transaction Review
 */
class TransactionDraftTest {

    // ===========================================
    // 🟥 RED Phase: Tests for Transaction Status
    // ===========================================

    @Test
    fun `transaction default status should be completed`() {
        val transaction = Transaction(
            amount = 100.0,
            note = "Test",
            jarId = "necessities",
            date = "2026-01-30T12:00:00.000Z"
        )
        assertEquals("completed", transaction.status)
    }

    @Test
    fun `transaction with draft status should be identified correctly`() {
        val draft = Transaction(
            amount = 100.0,
            note = "Test Draft",
            jarId = "necessities",
            date = "2026-01-30T12:00:00.000Z",
            status = "draft"
        )
        assertEquals("draft", draft.status)
    }

    @Test
    fun `transaction default type should be expense`() {
        val transaction = Transaction(
            amount = 100.0,
            note = "Test",
            jarId = "necessities",
            date = "2026-01-30T12:00:00.000Z"
        )
        assertEquals("expense", transaction.type)
    }

    @Test
    fun `income transaction should have type income`() {
        val income = Transaction(
            amount = 5000.0,
            note = "Salary",
            jarId = "income",
            date = "2026-01-30T12:00:00.000Z",
            type = "income"
        )
        assertEquals("income", income.type)
    }

    @Test
    fun `draft transaction can be updated to completed`() {
        val draft = Transaction(
            id = 1,
            amount = 100.0,
            note = "Test Draft",
            jarId = "necessities",
            date = "2026-01-30T12:00:00.000Z",
            status = "draft"
        )
        
        val completed = draft.copy(status = "completed")
        
        assertEquals("draft", draft.status)
        assertEquals("completed", completed.status)
        assertEquals(draft.id, completed.id)
    }
}
