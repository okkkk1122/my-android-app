package com.gymway.auth

// حالات UI برای auth
sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    data class Success(val uid: String) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
    object NeedEmailVerification : AuthUiState()
}