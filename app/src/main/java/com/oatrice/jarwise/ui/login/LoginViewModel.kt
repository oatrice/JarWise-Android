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
    private val authService: AuthService,
    private val backupManager: com.oatrice.jarwise.data.backup.BackupManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun getSignInIntent(): android.content.Intent {
        return (authService as? com.oatrice.jarwise.data.auth.GoogleAuthService)?.getSignInIntent() 
            ?: android.content.Intent()
    }

    fun handleSignInResult(intent: android.content.Intent?) {
        _uiState.value = LoginUiState.Loading
        viewModelScope.launch {
            if (authService is com.oatrice.jarwise.data.auth.GoogleAuthService) {
                val result = authService.handleSignInResult(intent)
                result.onSuccess { user ->
                    Log.d("LoginViewModel", "Login Success: ${user.email}")
                    checkForBackup(user)
                }.onFailure { error ->
                    Log.e("LoginViewModel", "Login Failed", error)
                    _uiState.value = LoginUiState.Error(error.message ?: "Sign in failed")
                }
            } else {
                 if (authService is com.oatrice.jarwise.data.auth.MockAuthService) {
                     val result = authService.signIn()
                     result.onSuccess { user -> _uiState.value = LoginUiState.Success(user) }
                 }
            }
        }
    }
    
    private suspend fun checkForBackup(user: com.oatrice.jarwise.data.auth.AuthUser) {
        val backupResult = backupManager.checkForBackup()
        backupResult.onSuccess { backups ->
            if (backups.isNotEmpty()) {
                val latest = backups.maxByOrNull { it.createdTime }!!
                val date = java.text.SimpleDateFormat("dd MMM yyyy, HH:mm", java.util.Locale.getDefault())
                    .format(java.util.Date(latest.createdTime))
                _uiState.value = LoginUiState.RestoreAvailable(user, latest.name, date, latest.id)
            } else {
                _uiState.value = LoginUiState.Success(user)
            }
        }.onFailure {
            // Backup check failed, just proceed to dashboard log (silent fail for user UX)
             Log.e("LoginViewModel", "Failed to check backups", it)
            _uiState.value = LoginUiState.Success(user)
        }
    }
    
    fun onRestoreConfirmed(fileId: String) {
        _uiState.value = LoginUiState.RestoreInProgress
        viewModelScope.launch {
            val result = backupManager.restoreBackup(fileId)
            result.onSuccess {
                _uiState.value = LoginUiState.RestoreSuccess
            }.onFailure {
                _uiState.value = LoginUiState.Error("Restore failed: ${it.message}")
            }
        }
    }
    
    fun onRestoreCancelled(user: com.oatrice.jarwise.data.auth.AuthUser) {
        _uiState.value = LoginUiState.Success(user)
    }
    
    fun handleSignInCancelled() {
        _uiState.value = LoginUiState.Error("Sign in cancelled")
    }

    fun onSignInClick() {
        if (authService is com.oatrice.jarwise.data.auth.MockAuthService) {
            _uiState.value = LoginUiState.Loading
            viewModelScope.launch {
                val result = authService.signIn()
                 result.onSuccess { user -> _uiState.value = LoginUiState.Success(user) }
            }
        }
    }
}
