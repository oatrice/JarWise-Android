package com.oatrice.jarwise.ui.login

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

    fun onSignInClick() {
        _uiState.value = LoginUiState.Loading
        viewModelScope.launch {
            val result = authService.signIn()
            result.onSuccess { user ->
                _uiState.value = LoginUiState.Success(user)
            }.onFailure { error ->
                _uiState.value = LoginUiState.Error(error.message ?: "Unknown error")
            }
        }
    }
}
