package com.niki.cloudmusic

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.BitmapFactory
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.niki.cloudmusic.repository.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL


class MyService : Service() {
    lateinit var myRemoteViews: RemoteViews
    private val binder = MyBinder()

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate() {
        super.onCreate()

        myRemoteViews = RemoteViews(this@MyService.packageName, R.layout.layout_notification)
        startForeground(
            123,
            createNotification(),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
        )
    }

    private suspend fun setCover(
        imageUrl: String,
        callback: () -> Unit
    ) {
        withContext(Dispatchers.IO) {
            try {
                val url = URL(imageUrl)
                val bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream())
                withContext(Dispatchers.Main) {
                    myRemoteViews.setImageViewBitmap(R.id.nCover, bitmap)
                    callback()
                }
            } catch (ignored: Exception) {
                callback()
            }
        }
    }

    suspend fun setSong(song: Song, callback: () -> Unit) {
        val builder = StringBuilder()
        for (artist in song.ar!!) {
            builder.append(artist.name)
            if (song.ar!!.indexOf(artist) != song.ar!!.size - 1)
                builder.append(" & ")
        }
        myRemoteViews.setTextViewText(R.id.nSongName, song.name)
        myRemoteViews.setTextViewText(R.id.nArtistName, builder.toString())
        setCover(song.al!!.picUrl, callback)
    }

    fun setPLayingStatus(isPlaying: Boolean) {
        if (isPlaying)
            myRemoteViews.setImageViewResource(R.id.nPlay, R.drawable.ic_pause)
        else
            myRemoteViews.setImageViewResource(R.id.nPlay, R.drawable.ic_play)
    }

    private fun sendBroadcast(action: String) {
        val intent = Intent(action)
        sendBroadcast(intent)
    }

    fun createNotification(): Notification {
        val intentPlay = Intent(this, MyService::class.java)
        intentPlay.action = "play"
        val pendingIntentPlay =
            PendingIntent.getService(this, 0, intentPlay, PendingIntent.FLAG_MUTABLE)
        myRemoteViews.setOnClickPendingIntent(R.id.nPlay, pendingIntentPlay)

        val intentPrevious = Intent(this, MyService::class.java)
        intentPrevious.action = "previous"
        val pendingIntentPrevious =
            PendingIntent.getService(this, 1, intentPrevious, PendingIntent.FLAG_MUTABLE)
        myRemoteViews.setOnClickPendingIntent(R.id.nPrevious, pendingIntentPrevious)

        val intentNext = Intent(this, MyService::class.java)
        intentNext.action = "next"
        val pendingIntentNext =
            PendingIntent.getService(this, 2, intentNext, PendingIntent.FLAG_MUTABLE)
        myRemoteViews.setOnClickPendingIntent(R.id.nNext, pendingIntentNext)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "123",
                "S",
                NotificationManager.IMPORTANCE_LOW
            )
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, "123")
            .setSmallIcon(R.drawable.icon)
            .setCustomContentView(myRemoteViews)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        return notification
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {

        when (intent.action) {
            "play" -> {
                Log.d("MyService", "发送play广播")
                sendBroadcast("com.niki.cloudmusic.ACTION_PLAY")
            }

            "previous" -> {
                Log.d("MyService", "发送previous广播")
                sendBroadcast("com.niki.cloudmusic.ACTION_PREVIOUS")
            }

            "next" -> {
                Log.d("MyService", "发送next广播")
                sendBroadcast("com.niki.cloudmusic.ACTION_NEXT")
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }


    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    inner class MyBinder : Binder() {
        fun getService(): MyService = this@MyService
    }
}
