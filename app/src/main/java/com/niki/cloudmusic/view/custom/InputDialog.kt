package com.niki.cloudmusic.view.custom

import android.app.AlertDialog
import android.content.Context
import android.text.InputType
import android.widget.EditText

/** 一个要求输入的弹框，不可取消，有使用默认值、确定（使用输入框值）两个选项
 **/
class InputDialog(private val context: Context) {

    fun showDialog(
        title: String,
        localhost: String,
        /**
         *  可以设置回调的变量 false -> localhost
         */
        onInputComplete: (Boolean) -> Unit
    ) {
        val input = EditText(context)
        input.inputType = InputType.TYPE_CLASS_TEXT
        input.setText(localhost)

        val dialog = AlertDialog.Builder(context)
            .setTitle(title)
            .setView(input)
            .setCancelable(false)
            .setNeutralButton("localhost") { _, _ ->
                onInputComplete(false)
            }
            .setPositiveButton("natapp") { _, _ ->
                onInputComplete(true)
            }
            .create()

        dialog.show()

        /* 不可点击其他地方返回 */
        dialog.setCanceledOnTouchOutside(false)

        /* 接管返回按钮事件 */
        dialog.setOnKeyListener { _, keyCode, _ ->
            keyCode == android.view.KeyEvent.KEYCODE_BACK
        }
    }
}