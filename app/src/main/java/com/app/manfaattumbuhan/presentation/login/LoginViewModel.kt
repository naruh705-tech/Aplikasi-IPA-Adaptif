package com.app.manfaattumbuhan.presentation.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.manfaattumbuhan.domain.model.User
import com.app.manfaattumbuhan.domain.model.UserRole
import com.app.manfaattumbuhan.domain.usecase.LoginUseCase

class LoginViewModel(private val loginUseCase: LoginUseCase) : ViewModel() {

    private val _loginResult = MutableLiveData<LoginResult>()
    val loginResult: LiveData<LoginResult> = _loginResult

    private val _selectedRole = MutableLiveData<UserRole>()
    val selectedRole: LiveData<UserRole> = _selectedRole

    fun selectRole(role: UserRole) {
        _selectedRole.value = role
    }

    fun login(username: String, password: String) {
        if (username.isBlank() || password.isBlank()) {
            _loginResult.value = LoginResult.Error("Username dan password harus diisi")
            return
        }
        val user = loginUseCase.execute(username, password)
        if (user != null) {
            _loginResult.value = LoginResult.Success(user)
        } else {
            _loginResult.value = LoginResult.Error("Username atau password salah")
        }
    }
}

sealed class LoginResult {
    data class Success(val user: User) : LoginResult()
    data class Error(val message: String) : LoginResult()
}
