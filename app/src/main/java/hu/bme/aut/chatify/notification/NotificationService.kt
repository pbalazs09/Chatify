package hu.bme.aut.chatify.notification

import android.app.*
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.LocusId
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.Icon
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.graphics.drawable.toIcon
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.squareup.picasso.Picasso
import hu.bme.aut.chatify.NavigationActivity
import hu.bme.aut.chatify.R


class NotificationService : FirebaseMessagingService() {

    companion object{
        private const val NOTIFICATION_CHANNEL_ID = "1"
        private const val NOTIFICATION_CHANNEL_NAME = "Messages"
        private const val NOTIF_FOREGROUND_ID = 0
        private const val REQUEST_CONTENT = 1
        private const val REQUEST_BUBBLE = 2
    }
    private lateinit var shortcutManager: ShortcutManager
    private var shortcuts = arrayListOf<ShortcutInfo>()

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
            val sender = data["sender"].toString()
            val message = data["message"].toString()
            shortcutManager = getSystemService(ShortcutManager::class.java)
            if(Firebase.auth.currentUser?.uid.toString() == receiver){
                Firebase.firestore.collection("Users").document(sender).get().addOnSuccessListener {
                    if(it.exists()){
                        val senderName = it.data?.get("name").toString()
                        val photoUrl = it.data?.get("photoUrl").toString()
                        if(photoUrl.isNotEmpty()){
                            Picasso.get().load(photoUrl).into(object : com.squareup.picasso.Target {
                                override fun onBitmapLoaded(
                                    bitmap: Bitmap?,
                                    from: Picasso.LoadedFrom?
                                ) {
                                    sendNotification(senderName, bitmap!!, message, sender)
                                }

                                override fun onBitmapFailed(
                                    e: Exception?,
                                    errorDrawable: Drawable?
                                ) {
                                }

                                override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}
                            })
                        }
                        else{
                            sendNotification(senderName, null, message, sender)
                        }
                    }
                }
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    fun convertBitmapToAdaptive(bitmap: Bitmap?, context: Context): Bitmap? {
        val bitmapDrawable: Drawable = BitmapDrawable(context.resources, bitmap)
        val drawableIcon = AdaptiveIconDrawable(bitmapDrawable, bitmapDrawable)
        val result = Bitmap.createBitmap(
            drawableIcon.intrinsicWidth,
            drawableIcon.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(result)
        drawableIcon.setBounds(0, 0, canvas.width, canvas.height)
        drawableIcon.draw(canvas)
        return result
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun updateShortcuts(sender: String, senderName: String, bitmap: Bitmap?) {
        val shortcut = shortcuts.find { it.id == sender }
        val person = Person.Builder()
        var icon: Icon? = null
        val scut = ShortcutInfo.Builder(this, sender)
        if(bitmap != null){
            icon = Icon.createWithAdaptiveBitmap(convertBitmapToAdaptive(bitmap, this))
            person.setName(senderName).setIcon(bitmap.toIcon())
            scut.setIcon(icon)
        }
        else{
            person.setName(senderName).setIcon(Icon.createWithResource(this, R.drawable.ic_account))
            scut.setIcon(Icon.createWithResource(this, R.drawable.ic_account))
        }
        val intent = Intent(this, NavigationActivity::class.java).setAction(Intent.ACTION_VIEW)
        intent.putExtra("sender", sender)
        if(shortcut == null){
            scut.setLocusId(LocusId(sender))
                .setActivity(ComponentName(this, NavigationActivity::class.java))
                .setShortLabel(senderName)
                .setLongLived(true)
                .setCategories(setOf("com.example.android.bubbles.category.TEXT_SHARE_TARGET"))
                .setIntent(intent)
                .setPerson(person.build())
            shortcuts.add(scut.build())
        }
        // Create a dynamic shortcut for each of the contacts.
        // The same shortcut ID will be used when we show a bubble notification.
        // Move the important contact to the front of the shortcut list.
        shortcuts = ArrayList(shortcuts.sortedByDescending { it.id == sender })
        // Truncate the list if we can't show all of our contacts.
        val maxCount = shortcutManager.maxShortcutCountPerActivity
        if (shortcuts.size > maxCount) {
            shortcuts = ArrayList(shortcuts.take(maxCount))
        }
        shortcutManager.addDynamicShortcuts(shortcuts)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getBubble(senderName: String, senderIcon: Bitmap?, message: String, sender: String) : Notification {
        val target = Intent(this, NavigationActivity::class.java).setAction(Intent.ACTION_VIEW)
        target.putExtra("sender", sender)
        val pendingIntent = PendingIntent.getActivity(
            this,
            REQUEST_BUBBLE,
            target,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        val person = Person.Builder()
        if(senderIcon != null){
            person.setName(senderName).setIcon(Icon.createWithAdaptiveBitmap(convertBitmapToAdaptive(senderIcon, this)))
        }
        else{
            person.setName(senderName).setIcon(Icon.createWithResource(this, R.drawable.ic_account))
        }
        person.setImportant(true)
        val you = Person.Builder().setName("You").build()
        val notification = Notification.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setBubbleMetadata(
                    Notification.BubbleMetadata.Builder(
                        pendingIntent, Icon.createWithResource(
                            this,
                            R.drawable.ic_account
                        )
                    )
                        .setDesiredHeight(this.resources.getDimensionPixelSize(R.dimen.bubble_height))
                        //.setAutoExpandBubble(true)
                        //.setSuppressNotification(true)
                        .build()
                )
                .setContentTitle("Teszt")
                .setSmallIcon(R.drawable.ic_chat)
                .setCategory(Notification.CATEGORY_MESSAGE)
                .setShortcutId(sender)
                .setLocusId(LocusId(sender))
                .addPerson(person.build())
                .setShowWhen(true)
                .setContentIntent(
                    PendingIntent.getActivity(
                        this,
                        REQUEST_CONTENT,
                        Intent(this, NavigationActivity::class.java)
                            .setAction(Intent.ACTION_VIEW),
                        PendingIntent.FLAG_UPDATE_CURRENT
                    )
                )
                .setStyle(Notification.MessagingStyle(you).apply {
                    addMessage(Notification.MessagingStyle.Message(message, 0, person.build()))
                })
                .build()
        updateShortcuts(sender, senderName, senderIcon)
        return notification
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getPush(senderName: String, senderIcon: Bitmap?, message: String, sender: String) : Notification {
        val person = Person.Builder().setName(senderName).setIcon(
            Icon.createWithResource(
                this,
                R.drawable.ic_chat
            )
        ).build()
        val user = Person.Builder().setName("You").build()
        val notificationIntent = Intent(this, NavigationActivity::class.java)
        notificationIntent.putExtra("sender", sender)
        val contentIntent = PendingIntent.getActivity(
            this,
            NOTIF_FOREGROUND_ID,
            notificationIntent,
            PendingIntent.FLAG_ONE_SHOT
        )

        return NotificationCompat.Builder(
            this, NOTIFICATION_CHANNEL_ID
        )
                .setContentTitle(senderName)
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_notif_icon)
                .setVibrate(longArrayOf(1000, 2000, 1000))
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setContentIntent(contentIntent)
                .setDefaults(0)
                .setAutoCancel(true)
                .build()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getMyNotification(
        senderName: String,
        senderIcon: Bitmap?,
        message: String,
        sender: String
    ) : Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }
        return getBubble(senderName, senderIcon, message, sender)
        /*val person = Person.Builder()
        if(senderIcon != null){
            person.setName(senderName).setIcon(senderIcon.toIcon())
        }
        else{
            person.setName(senderName)
        }
        val you = Person.Builder().setName("You").build()
        val notificationIntent = Intent(this, NavigationActivity::class.java)
        notificationIntent.putExtra("sender", sender)
        val contentIntent = PendingIntent.getActivity(this,
            NOTIF_FOREGROUND_ID,
            notificationIntent,
            PendingIntent.FLAG_ONE_SHOT)

        return Notification.Builder(
            this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(senderName)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_notif_icon)
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .setStyle(Notification.MessagingStyle(you).apply {
                addMessage(Notification.MessagingStyle.Message(message, 0, person.build()))
            })
            .build()*/
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun sendNotification(
        senderName: String,
        senderIcon: Bitmap?,
        message: String,
        sender: String
    ) {
        val notification = getMyNotification(senderName, senderIcon, message, sender)
        val notifMan = getSystemService(NOTIFICATION_SERVICE) as NotificationManager?
        notifMan?.notify(sender.hashCode(), notification)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        )
        channel.setShowBadge(true)
        channel.enableLights(true)
        channel.enableVibration(true)
        channel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
        notificationManager?.createNotificationChannel(channel)
    }
}