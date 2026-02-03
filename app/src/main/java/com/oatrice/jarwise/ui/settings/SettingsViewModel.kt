package com.oatrice.jarwise.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oatrice.jarwise.data.auth.AuthService
import com.oatrice.jarwise.data.backup.BackupManager
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow

class SettingsViewModel(
    private val authService: AuthService,
    private val backupManager: BackupManager
) : ViewModel() {

    val user = authService.currentUser
    val syncStatus = backupManager.syncStatus

    fun triggerBackup() {
        backupManager.triggerManualBackup() 
    }

    fun signOut(clearData: Boolean = false) {
        viewModelScope.launch {
            if (clearData) {
                backupManager.clearLocalData()
            }
            authService.signOut()
            resetState()
            
            if (clearData) {
                _restartAppEvent.emit(Unit)
            }
        }
    }


    fun getSignInIntent(): android.content.Intent {
        return (authService as? com.oatrice.jarwise.data.auth.GoogleAuthService)?.getSignInIntent() 
            ?: android.content.Intent()
    }

    sealed class SettingsUiState {
        data object Idle : SettingsUiState()
        data class RestoreAvailable(val fileId: String, val backupDate: String) : SettingsUiState()
        data object RestoreInProgress : SettingsUiState()
        data object RestoreSuccess : SettingsUiState()
    }

    private val _uiState = kotlinx.coroutines.flow.MutableStateFlow<SettingsUiState>(SettingsUiState.Idle)
    val uiState: kotlinx.coroutines.flow.StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _restartAppEvent = kotlinx.coroutines.flow.MutableSharedFlow<Unit>()
    val restartAppEvent = _restartAppEvent.asSharedFlow()

    fun handleSignInResult(intent: android.content.Intent?) {
        viewModelScope.launch {
            val result = if (authService is com.oatrice.jarwise.data.auth.GoogleAuthService) {
                authService.handleSignInResult(intent)
            } else {
                (authService as? com.oatrice.jarwise.data.auth.MockAuthService)?.signIn()
                Result.success(authService.currentUser.value!!)
            }

            result.onSuccess {
                // Check for backup
                val backupResult = backupManager.checkForBackup()
                val backups = backupResult.getOrNull()
                if (!backups.isNullOrEmpty()) {
                    val latest = backups.maxByOrNull { it.createdTime }!!
                    val date = java.text.SimpleDateFormat("dd MMM yyyy HH:mm", java.util.Locale.getDefault())
                        .format(java.util.Date(latest.createdTime))
                    _uiState.value = SettingsUiState.RestoreAvailable(latest.id, date)
                }
            }
        }
    }
    
    fun onRestoreConfirmed(fileId: String) {
        viewModelScope.launch {
            _uiState.value = SettingsUiState.RestoreInProgress
            
            // Re-use logic or call BackupManager
            val result = backupManager.restoreBackup(fileId)
            
            if (result.isSuccess) {
                _uiState.value = SettingsUiState.RestoreSuccess
            } else {
                // error handling? For now reset to Idle or show error
                _uiState.value = SettingsUiState.Idle
            }
        }
    }
    
    fun onRestoreCancelled() {
        _uiState.value = SettingsUiState.Idle
    }
    
    fun resetState() {
        _uiState.value = SettingsUiState.Idle
    }
}
