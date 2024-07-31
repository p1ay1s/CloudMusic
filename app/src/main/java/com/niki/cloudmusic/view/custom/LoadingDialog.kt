package com.niki.cloudmusic.view.custom

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import com.niki.cloudmusic.R

/* 一个耗时操作的加载界面，展示透明黑色背景以及一个process bar */
class LoadingDialog(context: Context) : AlertDialog.Builder(context) {
    private val alertDialog: AlertDialog

    init {
        AlertDialog.Builder(context).let {
            it.setView(LayoutInflater.from(context).inflate(R.layout.layout_loading, null))
            it.setCancelable(false)
            alertDialog = it.create()
        }
        /*将自带的对话框样式变成透明*/
        alertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    override fun show(): AlertDialog {
        alertDialog.show()
        return alertDialog
    }
}