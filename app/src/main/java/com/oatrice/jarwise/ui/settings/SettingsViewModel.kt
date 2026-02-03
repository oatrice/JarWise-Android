package com.oatrice.jarwise.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oatrice.jarwise.data.auth.AuthService
import com.oatrice.jarwise.data.backup.BackupManager
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val authService: AuthService,
    private val backupManager: BackupManager
) : ViewModel() {

    val user = authService.currentUser
    val syncStatus = backupManager.syncStatus

    fun triggerBackup() {
        backupManager.triggerManualBackup() 
    }

    fun signOut() {
        viewModelScope.launch {
            authService.signOut()
        }
    }

    fun getSignInIntent(): android.content.Intent {
        return (authService as? com.oatrice.jarwise.data.auth.GoogleAuthService)?.getSignInIntent() 
            ?: android.content.Intent()
    }

    fun handleSignInResult(intent: android.content.Intent?) {
        viewModelScope.launch {
            if (authService is com.oatrice.jarwise.data.auth.GoogleAuthService) {
                val result = authService.handleSignInResult(intent)
                result.onSuccess {
                    // Logic for post-login (check backup?)
                    // For now, simpler than LoginViewModel, just link/login.
                    // If we want to check backup, we'd need to prompt user.
                    // Given request "Sign in -> Dialog", simplest is assume just login.
                    // If they have data, it's fine.
                }
            } else if (authService is com.oatrice.jarwise.data.auth.MockAuthService) {
                 authService.signIn()
            }
        }
    }
}
