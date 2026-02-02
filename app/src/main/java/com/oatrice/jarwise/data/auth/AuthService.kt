package com.oatrice.jarwise.data.auth

import kotlinx.coroutines.flow.StateFlow

interface AuthService {
    val currentUser: StateFlow<AuthUser?>
    suspend fun signIn(): Result<AuthUser>
    suspend fun signOut()
}
