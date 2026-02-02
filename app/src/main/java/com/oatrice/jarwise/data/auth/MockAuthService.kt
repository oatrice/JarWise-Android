package com.oatrice.jarwise.data.auth

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MockAuthService : AuthService {
    private val _currentUser = MutableStateFlow<AuthUser?>(null)
    override val currentUser: StateFlow<AuthUser?> = _currentUser.asStateFlow()

    override suspend fun signIn(): Result<AuthUser> {
        val user = AuthUser(
            id = "mock_id_123",
            name = "Mock User",
            email = "mock@example.com",
            photoUrl = "https://example.com/photo.jpg"
        )
        _currentUser.value = user
        return Result.success(user)
    }

    override suspend fun signOut() {
        _currentUser.value = null
    }
}
