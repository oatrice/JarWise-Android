package com.oatrice.jarwise.ui.login

import com.oatrice.jarwise.data.auth.AuthUser

sealed class LoginUiState {
    data object Idle : LoginUiState()
    data object Loading : LoginUiState()
    data class Success(val user: AuthUser) : LoginUiState()
    data class Error(val message: String) : LoginUiState()
    data class RestoreAvailable(val user: AuthUser, val backupName: String, val backupDate: String, val fileId: String) : LoginUiState()
    data object RestoreInProgress : LoginUiState()
    data object RestoreSuccess : LoginUiState()
}
