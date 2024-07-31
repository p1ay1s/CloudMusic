package com.niki.cloudmusic.view.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment

/**
 * 目前还没有实质性的作用
 **/
open class DefaultFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(tag, "onCreate: 开始")
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d(tag, "onViewCreated: 开始")
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onResume() {
        Log.d(tag, "onResume: 开始")
        super.onResume()
    }

    override fun onPause() {
        Log.d(tag, "onPause: 开始")
        super.onPause()
    }

    override fun onDestroy() {
        Log.d(tag, "onDestroy: 开始")
        super.onDestroy()
    }
}