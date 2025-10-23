package com.gymway.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.gymway.auth.model.User

class AuthViewModel : ViewModel() {
    private val repository = AuthRepository()

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

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
                // Load user data after successful login
                val userResult = repository.getUserData(uid)
                if (userResult.isSuccess) {
                    _currentUser.value = userResult.getOrNull()
                    _uiState.value = AuthUiState.Success(uid)
                } else {
                    _uiState.value = AuthUiState.Error("ورود موفق اما خطا در بارگذاری پروفایل")
                }
            } else {
                val errorMessage = result.exceptionOrNull()?.message ?: "Login failed"
                _uiState.value = AuthUiState.Error(errorMessage)
            }
        }
    }

    fun loadCurrentUser() {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading

            val result = repository.getCurrentUserData()
            if (result.isSuccess) {
                _currentUser.value = result.getOrNull()
                _uiState.value = AuthUiState.Success(result.getOrNull()?.uid ?: "")
            } else {
                _uiState.value = AuthUiState.Error("خطا در بارگذاری اطلاعات کاربر")
            }
        }
    }

    fun checkEmailVerification() {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading

            val result = repository.checkEmailVerification()
            if (result.isSuccess && result.getOrNull() == true) {
                val uid = repository.getCurrentUser()?.uid ?: ""
                // Load user data after email verification
                val userResult = repository.getUserData(uid)
                if (userResult.isSuccess) {
                    _currentUser.value = userResult.getOrNull()
                    _uiState.value = AuthUiState.Success(uid)
                } else {
                    _uiState.value = AuthUiState.Error("ایمیل تأیید شد اما خطا در بارگذاری پروفایل")
                }
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
            _currentUser.value = null
            _uiState.value = AuthUiState.Idle
        }
    }

    fun resetState() {
        _uiState.value = AuthUiState.Idle
    }
}