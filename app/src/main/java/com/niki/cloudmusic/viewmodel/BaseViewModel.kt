package com.niki.cloudmusic.viewmodel

import android.app.AlertDialog
import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.niki.cloudmusic.repository.PreferencesRepository
import com.niki.cloudmusic.repository.model.StateResponse
import com.niki.cloudmusic.view.custom.LoadingDialog

open class BaseViewModel(private val application: Application) : AndroidViewModel(application) {
    var TAG = "BaseViewModel"
    val preferencesRepository = PreferencesRepository(application.applicationContext)
    lateinit var loadingDialog: LoadingDialog
    private var dialog: AlertDialog? = null

    val isLoggedIn = MutableLiveData<Int>()

    init {
        isLoggedIn.value = if (preferencesRepository.getLoginStatus()) 1 else 0
    }

    fun getLoginData(): StateResponse.LoginResponse? {
        return preferencesRepository.getLogin()
    }

    fun showDialog() {
        dialog = loadingDialog.show()
    }

    fun dismissDialog() {
        if (dialog != null)
            dialog!!.dismiss()
    }

    fun toast(message: String) {
        Toast.makeText(application.applicationContext, message, Toast.LENGTH_SHORT).show()
    }

    fun isSuccess(code: Int?): Boolean {
        if (code == null)
            return false
        return code == 200
    }

    /**
     * 用于对网络请求结果的基本判断以及错误信息提示
     **/
    fun debug(data: Any?, code: Int?, message: String?, funName: String): Boolean {
        dismissDialog()
        if (code != 200) {
            toast("[$code]: $message")
        }
        if (data == null)
            Log.e(TAG, "$funName: failed")
        else
            Log.d(TAG, "$funName: success")
        return data != null
    }
}