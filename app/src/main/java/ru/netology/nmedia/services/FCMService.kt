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
        message.data[action]?.let {
            when (Action.getValidAction(it)) {
                Action.LIKE -> handleLike(gson.fromJson(message.data[content], Like::class.java))
                Action.NEW_POST -> handleAddPost(gson.fromJson(message.data[content], AddPost::class.java))
                Action.ERROR -> println("ERROR_PUSH")
            }
        }
    }

    private fun handleLike(content: Like) {
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(
                getString(R.string.notification_user_liked, content.userName, content.postAuthor))

            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        notify(notification)
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
        Log.i("fcm token", token)
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
        val userId: Int,
        val userName: String,
        val postId: Int,
        val postAuthor: String,
    )

    data class AddPost(
        val userId: Int,
        val postAuthor: String,
        val postText: String,
        val postTopic: String
    )

}