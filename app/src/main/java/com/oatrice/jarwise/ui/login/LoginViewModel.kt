package com.oatrice.jarwise.ui.login

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oatrice.jarwise.data.auth.AuthService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authService: AuthService
) : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun getSignInIntent(): android.content.Intent {
        // Safe cast or interface change required. For now, assuming GoogleAuthService is injected as AuthService
        // logic to get intent. Ideally AuthService has this, or we cast.
        // But AuthService interface doesn't have it.
        // Let's modify AuthService interface to support Intent flow or check type.
        return (authService as? com.oatrice.jarwise.data.auth.GoogleAuthService)?.getSignInIntent() 
            ?: android.content.Intent() // Fallback or throw
    }

    fun handleSignInResult(intent: android.content.Intent?) {
        _uiState.value = LoginUiState.Loading
        viewModelScope.launch {
            if (authService is com.oatrice.jarwise.data.auth.GoogleAuthService) {
                val result = authService.handleSignInResult(intent)
                result.onSuccess { user ->
                    Log.d("LoginViewModel", "Login Success: ${user.email}")
                    _uiState.value = LoginUiState.Success(user)
                }.onFailure { error ->
                    Log.e("LoginViewModel", "Login Failed", error)
                    _uiState.value = LoginUiState.Error(error.message ?: "Sign in failed")
                }
            } else {
                 // Fallback for Mock flow if needed, or generic error
                 // If Mock, we might just call signIn() directly in onSignInClick?
                 if (authService is com.oatrice.jarwise.data.auth.MockAuthService) {
                     val result = authService.signIn()
                     result.onSuccess { user -> _uiState.value = LoginUiState.Success(user) }
                 }
            }
        }
    }
    
    fun handleSignInCancelled() {
        _uiState.value = LoginUiState.Error("Sign in cancelled")
    }

    fun onSignInClick() {
        // If Mock, just do direct sign in (simulated)
        if (authService is com.oatrice.jarwise.data.auth.MockAuthService) {
            _uiState.value = LoginUiState.Loading
            viewModelScope.launch {
                val result = authService.signIn()
                 result.onSuccess { user -> _uiState.value = LoginUiState.Success(user) }
            }
        }
        // If Google, the UI should observe a "LaunchIntent" event or similar.
        // But simplest pattern: UI calls viewModel.getSignInIntent() directly in onClick?
        // Let's keep onSignInClick for Mock, and add specific handling in UI for Google.
    }
}
