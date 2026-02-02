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
}
