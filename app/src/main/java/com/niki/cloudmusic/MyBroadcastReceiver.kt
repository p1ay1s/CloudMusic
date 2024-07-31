package com.niki.cloudmusic

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.niki.cloudmusic.viewmodel.MusicViewModel

class MyBroadcastReceiver(val viewModel: MusicViewModel) : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            "com.niki.cloudmusic.ACTION_PLAY" -> {
                Log.d("MyService", "接受play广播")
                viewModel.switchStatus()
            }
            "com.niki.cloudmusic.ACTION_PREVIOUS" -> {
                Log.d("MyService", "接受previous广播")
                viewModel.previousSong()
            }
            "com.niki.cloudmusic.ACTION_NEXT" -> {
                Log.d("MyService", "接受next广播")
                viewModel.nextSong()
            }
        }
    }
}