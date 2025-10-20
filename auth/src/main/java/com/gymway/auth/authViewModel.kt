package com.gymway.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val repository = AuthRepository()

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState

    fun register(email: String, password: String, displayName: String, role: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading

            val result = repository.registerWithEmail(email, password, displayName, role)

            if (result.isSuccess) {
                _uiState.value = AuthUiState.NeedEmailVerification
            } else {
                val errorMessage = result.exceptionOrNull()?.message ?: "Registration failed"
                _uiState.value = AuthUiState.Error(errorMessage)
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading

            val result = repository.loginWithEmail(email, password)

            if (result.isSuccess) {
                val uid = result.getOrNull() ?: ""
                _uiState.value = AuthUiState.Success(uid)
            } else {
                val errorMessage = result.exceptionOrNull()?.message ?: "Login failed"
                _uiState.value = AuthUiState.Error(errorMessage)
            }
        }
    }

    fun checkEmailVerification() {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading

            val result = repository.checkEmailVerification()
            if (result.isSuccess && result.getOrNull() == true) {
                val uid = repository.getCurrentUser()?.uid ?: ""
                _uiState.value = AuthUiState.Success(uid)
            } else {
                _uiState.value = AuthUiState.Error("ایمیل شما هنوز تأیید نشده است")
            }
        }
    }

    fun resendVerificationEmail() {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading

            val result = repository.resendVerificationEmail()
            if (result.isSuccess) {
                _uiState.value = AuthUiState.NeedEmailVerification
            } else {
                _uiState.value = AuthUiState.Error("خطا در ارسال ایمیل تأیید")
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            repository.signOut()
            _uiState.value = AuthUiState.Idle
        }
    }

    fun resetState() {
        _uiState.value = AuthUiState.Idle
    }
}