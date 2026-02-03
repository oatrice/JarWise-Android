package com.oatrice.jarwise.data.auth

data class AuthUser(
    val id: String,
    val name: String,
    val email: String,
    val photoUrl: String? = null
)
