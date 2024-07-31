package com.niki.cloudmusic

import android.app.Service
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MusicService : Service() {
    var isPlaying = false
    var retryTimes = 3

    private var mediaPlayer: MediaPlayer? = null
    private var musicProgressJob: Job? = null

    override fun onBind(intent: Intent): IBinder {
        return MusicBinder()
    }

    override fun onCreate() {
        super.onCreate()
        newMediaPlayer()
    }

    private fun newMediaPlayer() {
        mediaPlayer = MediaPlayer()
        mediaPlayer!!.apply {
            setAudioAttributes(
                AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            setOnCompletionListener {
                switchByBoolean(false)
                sendBroadcast(
                    Intent("com.niki.cloudmusic.music.NEXT_ONE")
                )
            }
        }
    }

    private fun switchByBoolean(status: Boolean) {
        if (isPlaying != status) {
            sendBroadcast(
                Intent("com.niki.cloudmusic.music.NEW_STATUS").putExtra(
                    "status",
                    status
                )
            )
            isPlaying = status
        }
    }

    private fun switchByForce(status: Boolean) {
        sendBroadcast(
            Intent("com.niki.cloudmusic.music.NEW_STATUS").putExtra(
                "status",
                status
            )
        )
        isPlaying = status
    }

    fun playMusic(url: String) {
        if (mediaPlayer != null) {
            mediaPlayer!!.stop() // 停止播放
            mediaPlayer!!.release() // 释放资源
            mediaPlayer = null
        }

        newMediaPlayer()
        try {
            mediaPlayer!!.setDataSource(url)
            mediaPlayer!!.prepare()
            mediaPlayer!!.start()
            switchByForce(true)
        } catch (ignored: Exception) {
            switchByForce(false)
            if (retryTimes > 0) {
                retryTimes--
                playMusic(url)
            } else {
                retryTimes = 3
            }
        }
    }

    fun switchStatus() {
        if (mediaPlayer == null) return
        if (isPlaying) {
            mediaPlayer!!.pause()
            isPlaying = false
        } else {
            mediaPlayer!!.start()
            isPlaying = true
        }
        sendBroadcast(
            Intent("com.niki.cloudmusic.music.NEW_STATUS").putExtra(
                "status",
                isPlaying
            )
        )
    }

    fun seekTo(progress: Int) {
        if (mediaPlayer == null) return
        val duration = mediaPlayer!!.duration
        val seekToPosition = (progress * duration) / 100
        mediaPlayer!!.seekTo(seekToPosition)
    }

    fun updateMusicProgress() {
        musicProgressJob?.cancel()

        musicProgressJob = CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                if (mediaPlayer == null) {
                    return@launch
                }
                val currentPosition = mediaPlayer!!.currentPosition
                val duration = mediaPlayer!!.duration
                val progress = (currentPosition * 100f) / duration
                sendBroadcast(
                    Intent("com.niki.cloudmusic.music.NEW_PROGRESS").putExtra(
                        "progress",
                        progress.toInt()
                    )
                )
                delay(300)
            }
        }
    }

    inner class MusicBinder : Binder() {
        fun getService(): MusicService = this@MusicService

        fun updateMusicProgress() {
            Log.e("lllllllllllllllllllllll", "updateMusicProgress: lllllllllllllllllllllll")
            this@MusicService.updateMusicProgress()
        }

        fun seekTo(progress: Int) {
            Log.e("lllllllllllllllllllllll", "seekTo: lllllllllllllllllllllll")
            this@MusicService.seekTo(progress)
        }

        fun switchStatus() {
            Log.e("lllllllllllllllllllllll", "switchStatus: lllllllllllllllllllllll")
            this@MusicService.switchStatus()
        }

        fun playMusic(url: String) {
            Log.e("lllllllllllllllllllllll", "playMusic: lllllllllllllllllllllll")
            this@MusicService.playMusic(url)
        }
    }
}