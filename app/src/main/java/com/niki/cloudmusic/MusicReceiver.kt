package com.niki.cloudmusic

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.niki.cloudmusic.viewmodel.MusicViewModel
//
//class MusicReceiver(private val viewModel: MusicViewModel) : BroadcastReceiver() {
//    override fun onReceive(context: Context?, intent: Intent?) {
//        when (intent?.action) {
//            "com.niki.cloudmusic.music.NEW_STATUS" -> {
//                val status = intent.getBooleanExtra("status", false)
//                viewModel.isPlaying.value = status
//            }
//
//            "com.niki.cloudmusic.music.NEW_PROGRESS" -> {
//                val progress = intent.getIntExtra("progress", 0)
//                viewModel.songPosition.value = progress
//            }
//
//            "com.niki.cloudmusic.music.FINISHED" -> {
//            }
//        }
//    }
//}