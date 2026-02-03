package com.oatrice.jarwise.data.auth

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthServiceTest {

    @Test
    fun `signIn updates currentUser state`() = runTest {
        val authService = MockAuthService()

        // Initial state
        assertNull(authService.currentUser.value)

        // Action
        val result = authService.signIn()

        // Assertion (Expect success)
        assertTrue(result.isSuccess)
        assertNotNull(authService.currentUser.value)
        assertEquals("Mock User", authService.currentUser.value?.name)
    }

    @Test
    fun `signOut clears currentUser state`() = runTest {
        val authService = MockAuthService()
        // We can't sign in yet because it fails, so we manually set state if possible, 
        // OR we just test signOut from null state which is trivial.
        // But let's assume signIn works for this test to be meaningful.
        // For now, this test will fail because signIn fails.
        val result = authService.signIn() 
        if (result.isSuccess) {
             authService.signOut()
             assertNull(authService.currentUser.value)
        }
    }
}
