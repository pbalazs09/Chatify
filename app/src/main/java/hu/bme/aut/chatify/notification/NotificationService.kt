package hu.bme.aut.chatify.notification

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Icon
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import hu.bme.aut.chatify.NavigationActivity
import hu.bme.aut.chatify.R
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.*


class NotificationService : FirebaseMessagingService() {

    private val NOTIFICATION_CHANNEL_ID = "1"
    private val NOTIFICATION_CHANNEL_NAME = "Messages"
    private val NOTIF_FOREGROUND_ID = 0

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        val uid = Firebase.auth.currentUser?.uid.toString()
        Firebase.firestore.collection("ClientTokens").document(uid).update("tokens.$token", true)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        if(message.data.isNotEmpty()){
            Log.d("Notif", "Notif")
            val data = message.data
            val receiver = data["receiver"].toString()
            val message = data["message"].toString()
            val conversationId = data["conversationId"].toString()
            Firebase.firestore.collection("Users").document(receiver).get().addOnSuccessListener {
                if(it.exists()){
                    val receiverName = it.data?.get("name").toString()
                    sendNotification(receiverName, message, conversationId)
                }
            }
        }
    }

    private fun getMyNotification(receiverName: String, message: String, conversationId: String) : Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }

        val notificationIntent = Intent(this, NavigationActivity::class.java)
        val contentIntent = PendingIntent.getActivity(this,
                NOTIF_FOREGROUND_ID,
                notificationIntent,
                PendingIntent.FLAG_ONE_SHOT)

        return NotificationCompat.Builder(
                this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(receiverName)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_notif_icon)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            //.setLargeIcon(getBitmapFromURL(""))
            .setVibrate(longArrayOf(1000, 2000, 1000))
            .setContentIntent(contentIntent)
            .setDeleteIntent(contentIntent)
            .setDefaults(0)
            .setAutoCancel(true)
            .build()
    }

    private fun sendNotification(receiverName: String, message: String, conversationId: String) {
        val notification = getMyNotification(receiverName, message, conversationId)
        val notifMan = getSystemService(NOTIFICATION_SERVICE) as NotificationManager?
        notifMan?.notify(NOTIF_FOREGROUND_ID, notification)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH)
        channel.setShowBadge(true)
        channel.enableLights(true)
        channel.enableVibration(true)
        channel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
        notificationManager?.createNotificationChannel(channel)
    }
}