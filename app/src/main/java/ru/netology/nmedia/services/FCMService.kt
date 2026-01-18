package ru.netology.nmedia.services

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import ru.netology.nmedia.R
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.dto.PushMessage
import kotlin.random.Random


class FCMService : FirebaseMessagingService() {

    private val action = "action"
    private val content = "content"
    private val gson = Gson()
    private val channelId = "remote"

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_remote_name)
            val descriptionText = getString(R.string.channel_remote_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        try {
            val contentJson = message.data["content"]
            if (contentJson == null) {
                handleActionMessage(message)
                return
            }

            val pushMessage = gson.fromJson(contentJson, PushMessage::class.java)
            val currentUserId = AppAuth.getInstance().authStateFlow.value.id

            when {
                pushMessage.recipientId == null -> {
                    sendNotification(pushMessage.content)
                }
                pushMessage.recipientId == currentUserId -> {
                    sendNotification(pushMessage.content)
                }
                else -> {
                    AppAuth.getInstance().sendPushToken()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            sendNotification("Новое уведомление")
        }
    }

    private fun sendNotification(text: String) {
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        notify(notification)
    }


    private fun handleActionMessage(message: RemoteMessage) {
        val action = message.data["action"]
        val content = message.data["content"]

        if (action != null && content != null) {
        }
    }

    @SuppressLint("StringFormatInvalid")
    private fun handleAddPost(content: AddPost) {

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(
                getString(R.string.notification_new_post, content.postAuthor))
            .setContentText(content.postTopic)
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText(content.postText))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        notify(notification)
    }

    private fun notify(notification: Notification) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        ) {
            NotificationManagerCompat.from(this).notify(Random.nextInt(100_000), notification)
        }
    }

    override fun onNewToken(token: String) {
        AppAuth.getInstance().sendPushToken(token)
    }

    enum class Action {
        LIKE, NEW_POST, ERROR;

        companion object {
            fun getValidAction(action: String): Action {
                return try {
                    valueOf(action)
                } catch (exception: IllegalArgumentException) {
                    ERROR
                }
            }
        }

    }

    data class Like(
        val userId: Long,
        val userName: String,
        val postId: Long,
        val postAuthor: String,
    )

    data class AddPost(
        val userId: Long,
        val postAuthor: String,
        val postText: String,
        val postTopic: String
    )

}