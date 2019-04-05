package com.example.stickyheader

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.drawable.Icon
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.support.annotation.DrawableRes
import android.support.annotation.WorkerThread
import android.support.v4.app.ActivityCompat
import android.support.v4.app.NotificationCompat
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        location.setOnClickListener {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), 10)
        }

        fullScreen.setOnClickListener {
            createChannel()
            startForegroundService(Intent(this, FullScreenService::class.java))
        }

        bubble.setOnClickListener {
            val icon = Icon.createWithBitmap((roundIcon(this, R.drawable.dog)))
            val builder = Notification.Builder(this, CHANNEL_ID)
                // A notification can be shown as a bubble by calling setBubbleMetadata()
                .setBubbleMetadata(
                    Notification.BubbleMetadata.Builder()
                        // The height of the expanded bubble.
                        .setDesiredHeight(resources.getDimensionPixelSize(R.dimen.bubble_height))
                        // The icon of the bubble.
                        // TODO: The icon is not displayed in Android Q Beta 2.
                        .setIcon(icon)
                        .apply {
                            // When the bubble is explicitly opened by the user, we can show the bubble automatically
                            // in the expanded state. This works only when the app is in the foreground.
                            // TODO: This does not yet work in Android Q Beta 2.
//                            if (fromUser) {
                            setAutoExpandBubble(true)
                            setSuppressInitialNotification(true)
//                            }
                        }
                        // The Intent to be used for the expanded bubble.
                        .setIntent(
                            PendingIntent.getActivity(
                                this,
                                10,
                                // Launch BubbleActivity as the expanded bubble.
                                Intent(this, FullScreenActivity::class.java),
                                PendingIntent.FLAG_UPDATE_CURRENT
                            )
                        )
                        .build()
                )
            builder.setContentTitle("Title")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setCategory(Notification.CATEGORY_MESSAGE)
                .setShowWhen(true)
                // The content Intent is used when the user clicks on the "Open Content" icon button on the expanded bubble,
                // as well as when the fall-back notification is clicked.
                .setContentIntent(
                    PendingIntent.getActivity(
                        this,
                        5,
                        Intent(this, MainActivity::class.java),
                        PendingIntent.FLAG_UPDATE_CURRENT
                    )
                )
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(1, builder.build())
        }

        noInternet.setOnClickListener {
            val panelIntent = Intent(Settings.Panel.ACTION_INTERNET_CONNECTIVITY)
            startActivity(panelIntent)
        }
    }

    @WorkerThread
    private fun roundIcon(context: Context, @DrawableRes id: Int): Bitmap {
        val original = BitmapFactory.decodeResource(context.resources, id)
        val width = original.width
        val height = original.height
        val rect = Rect(0, 0, width, height)
        val icon = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val paint = Paint().apply {
            isAntiAlias = true
            color = Color.BLACK
        }
        icon.applyCanvas {
            drawARGB(0, 0, 0, 0)
            drawOval(0f, 0f, width.toFloat(), height.toFloat(), paint)
            paint.blendMode = BlendMode.SRC_IN
            drawBitmap(original, rect, rect, paint)
        }
        return icon
    }

    inline fun Bitmap.applyCanvas(block: Canvas.() -> Unit): Bitmap {
        val c = Canvas(this)
        c.block()
        return this
    }


    private fun createChannel() {
        // Create the NotificationChannel
        val name = getString(R.string.channel_name)
        val descriptionText = getString(R.string.channel_description)
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val mChannel = NotificationChannel(CHANNEL_ID, name, importance)
        mChannel.description = descriptionText
        // Register the channel with the system; you can't change the importance
        // or other notification behaviors after this
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(mChannel)
    }

    companion object {
        const val CHANNEL_ID = "CHANNELS"
    }
}
