package com.oatrice.jarwise.ui.login

import com.oatrice.jarwise.data.auth.AuthUser
import com.oatrice.jarwise.data.auth.AuthService
import com.oatrice.jarwise.data.auth.MockAuthService
import com.oatrice.jarwise.utils.MainDispatcherRule
import org.mockito.kotlin.mock
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var authService: MockAuthService
    private val backupManager: com.oatrice.jarwise.data.backup.BackupManager = mock()
    private lateinit var viewModel: LoginViewModel

    @Before
    fun setup() {
        authService = MockAuthService()
        viewModel = LoginViewModel(authService, backupManager)
    }

    @Test
    fun `initial state is Idle`() = runTest {
        assertEquals(LoginUiState.Idle, viewModel.uiState.value)
    }

    @Test
    fun `onSignInClick updates state to Success on success`() = runTest {
        // Collect flow to ensure updates are processed
        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }

        viewModel.onSignInClick()
        advanceUntilIdle()

        val currentState = viewModel.uiState.value
        assertTrue("State should be Success: $currentState", currentState is LoginUiState.Success)
        assertEquals("Mock User", (currentState as LoginUiState.Success).user.name)
    }
}
