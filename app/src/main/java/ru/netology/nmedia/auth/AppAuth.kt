package ru.netology.nmedia.auth

import android.content.Context
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import ru.netology.nmedia.di.DependecyContainer
import ru.netology.nmedia.dto.PushToken
import kotlin.coroutines.EmptyCoroutineContext

class AppAuth(context: Context) {
    private val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
    private val idKey = "id"
    private val tokenKey = "token"

    private val _authStateFlow: MutableStateFlow<AuthState>

    init {
        val id = prefs.getLong(idKey, 0)
        val token = prefs.getString(tokenKey, null)

        if (id == 0L || token == null) {
            _authStateFlow = MutableStateFlow(AuthState())
            with(prefs.edit()) {
                clear()
                apply()
            }
        } else {
            _authStateFlow = MutableStateFlow(AuthState(id, token))
        }

        sendPushToken()
    }

    val authStateFlow: StateFlow<AuthState> = _authStateFlow.asStateFlow()

    @Synchronized
    fun setAuth(id: Long, token: String?) {
        _authStateFlow.value = AuthState(id, token)
        with(prefs.edit()) {
            putLong(idKey, id)
            putString(tokenKey, token)
            apply()
        }
        sendPushToken()
    }

    @Synchronized
    fun removeAuth() {
        _authStateFlow.value = AuthState()
        with(prefs.edit()) {
            clear()
            commit()
        }
        sendPushToken()
    }

//    fun sendPushToken(token: String? = null) {
//        CoroutineScope(Dispatchers.Default).launch {
//            try {
//                val pushToken = PushToken(token ?: Firebase.messaging.token.await())
//                Api.service.sendPushToken(pushToken)
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//        }
//    }

    fun sendPushToken(token: String? = null) {
        CoroutineScope(EmptyCoroutineContext).launch {
            runCatching {
                DependecyContainer.getInstance().apiService.sendPushToken(
                    PushToken(token ?: FirebaseMessaging.getInstance().token.await())
                )
            }
                .onFailure { it.printStackTrace() }
        }
    }

    fun isRecipientValid(recipientId: Long?): Boolean {
        val currentUserId = authStateFlow.value.id

        return when {
            recipientId == null -> true
            recipientId == 0L -> {
                if (currentUserId == 0L) {
                    true
                } else {
                    sendPushToken()
                    false
                }
            }
            recipientId == currentUserId -> true
            else -> {
                sendPushToken()
                false
            }
        }
    }
}

data class AuthState(val id: Long = 0, val token: String? = null)