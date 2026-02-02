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
        // We might want an explicit "force" backup method later, 
        // but for now triggerBackup checks debounce. 
        // If user spams click, debounce resets, which is fine, or we can add immediate implementation.
        // Given BackupManager implementation, triggerBackup starts a 10s timer.
        // The user likely expects IMMEDIATE backup when clicking the button.
        // I should probably add a forceBackup() or immediateBackup() to BackupManager later.
        // For now, let's just call triggerBackup() and maybe I'll refactor BackupManager to allow immediate execution.
        backupManager.triggerBackup() 
    }

    fun signOut() {
        viewModelScope.launch {
            authService.signOut()
        }
    }
}
