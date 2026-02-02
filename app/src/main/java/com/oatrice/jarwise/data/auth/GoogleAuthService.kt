package com.oatrice.jarwise.data.auth

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

class GoogleAuthService(private val context: Context) : AuthService {
    private val _currentUser = MutableStateFlow<AuthUser?>(null)
    override val currentUser: StateFlow<AuthUser?> = _currentUser.asStateFlow()

    init {
        // Check for existing signed-in user
        val account = GoogleSignIn.getLastSignedInAccount(context)
        if (account != null) {
            _currentUser.value = AuthUser(
                id = account.id ?: "",
                name = account.displayName ?: "",
                email = account.email ?: "",
                photoUrl = account.photoUrl?.toString()
            )
        }
    }

    override suspend fun signIn(): Result<AuthUser> {
        // In real implementation, this needs Activity context or result launcher.
        // For Service, we usually just return state or handle via repository.
        // This is a simplified interface for now.
        // Real Google Sign-In requires Activity interaction (startActivityForResult).
        // So this method might need to be "handleSignInResult" or similar, 
        // OR we inject a launcher helper. 
        // For now, let's keep it consistent with interface but note limitations.
        return Result.failure(Exception("Google Sign-In requires Activity interaction"))
    }
    
    fun getSignInIntent(): android.content.Intent {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestProfile()
            .requestScopes(com.google.android.gms.common.api.Scope(com.google.api.services.drive.DriveScopes.DRIVE_FILE), com.google.android.gms.common.api.Scope(com.google.api.services.drive.DriveScopes.DRIVE_APPDATA))
            .build()
        val client = GoogleSignIn.getClient(context, gso)
        return client.signInIntent
    }

    suspend fun handleSignInResult(intent: android.content.Intent?): Result<AuthUser> {
        return try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(intent)
            val account = task.await()
            val user = AuthUser(
                id = account.id ?: "",
                name = account.displayName ?: "",
                email = account.email ?: "",
                photoUrl = account.photoUrl?.toString()
            )
            _currentUser.value = user
            android.util.Log.d("GoogleAuthService", "Sign-in successful: ${user.email} (${user.name})")
            Result.success(user)
        } catch (e: Exception) {
            if (e is com.google.android.gms.common.api.ApiException) {
                android.util.Log.e("GoogleAuthService", "Sign-in failed code: ${e.statusCode}, message: ${e.message}")
            } else {
                android.util.Log.e("GoogleAuthService", "Sign-in failed", e)
            }
            Result.failure(e)
        }
    }

    override suspend fun signOut() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
        val client = GoogleSignIn.getClient(context, gso)
        try {
            client.signOut().await()
            _currentUser.value = null
        } catch (e: Exception) {
            e.printStackTrace()
            // Even if Google sign out fails, clear local state
            _currentUser.value = null
        }
    }
}
