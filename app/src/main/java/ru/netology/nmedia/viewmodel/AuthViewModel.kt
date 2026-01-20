package ru.netology.nmedia.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import ru.netology.nmedia.api.ApiService
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.auth.AuthState
import ru.netology.nmedia.auth.LoginFormState
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val apiService: ApiService,
    private val appAuth: AppAuth,
) : ViewModel() {
    val data : LiveData<AuthState> = appAuth
        .authStateFlow
        .asLiveData(Dispatchers.Default)
    val authenticated: LiveData<Boolean> = appAuth
        .authStateFlow
        .map { it.id != 0L }
        .asLiveData(Dispatchers.Default)

    private val _loginForm = MutableLiveData<LoginFormState>()
    val loginFormState: LiveData<LoginFormState> = _loginForm

    fun loginDataChanged(username: String, password: String) {
        if (!isUserNameValid(username)) {
            _loginForm.value = LoginFormState(isDataValid = false)
        } else if (!isPasswordValid(password)) {
            _loginForm.value = LoginFormState(isDataValid = false)
        } else {
            _loginForm.value = LoginFormState(isDataValid = true)
        }
    }

    private fun isUserNameValid(username: String): Boolean {
        return username.length > 0
    }

    private fun isPasswordValid(password: String): Boolean {
        return password.length > 0
    }

    suspend fun authenticate(login: String, pass: String): AuthState {
        return try {
            val response = apiService.updateUser(login, pass)
            appAuth.setAuth(response.id, response.token)
            response
        } catch (e: Exception) {
            AuthState()
        }
    }

}